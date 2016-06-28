package tank.viraj.realm.dataSource;

import rx.Observable;
import rx.schedulers.Schedulers;
import tank.viraj.realm.dao.GitHubUserProfileDao;
import tank.viraj.realm.jsonModel.GitHubUserProfile;
import tank.viraj.realm.retrofit.GitHubApiInterface;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileDataSource {
    private GitHubApiInterface gitHubApiInterface;
    private GitHubUserProfileDao gitHubUserProfileDao;

    public GitHubUserProfileDataSource(GitHubApiInterface gitHubApiInterface,
                                       GitHubUserProfileDao gitHubUserProfileDao) {
        this.gitHubApiInterface = gitHubApiInterface;
        this.gitHubUserProfileDao = gitHubUserProfileDao;
    }

    public Observable<GitHubUserProfile> getGitHubUserProfile(String login, boolean isForced) {
        return Observable.concat(
                getGitHubUserProfileFromRealm(login, isForced),
                getGitHubUserProfileFromRetrofit(login))
                .takeFirst(profile -> profile != null);
    }

    private Observable<GitHubUserProfile> getGitHubUserProfileFromRealm(String login,
                                                                        boolean isForced) {
        return Observable.just(isForced)
                .filter(isForcedIn -> !isForcedIn)
                .subscribeOn(Schedulers.immediate())
                .observeOn(Schedulers.immediate())
                .map(isForcedIn -> gitHubUserProfileDao.getProfile(login));
    }

    private Observable<GitHubUserProfile> getGitHubUserProfileFromRetrofit(String login) {
        return gitHubApiInterface.getGitHubUserProfile(login)
                .map(profile -> {
                    gitHubUserProfileDao.storeOrUpdateProfile(profile);
                    return profile;
                });
    }

    public void clearRealmData() {
        gitHubUserProfileDao.clearDatabase();
    }
}