package tank.viraj.realm.dataSource;

import android.content.Context;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import rx.Observable;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListDataSource {
    private Context context;
    private Realm realm;
    private GitHubApiInterface gitHubApiInterface;
    private InternetConnection internetConnection;

    private RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserListDataSource(Context context, GitHubApiInterface gitHubApiInterface,
                                    InternetConnection internetConnection,
                                    RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.context = context;
        this.gitHubApiInterface = gitHubApiInterface;
        this.internetConnection = internetConnection;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.realm = Realm.getDefaultInstance();
    }

    public Observable<Integer> getGitHubUsers(boolean isForced) {
        return Observable.concat(
                getGitHubUsersFromRealm(isForced),
                getGitHubUsersFromRetrofit())
                .first(gitHubUserListSize -> gitHubUserListSize > 0);
    }

    private Observable<Integer> getGitHubUsersFromRealm(boolean isForced) {
        return Observable.just(isForced)
                .filter(isForcedIn -> !isForcedIn)
                .flatMap(isForcedIn -> {
                    List<GitHubUser> gitHubUserList = realm.where(GitHubUser.class).findAllAsync();
                    return Observable.just(gitHubUserList.size());
                });
    }

    private Observable<Integer> getGitHubUsersFromRetrofit() {
        return internetConnection.isInternetOn(context)
                .filter(connectionStatus -> connectionStatus)
                .switchMap(connectionStatus -> gitHubApiInterface.getGitHubUsersList())
                .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .map(gitHubUserList -> {
                    realm.executeTransactionAsync(realm -> realm.copyToRealmOrUpdate(gitHubUserList));
                    return gitHubUserList.size();
                });
    }

    public void unSubscribe() {
        realm.close();
    }
}