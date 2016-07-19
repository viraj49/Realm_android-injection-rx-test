package tank.viraj.realm.dataSource;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import rx.Observable;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileDataSource {
    private Context context;
    private GitHubApiInterface gitHubApiInterface;
    private InternetConnection internetConnection;

    private RxSchedulerConfiguration rxSchedulerConfiguration;
    private Realm realm;

    public GitHubUserProfileDataSource(Context context,
                                       GitHubApiInterface gitHubApiInterface,
                                       InternetConnection internetConnection,
                                       RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.context = context;
        this.gitHubApiInterface = gitHubApiInterface;
        this.internetConnection = internetConnection;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.realm = Realm.getDefaultInstance();
    }

    public Observable<Boolean> GetGitHubUserProfile(String login, boolean isForced) {
        return Observable.concat(getGitHubUserProfileFromRealm(login, isForced),
                getGitHubUserProfileFromRetrofit(login))
                .first(profile -> profile);
    }

    private Observable<Boolean> getGitHubUserProfileFromRealm(String login, boolean isForced) {
        return Observable.just(isForced)
                .filter(isForcedIn -> !isForcedIn)
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .map(isForcedIn -> realm.where(GitHubUserProfile.class)
                        .equalTo("login", login)
                        .findFirst() != null);
    }

    private Observable<Boolean> getGitHubUserProfileFromRetrofit(String login) {
        return internetConnection.isInternetOn(context)
                .filter(connectionStatus -> connectionStatus)
                .switchMap(connectionStatus -> gitHubApiInterface.getGitHubUserProfile(login))
                .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .map(gitHubUserProfile -> {
                    realm.executeTransactionAsync(realm1 -> realm1.copyToRealmOrUpdate(gitHubUserProfile));
                    return gitHubUserProfile != null;
                });
    }

    public void unSubscribe() {
        realm.close();
    }
}