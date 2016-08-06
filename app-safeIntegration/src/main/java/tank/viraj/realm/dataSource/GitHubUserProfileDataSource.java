package tank.viraj.realm.dataSource;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import tank.viraj.realm.dao.GitHubUserProfileDao;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileDataSource {
    private GitHubApiInterface gitHubApiInterface;
    private GitHubUserProfileDao gitHubUserProfileDao;
    private InternetConnection internetConnection;
    private PublishSubject<GitHubUserProfile> gitHubUserProfileSubject;
    private Subscription gitHubUserProfileHotSubscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserProfileDataSource(GitHubApiInterface gitHubApiInterface,
                                       GitHubUserProfileDao gitHubUserProfileDao,
                                       InternetConnection internetConnection,
                                       RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.gitHubApiInterface = gitHubApiInterface;
        this.gitHubUserProfileDao = gitHubUserProfileDao;
        this.internetConnection = internetConnection;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.gitHubUserProfileSubject = PublishSubject.create();
    }

    public Observable<GitHubUserProfile> getGitHubUserProfileHotSubscription() {
        return gitHubUserProfileSubject.asObservable().serialize();
    }

    public void getGitHubUserProfile(String login, boolean isForced) {
        if (gitHubUserProfileHotSubscription == null || gitHubUserProfileHotSubscription.isUnsubscribed() || isForced) {
            /* This is not needed here, since pullToRefresh will not trigger onRefresh()
               a second time as long as we don't stop the animation, this is to demonstrate
               how it can be done */
            if (gitHubUserProfileHotSubscription != null && !gitHubUserProfileHotSubscription.isUnsubscribed()) {
                gitHubUserProfileHotSubscription.unsubscribe();
            }

            gitHubUserProfileHotSubscription = Observable.concat(
                    getGitHubUserProfileFromRealm(login, isForced),
                    getGitHubUserProfileFromRetrofit(login),
                    getDefaultResponse())
                    .takeFirst(profile -> profile != null)
                    .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                    .observeOn(rxSchedulerConfiguration.getComputationThread())
                    .subscribe(gitHubUserProfile -> {
                                gitHubUserProfileSubject.onNext(gitHubUserProfile);
                            },
                            error -> {
                                gitHubUserProfileSubject.onNext(new GitHubUserProfile("default", "default", "default"));
                            });
        }
    }

    private Observable<GitHubUserProfile> getGitHubUserProfileFromRealm(String login,
                                                                        boolean isForced) {
        return !isForced
                ? Observable.just(gitHubUserProfileDao.getProfile(login))
                : Observable.empty();
    }

    private Observable<GitHubUserProfile> getGitHubUserProfileFromRetrofit(String login) {
        return internetConnection.isInternetOnObservable()
                .switchMap(connectionStatus -> connectionStatus ?
                        gitHubApiInterface.getGitHubUserProfile(login)
                                .map(profile -> {
                                    gitHubUserProfileDao.storeOrUpdateProfile(profile);
                                    return profile;
                                })
                        : Observable.empty()
                );
    }

    private Observable<GitHubUserProfile> getDefaultResponse() {
        return Observable.just(new GitHubUserProfile("default", "default", "default"));
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