package tank.viraj.realm.dataSource;

import io.realm.Realm;
import rx.Observable;
import rx.functions.Func1;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

import static tank.viraj.realm.util.StatusCodes.statusCodes;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListDataSource {
    private Realm realm;
    private GitHubApiInterface gitHubApiInterface;
    private InternetConnection internetConnection;
    private RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserListDataSource(GitHubApiInterface gitHubApiInterface,
                                    InternetConnection internetConnection,
                                    RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.gitHubApiInterface = gitHubApiInterface;
        this.internetConnection = internetConnection;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.realm = Realm.getDefaultInstance();
    }

    public Observable<statusCodes> getGitHubUsers(boolean isForced) {
        return Observable.concat(
                getGitHubUsersFromRealm(isForced),
                getGitHubUsersFromRetrofit(),
                getDefaultResponse())
                .takeFirst(gitHubUserListResponse -> gitHubUserListResponse != statusCodes.GITHUB_USER_LIST_NOT_AVAILABLE);
    }

    private Observable<statusCodes> getGitHubUsersFromRealm(boolean isForced) {
        return Observable.just(isForced)
                .filter(isForcedIn -> !isForcedIn)
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .map(new Func1<Boolean, statusCodes>() {
                    @Override
                    public statusCodes call(Boolean isForcedIn) {
                        return (GitHubUserListDataSource.this.realm.where(GitHubUser.class).findAll().size() > 0) ?
                                statusCodes.GITHUB_USER_LIST_AVAILABLE :
                                statusCodes.GITHUB_USER_LIST_NOT_AVAILABLE;
                    }
                });
    }

    private Observable<statusCodes> getGitHubUsersFromRetrofit() {
        return internetConnection.isInternetOnObservable()
                .filter(connectionStatus -> connectionStatus)
                .switchMap(connectionStatus -> gitHubApiInterface.getGitHubUsersList())
                .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .map(gitHubUserList -> {
                    realm.executeTransactionAsync(realm -> realm.copyToRealmOrUpdate(gitHubUserList));
                    return gitHubUserList.size() > 0 ?
                            statusCodes.GITHUB_USER_LIST_AVAILABLE :
                            statusCodes.GITHUB_USER_LIST_NOT_AVAILABLE;
                });
    }

    private Observable<statusCodes> getDefaultResponse() {
        return Observable.just(statusCodes.DEFAULT_RESPONSE);
    }

    public void unSubscribe() {
        realm.close();
    }
}