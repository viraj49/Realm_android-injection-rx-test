package tank.viraj.realm.dataSource;

import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;
import tank.viraj.realm.dao.GitHubUserDao;
import tank.viraj.realm.jsonModel.GitHubUser;
import tank.viraj.realm.retrofit.GitHubApiInterface;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListDataSource {
    private GitHubApiInterface gitHubApiInterface;
    private GitHubUserDao gitHubUserDao;

    public GitHubUserListDataSource(GitHubApiInterface gitHubApiInterface, GitHubUserDao gitHubUserDao) {
        this.gitHubApiInterface = gitHubApiInterface;
        this.gitHubUserDao = gitHubUserDao;
    }

    public Observable<List<GitHubUser>> getGitHubUsers(boolean isForced) {
        return Observable.concat(getGitHubUsersFromRealm(isForced), getGitHubUsersFromRetrofit())
                .takeFirst(gitHubUserList -> gitHubUserList != null && gitHubUserList.size() > 0);
    }

    private Observable<List<GitHubUser>> getGitHubUsersFromRealm(boolean isForced) {
        return Observable.just(isForced)
                .filter(isForcedIn -> !isForcedIn)
                .subscribeOn(Schedulers.immediate())
                .observeOn(Schedulers.immediate())
                .map(isForcedIn -> gitHubUserDao.getGitHubUserList());
    }

    private Observable<List<GitHubUser>> getGitHubUsersFromRetrofit() {
        return gitHubApiInterface.getGitHubUsersList()
                .map(gitHubUserList -> {
                    gitHubUserDao.storeOrUpdateGitHubUserList(gitHubUserList);
                    return gitHubUserList;
                });
    }

    public void clearRealmData() {
        gitHubUserDao.clearDatabase();
    }
}