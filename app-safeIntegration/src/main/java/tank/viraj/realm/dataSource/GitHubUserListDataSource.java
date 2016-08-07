package tank.viraj.realm.dataSource;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import tank.viraj.realm.dao.GitHubUserDao;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListDataSource {
    private GitHubApiInterface gitHubApiInterface;
    private GitHubUserDao gitHubUserDao;
    private InternetConnection internetConnection;
    private PublishSubject<List<GitHubUser>> gitHubUserListSubject;
    private Subscription gitHubUserListDataSubscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserListDataSource(GitHubApiInterface gitHubApiInterface,
                                    GitHubUserDao gitHubUserDao, InternetConnection internetConnection,
                                    RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.gitHubApiInterface = gitHubApiInterface;
        this.gitHubUserDao = gitHubUserDao;
        this.internetConnection = internetConnection;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.gitHubUserListSubject = PublishSubject.create();
    }

    public Observable<List<GitHubUser>> getGitHubUserListDataSubscription() {
        return gitHubUserListSubject.asObservable().serialize();
    }

    public void getGitHubUsers(boolean isForced) {
        if (gitHubUserListDataSubscription == null || gitHubUserListDataSubscription.isUnsubscribed() || isForced) {
            /* This is not needed here, since pullToRefresh will not trigger onRefresh()
               a second time as long as we don't stop the animation, this is to demonstrate
               how it can be done */
            if (gitHubUserListDataSubscription != null && !gitHubUserListDataSubscription.isUnsubscribed()) {
                gitHubUserListDataSubscription.unsubscribe();
            }

            gitHubUserListDataSubscription = Observable.concat(
                    getGitHubUsersFromRealm(isForced),
                    getGitHubUsersFromRetrofit(),
                    getDefaultResponse())
                    .takeFirst(gitHubUserList -> gitHubUserList != null && gitHubUserList.size() > 0)
                    .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                    .observeOn(rxSchedulerConfiguration.getMainThread())
                    .subscribe(gitHubUserList -> gitHubUserListSubject.onNext(gitHubUserList),
                            error -> gitHubUserListSubject.onNext(getDefaultGitHubUserList())
                    );
        }
    }

    private Observable<List<GitHubUser>> getGitHubUsersFromRealm(boolean isForced) {
        return !isForced ?
                Observable.just(gitHubUserDao.getGitHubUserList()) :
                Observable.empty();
    }

    private Observable<List<GitHubUser>> getGitHubUsersFromRetrofit() {
        return internetConnection.isInternetOnObservable()
                .switchMap(connectionStatus -> connectionStatus ?
                        gitHubApiInterface.getGitHubUsersList()
                                .map(gitHubUserList -> {
                                    gitHubUserDao.storeOrUpdateGitHubUserList(gitHubUserList);
                                    return gitHubUserList;
                                })
                        : Observable.empty());
    }

    private Observable<List<GitHubUser>> getDefaultResponse() {
        return Observable.just(getDefaultGitHubUserList());
    }

    private List<GitHubUser> getDefaultGitHubUserList() {
        GitHubUser gitHubUser = new GitHubUser(-1, "default", "default");
        List<GitHubUser> list = new ArrayList<>();
        list.add(gitHubUser);
        return list;
    }

    public void clearRealmData() {
        gitHubUserDao.clearDatabase();
    }

    public void unSubscribeHotSubscription() {
        if (gitHubUserListDataSubscription != null && !gitHubUserListDataSubscription.isUnsubscribed()) {
            gitHubUserListDataSubscription.unsubscribe();
        }
    }
}