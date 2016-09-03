package tank.viraj.realm.dataSource;

import rx.Observable;
import tank.viraj.realm.dao.GitHubUserDao;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;

import static tank.viraj.realm.util.StatusCodes.statusCodes;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListDataSource {
    private GitHubApiInterface gitHubApiInterface;
    private InternetConnection internetConnection;
    private GitHubUserDao gitHubUserDao;

    public GitHubUserListDataSource(GitHubApiInterface gitHubApiInterface,
                                    InternetConnection internetConnection,
                                    GitHubUserDao gitHubUserDao) {
        this.gitHubUserDao = gitHubUserDao;
        this.gitHubApiInterface = gitHubApiInterface;
        this.internetConnection = internetConnection;
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
                .map(isForcedIn -> (gitHubUserDao.getGitHubUserListStatus()) ?
                        statusCodes.GITHUB_USER_LIST_AVAILABLE :
                        statusCodes.GITHUB_USER_LIST_NOT_AVAILABLE);
    }

    private Observable<statusCodes> getGitHubUsersFromRetrofit() {
        return internetConnection.isInternetOnObservable()
                .filter(connectionStatus -> connectionStatus)
                .switchMap(connectionStatus -> gitHubApiInterface.getGitHubUsersList())
                .map(gitHubUserList -> {
                    gitHubUserDao.storeOrUpdateGitHubUserList(gitHubUserList);
                    return gitHubUserList.size() > 0 ?
                            statusCodes.GITHUB_USER_LIST_AVAILABLE :
                            statusCodes.GITHUB_USER_LIST_NOT_AVAILABLE;
                });
    }

    private Observable<statusCodes> getDefaultResponse() {
        return Observable.just(statusCodes.DEFAULT_RESPONSE);
    }
}