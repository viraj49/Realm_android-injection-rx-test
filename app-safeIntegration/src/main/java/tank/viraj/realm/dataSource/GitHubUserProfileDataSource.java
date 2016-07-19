package tank.viraj.realm.dataSource;

import android.content.Context;

import rx.Observable;
import rx.Subscription;
import rx.subjects.ReplaySubject;
import tank.viraj.realm.dao.GitHubUserProfileDao;
import tank.viraj.realm.jsonModel.GitHubUserProfile;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileDataSource {
    private Context context;
    private GitHubApiInterface gitHubApiInterface;
    private GitHubUserProfileDao gitHubUserProfileDao;
    private InternetConnection internetConnection;
    private ReplaySubject<GitHubUserProfile> gitHubUserProfileSubject;
    private Subscription gitHubUserProfileHotSubscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserProfileDataSource(Context context,
                                       GitHubApiInterface gitHubApiInterface,
                                       GitHubUserProfileDao gitHubUserProfileDao,
                                       InternetConnection internetConnection,
                                       RxSchedulerConfiguration rxSchedulerConfiguration,
                                       ReplaySubject<GitHubUserProfile> gitHubUserProfileSubject) {
        this.context = context;
        this.gitHubApiInterface = gitHubApiInterface;
        this.gitHubUserProfileDao = gitHubUserProfileDao;
        this.internetConnection = internetConnection;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.gitHubUserProfileSubject = gitHubUserProfileSubject;
    }

    public Observable<GitHubUserProfile> getGitHubUserListHotSubscription() {
        return gitHubUserProfileSubject.asObservable();
    }

    public void getGitHubUserProfile(String login, boolean isForced) {
        if (gitHubUserProfileHotSubscription == null || isForced) {
            /* This is not needed here, since pullToRefresh will not trigger onRefresh()
               a second time as long as we don't stop the animation, this is to demonstrate
               how it can be done */
            if (gitHubUserProfileHotSubscription != null && !gitHubUserProfileHotSubscription.isUnsubscribed()) {
                gitHubUserProfileHotSubscription.unsubscribe();
            }

            gitHubUserProfileHotSubscription = Observable.concat(
                    getGitHubUserProfileFromRealm(login, isForced),
                    getGitHubUserProfileFromRetrofit(login))
                    .first(profile -> profile != null)
                    .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                    .observeOn(rxSchedulerConfiguration.getComputationThread())
                    .subscribe(gitHubUserProfile -> gitHubUserProfileSubject.onNext(gitHubUserProfile),
                            Throwable::printStackTrace);
        }
    }

    private Observable<GitHubUserProfile> getGitHubUserProfileFromRealm(String login,
                                                                        boolean isForced) {
        return !isForced
                ? Observable.just(gitHubUserProfileDao.getProfile(login))
                : Observable.empty();
    }

    private Observable<GitHubUserProfile> getGitHubUserProfileFromRetrofit(String login) {
        return internetConnection.isInternetOn(context)
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

    public void unSubscribe() {
        if (gitHubUserProfileHotSubscription != null && !gitHubUserProfileHotSubscription.isUnsubscribed()) {
            gitHubUserProfileHotSubscription.unsubscribe();
        }
    }
}