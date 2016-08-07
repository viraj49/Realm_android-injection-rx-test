package tank.viraj.realm.presenter;


import java.lang.ref.WeakReference;

import io.realm.Realm;
import rx.Subscription;
import tank.viraj.realm.dataSource.GitHubUserProfileDataSource;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.ui.fragment.GitHubUserProfileFragment;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

import static tank.viraj.realm.util.StatusCodes.statusCodes;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfilePresenter {
    private Realm realm;
    private WeakReference<GitHubUserProfileFragment> weakReferenceView;
    private Subscription gitHubUserProfileViewSubscription;
    private Subscription gitHubUserProfileDataSubscription;
    private Subscription internetStatusSubscription;
    private InternetConnection internetConnection;
    private RxSchedulerConfiguration rxSchedulerConfiguration;
    private GitHubUserProfileDataSource gitHubUserProfileDataSource;
    private boolean isViewLoadedAtLeastOnce;
    private boolean areWeLoadingSomething;

    public GitHubUserProfilePresenter(RxSchedulerConfiguration rxSchedulerConfiguration,
                                      GitHubUserProfileDataSource gitHubUserProfileDataSource,
                                      InternetConnection internetConnection) {
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.gitHubUserProfileDataSource = gitHubUserProfileDataSource;
        this.isViewLoadedAtLeastOnce = false;
        this.areWeLoadingSomething = false;
        this.internetConnection = internetConnection;
    }

    public void loadGitHubUserProfile(String login, boolean isForced) {
        if (gitHubUserProfileDataSubscription == null || gitHubUserProfileDataSubscription.isUnsubscribed() || isForced) {
            /* This is not needed here, since pullToRefresh will not trigger onRefresh()
               a second time as long as we don't stop the animation, this is to demonstrate
               how it can be done */
            if (gitHubUserProfileDataSubscription != null && !gitHubUserProfileDataSubscription.isUnsubscribed()) {
                gitHubUserProfileDataSubscription.unsubscribe();
            }

            areWeLoadingSomething = true;

            gitHubUserProfileDataSubscription = gitHubUserProfileDataSource.getGitHubUserProfile(login, isForced)
                    .subscribe(gitHubUserProfileStatus -> {
                        if (gitHubUserProfileStatus == statusCodes.DEFAULT_RESPONSE) {
                            if (!isViewLoadedAtLeastOnce) {
                                weakReferenceView.get().setData(new GitHubUserProfile("default", "default", "default"));
                                waitForInternetToComeBack(login);
                            }

                            weakReferenceView.get().showSnackBar();
                        } else {
                            isViewLoadedAtLeastOnce = true;
                        }

                        weakReferenceView.get().stopRefreshAnimation();
                        areWeLoadingSomething = false;
                    }, error -> {
                        if (!isViewLoadedAtLeastOnce) {
                            weakReferenceView.get().setData(new GitHubUserProfile("default", "default", "default"));
                            waitForInternetToComeBack(login);
                        }

                        weakReferenceView.get().showSnackBar();
                        weakReferenceView.get().stopRefreshAnimation();
                        areWeLoadingSomething = false;
                    });
        }
    }

    public void editUserId(String login, int updatedId) {
        GitHubUser gitHubUser = realm.where(GitHubUser.class)
                .equalTo("login", login)
                .findFirst();

        if (gitHubUser != null) {
            realm.executeTransaction(realm1 -> gitHubUser.setId(updatedId));
        }
    }

    public void editUserName(String login, String updatedNameText) {
        GitHubUserProfile gitHubUserProfile = realm.where(GitHubUserProfile.class)
                .equalTo("login", login)
                .findFirst();

        if (gitHubUserProfile != null) {
            realm.executeTransaction(realm1 -> gitHubUserProfile.setName(updatedNameText));
        }
    }

    public void bind(GitHubUserProfileFragment gitHubUserProfileFragment, String login, boolean isForce) {
        weakReferenceView = new WeakReference<>(gitHubUserProfileFragment);
        realm = Realm.getDefaultInstance();

        if (!isViewLoadedAtLeastOnce || areWeLoadingSomething) {
            weakReferenceView.get().startRefreshAnimation();
        }

        if (gitHubUserProfileViewSubscription == null || gitHubUserProfileViewSubscription.isUnsubscribed()) {
            gitHubUserProfileViewSubscription = realm.where(GitHubUserProfile.class)
                    .equalTo("login", login)
                    .findFirstAsync()
                    .asObservable()
                    .cast(GitHubUserProfile.class)
                    .filter(realmObject -> realmObject.isLoaded())
                    .filter(realmObject -> realmObject.isValid())
                    .subscribeOn(rxSchedulerConfiguration.getMainThread())
                    .observeOn(rxSchedulerConfiguration.getMainThread())
                    .subscribe(gitHubUserProfile -> weakReferenceView.get().setData(gitHubUserProfile),
                            error -> weakReferenceView.get().stopRefreshAnimation());
        }

        if (!isViewLoadedAtLeastOnce && !areWeLoadingSomething) {
            loadGitHubUserProfile(login, isForce);
        } else {
            weakReferenceView.get().loadData();
        }
    }

    public void unBind() {
        if (gitHubUserProfileViewSubscription != null && !gitHubUserProfileViewSubscription.isUnsubscribed()) {
            gitHubUserProfileViewSubscription.unsubscribe();
        }

        realm.removeAllChangeListeners();
        realm.close();
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
                        loadGitHubUserProfile(login, true);
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
        if (gitHubUserProfileDataSubscription != null && !gitHubUserProfileDataSubscription.isUnsubscribed()) {
            gitHubUserProfileDataSubscription.unsubscribe();
        }

        gitHubUserProfileDataSource.unSubscribe();
    }
}