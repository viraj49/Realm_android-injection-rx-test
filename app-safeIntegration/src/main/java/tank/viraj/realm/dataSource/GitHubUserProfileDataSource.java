package tank.viraj.realm.dataSource;

import android.content.Context;

import rx.Observable;
import tank.viraj.realm.dao.GitHubUserProfileDao;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileDataSource {
    private Context context;
    private GitHubApiInterface gitHubApiInterface;
    private GitHubUserProfileDao gitHubUserProfileDao;

    public GitHubUserProfileDataSource(Context context,
                                       GitHubApiInterface gitHubApiInterface,
                                       GitHubUserProfileDao gitHubUserProfileDao) {
        this.context = context;
        this.gitHubApiInterface = gitHubApiInterface;
        this.gitHubUserProfileDao = gitHubUserProfileDao;
    }

    public Observable<GitHubUserProfile> getGitHubUserProfile(String login, boolean isForced) {
        return Observable.concat(
                getGitHubUserProfileFromRealm(login, isForced),
                getGitHubUserProfileFromRetrofit(login))
                .first(profile -> profile != null);
    }

    private Observable<GitHubUserProfile> getGitHubUserProfileFromRealm(String login,
                                                                        boolean isForced) {
        return !isForced
                ? Observable.just(gitHubUserProfileDao.getProfile(login))
                : Observable.empty();
    }

    private Observable<GitHubUserProfile> getGitHubUserProfileFromRetrofit(String login) {
        return new InternetConnection().isInternetOn(context)
                .switchMap(connectionStatus -> connectionStatus ?
                        gitHubApiInterface.getGitHubUserProfile(login)
                                .map(profile -> {
                                    gitHubUserProfileDao.storeOrUpdateProfile(profile);
                                    return profile;
                                })
                        : Observable.empty());
    }

    public void clearRealmData() {
        gitHubUserProfileDao.clearDatabase();
    }
}