package tank.viraj.realm.dataSource;

import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;
import tank.viraj.realm.dao.GitHubUserDao;
import tank.viraj.realm.jsonModel.GitHubUser;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListDataSource {
    private GitHubApiInterface gitHubApiInterface;
    private GitHubUserDao gitHubUserDao;
    private RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserListDataSource(GitHubApiInterface gitHubApiInterface,
                                    GitHubUserDao gitHubUserDao) {
        this.gitHubApiInterface = gitHubApiInterface;
        this.gitHubUserDao = gitHubUserDao;
        this.rxSchedulerConfiguration = new RxSchedulerConfiguration(Schedulers.computation(),
                Schedulers.computation());
    }

    public Observable<List<GitHubUser>> getGitHubUsers(boolean isForced) {
        return Observable.concat(getGitHubUsersFromRealm(isForced), getGitHubUsersFromRetrofit())
                .takeFirst(gitHubUserList -> gitHubUserList != null && gitHubUserList.size() > 0);
    }

    private Observable<List<GitHubUser>> getGitHubUsersFromRealm(boolean isForced) {
        return Observable.just(isForced)
                .filter(isForcedIn -> !isForcedIn)
                .subscribeOn(rxSchedulerConfiguration.getSubscribeOn())
                .observeOn(rxSchedulerConfiguration.getObserveOn())
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