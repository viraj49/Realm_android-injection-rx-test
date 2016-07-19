package tank.viraj.realm.dataSource;

import android.content.Context;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subjects.ReplaySubject;
import tank.viraj.realm.dao.GitHubUserDao;
import tank.viraj.realm.jsonModel.GitHubUser;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListDataSource {
    private Context context;
    private GitHubApiInterface gitHubApiInterface;
    private GitHubUserDao gitHubUserDao;
    private InternetConnection internetConnection;
    private ReplaySubject<List<GitHubUser>> gitHubUserListSubject;
    private Subscription gitHubUserListHotSubscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserListDataSource(Context context, GitHubApiInterface gitHubApiInterface,
                                    GitHubUserDao gitHubUserDao, InternetConnection internetConnection,
                                    RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.context = context;
        this.gitHubApiInterface = gitHubApiInterface;
        this.gitHubUserDao = gitHubUserDao;
        this.internetConnection = internetConnection;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.gitHubUserListSubject = ReplaySubject.create();
    }

    public Observable<List<GitHubUser>> getGitHubUserListHotSubscription() {
        return gitHubUserListSubject.asObservable();
    }

    public void getGitHubUsers(boolean isForced) {
        if (gitHubUserListHotSubscription == null || isForced) {
            /* This is not needed here, since pullToRefresh will not trigger onRefresh()
               a second time as long as we don't stop the animation, this is to demonstrate
               how it can be done */
            if (gitHubUserListHotSubscription != null && !gitHubUserListHotSubscription.isUnsubscribed()) {
                gitHubUserListHotSubscription.unsubscribe();
            }

            gitHubUserListHotSubscription = Observable.concat(
                    getGitHubUsersFromRealm(isForced),
                    getGitHubUsersFromRetrofit())
                    .takeFirst(gitHubUserList -> gitHubUserList != null && gitHubUserList.size() > 0)
                    .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                    .observeOn(rxSchedulerConfiguration.getComputationThread())
                    .subscribe(gitHubUserList -> gitHubUserListSubject.onNext(gitHubUserList),
                            Throwable::printStackTrace);
        }
    }

    private Observable<List<GitHubUser>> getGitHubUsersFromRealm(boolean isForced) {
        return !isForced ?
                Observable.just(gitHubUserDao.getGitHubUserList()) :
                Observable.empty();
    }

    private Observable<List<GitHubUser>> getGitHubUsersFromRetrofit() {
        return internetConnection.isInternetOn(context)
                .switchMap(connectionStatus -> connectionStatus ? gitHubApiInterface.getGitHubUsersList()
                        .map(gitHubUserList -> {
                            gitHubUserDao.storeOrUpdateGitHubUserList(gitHubUserList);
                            return gitHubUserList;
                        }) : Observable.empty());
    }

    public void clearRealmData() {
        gitHubUserDao.clearDatabase();
    }

    public void unSubscribeHotSubscription() {
        if (gitHubUserListHotSubscription != null && !gitHubUserListHotSubscription.isUnsubscribed()) {
            gitHubUserListHotSubscription.unsubscribe();
        }
    }
}