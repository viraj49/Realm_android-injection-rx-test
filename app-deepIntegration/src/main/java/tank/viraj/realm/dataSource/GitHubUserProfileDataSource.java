package tank.viraj.realm.dataSource;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import rx.Observable;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

import static tank.viraj.realm.util.StatusCodes.statusCodes;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileDataSource {
    private Realm realm;
    private GitHubApiInterface gitHubApiInterface;
    private InternetConnection internetConnection;
    private RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserProfileDataSource(GitHubApiInterface gitHubApiInterface,
                                       InternetConnection internetConnection,
                                       RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.gitHubApiInterface = gitHubApiInterface;
        this.internetConnection = internetConnection;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.realm = Realm.getDefaultInstance();
    }

    public Observable<statusCodes> getGitHubUserProfile(String login, boolean isForced) {
        return Observable.concat(getGitHubUserProfileFromRealm(login, isForced),
                getGitHubUserProfileFromRetrofit(login),
                getDefaultResponse())
                .takeFirst(profile -> profile != statusCodes.GITHUB_USER_PROFILE_OBJECT_NOT_AVAILABLE);
    }

    private Observable<statusCodes> getGitHubUserProfileFromRealm(String login, boolean isForced) {
        return Observable.just(isForced)
                .filter(isForcedIn -> !isForcedIn)
                .map(isForcedIn -> {
                    GitHubUserProfile gitHubUserProfile = realm.where(GitHubUserProfile.class)
                            .equalTo("login", login)
                            .findFirst();
                    return gitHubUserProfile != null ?
                            statusCodes.GITHUB_USER_PROFILE_OBJECT_AVAILABLE :
                            statusCodes.GITHUB_USER_PROFILE_OBJECT_NOT_AVAILABLE;
                });
    }

    private Observable<statusCodes> getGitHubUserProfileFromRetrofit(String login) {
        return internetConnection.isInternetOnObservable()
                .filter(connectionStatus -> connectionStatus)
                .switchMap(connectionStatus -> gitHubApiInterface.getGitHubUserProfile(login)
                        .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                        .observeOn(rxSchedulerConfiguration.getMainThread())
                        .map(gitHubUserProfile -> {
                            realm.executeTransactionAsync(realm1 -> realm1.copyToRealmOrUpdate(gitHubUserProfile));
                            return gitHubUserProfile != null ?
                                    statusCodes.GITHUB_USER_PROFILE_OBJECT_AVAILABLE :
                                    statusCodes.GITHUB_USER_PROFILE_OBJECT_NOT_AVAILABLE;
                        }));
    }

    private Observable<statusCodes> getDefaultResponse() {
        return Observable.just(statusCodes.DEFAULT_RESPONSE);
    }

    public void unSubscribe() {
        realm.close();
    }
}