package tank.viraj.realm.dataSource;

import rx.Observable;
import tank.viraj.realm.dao.GitHubUserProfileDao;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;

import static tank.viraj.realm.util.StatusCodes.statusCodes;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileDataSource {
    private GitHubApiInterface gitHubApiInterface;
    private InternetConnection internetConnection;
    private GitHubUserProfileDao gitHubUserProfileDao;

    public GitHubUserProfileDataSource(GitHubApiInterface gitHubApiInterface,
                                       InternetConnection internetConnection,
                                       GitHubUserProfileDao gitHubUserProfileDao) {
        this.gitHubApiInterface = gitHubApiInterface;
        this.internetConnection = internetConnection;
        this.gitHubUserProfileDao = gitHubUserProfileDao;
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
                .map(isForcedIn -> gitHubUserProfileDao.getProfileStatus(login) ?
                        statusCodes.GITHUB_USER_PROFILE_OBJECT_AVAILABLE :
                        statusCodes.GITHUB_USER_PROFILE_OBJECT_NOT_AVAILABLE);
    }

    private Observable<statusCodes> getGitHubUserProfileFromRetrofit(String login) {
        return internetConnection.isInternetOnObservable()
                .filter(connectionStatus -> connectionStatus)
                .switchMap(connectionStatus -> gitHubApiInterface.getGitHubUserProfile(login)
                        .map(gitHubUserProfile -> {
                            gitHubUserProfileDao.storeOrUpdateProfile(gitHubUserProfile);
                            return gitHubUserProfile != null ?
                                    statusCodes.GITHUB_USER_PROFILE_OBJECT_AVAILABLE :
                                    statusCodes.GITHUB_USER_PROFILE_OBJECT_NOT_AVAILABLE;
                        }));
    }

    private Observable<statusCodes> getDefaultResponse() {
        return Observable.just(statusCodes.DEFAULT_RESPONSE);
    }
}