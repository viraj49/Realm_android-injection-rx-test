package tank.viraj.realm.presenter;


import java.lang.ref.WeakReference;

import rx.Subscription;
import tank.viraj.realm.dataSource.GitHubUserProfileDataSource;
import tank.viraj.realm.ui.fragment.GitHubUserProfileFragment;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfilePresenter {
    private WeakReference<GitHubUserProfileFragment> weakReferenceView;
    private GitHubUserProfileDataSource gitHubUserProfileDataSource;
    private RxSchedulerConfiguration rxSchedulerConfiguration;
    private Subscription subscription;
    private Subscription internetStatusSubscription;
    private InternetConnection internetConnection;
    private boolean isViewLoadedAtLeastOnce;
    private boolean areWeLoadingSomething;

    public GitHubUserProfilePresenter(GitHubUserProfileDataSource gitHubUserProfileDataSource,
                                      RxSchedulerConfiguration rxSchedulerConfiguration,
                                      InternetConnection internetConnection) {
        this.gitHubUserProfileDataSource = gitHubUserProfileDataSource;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.internetConnection = internetConnection;
        this.isViewLoadedAtLeastOnce = false;
        this.areWeLoadingSomething = false;
    }

    public void bind(GitHubUserProfileFragment gitHubUserProfileFragment,
                     String login,
                     boolean isForced) {
        this.weakReferenceView = new WeakReference<>(gitHubUserProfileFragment);

        if (!isViewLoadedAtLeastOnce || areWeLoadingSomething) {
            weakReferenceView.get().startRefreshAnimation();
        }

        loadGitHubUserProfile(login);

        if (!isViewLoadedAtLeastOnce && !areWeLoadingSomething) {
            getGitHubUserProfile(login, isForced);
        } else {
            weakReferenceView.get().loadData();
        }
    }

    public void getGitHubUserProfile(String login, boolean isForced) {
        areWeLoadingSomething = true;
        gitHubUserProfileDataSource.getGitHubUserProfile(login, isForced);
    }

    private void loadGitHubUserProfile(String login) {
        if (subscription == null || subscription.isUnsubscribed()) {
            subscription = gitHubUserProfileDataSource.getGitHubUserProfileHotSubscription()
                    .onBackpressureDrop()
                    .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                    .observeOn(rxSchedulerConfiguration.getMainThread())
                    .subscribe(gitHubUserProfile -> {
                        if (gitHubUserProfile.getName().contains("default")) {
                            if (!isViewLoadedAtLeastOnce) {
                                weakReferenceView.get().setData(gitHubUserProfile);
                                waitForInternetToComeBack(login);
                            }

                            weakReferenceView.get().showSnackBar();
                        } else {
                            weakReferenceView.get().setData(gitHubUserProfile);
                            isViewLoadedAtLeastOnce = true;
                        }

                        weakReferenceView.get().stopRefreshAnimation();
                        areWeLoadingSomething = false;
                    }, error -> {
                        weakReferenceView.get().stopRefreshAnimation();
                        areWeLoadingSomething = false;
                    });
        }
    }

    public void clearGitHubUserProfileFromRealm() {
        gitHubUserProfileDataSource.clearRealmData();
    }

    public void unBind() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        this.weakReferenceView = null;
    }

    private void waitForInternetToComeBack(String login) {
        if (internetStatusSubscription == null || internetStatusSubscription.isUnsubscribed()) {
            internetStatusSubscription = internetConnection.getInternetStatusHotObservable()
                    .filter(internetConnectionStatus -> internetConnectionStatus)
                    .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                    .observeOn(rxSchedulerConfiguration.getComputationThread())
                    .subscribe(internetConnectionStatus -> {
                        weakReferenceView.get().startRefreshAnimation();
                        loadGitHubUserProfile(login);
                        getGitHubUserProfile(login, true);
                        stopWaitForInternetToComeBack();
                    });
        }
        internetConnection.registerBroadCastReceiver();
    }

    private void stopWaitForInternetToComeBack() {
        if (internetStatusSubscription != null && !internetStatusSubscription.isUnsubscribed()) {
            internetStatusSubscription.unsubscribe();
            internetConnection.unRegisterBroadCastReceiver();
        }
    }

    public void unSubscribe() {
        gitHubUserProfileDataSource.unSubscribe();
        stopWaitForInternetToComeBack();
    }
}