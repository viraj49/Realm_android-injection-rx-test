package tank.viraj.realm.presenter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Subscription;
import tank.viraj.realm.dataSource.GitHubUserListDataSource;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.ui.fragment.GitHubUserListFragment;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

import static tank.viraj.realm.util.StatusCodes.statusCodes;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListPresenter {
    private Realm realm;
    private Subscription subscription;
    private Subscription internetStatusSubscription;
    private InternetConnection internetConnection;
    private WeakReference<GitHubUserListFragment> weakReferenceView;
    private Subscription gitHubUserListHotSubscription;
    private GitHubUserListDataSource gitHubUserListDataSource;
    private RxSchedulerConfiguration rxSchedulerConfiguration;
    private boolean isViewLoadedAtLeastOnce;
    private boolean areWeLoadingSomething;

    public GitHubUserListPresenter(RxSchedulerConfiguration rxSchedulerConfiguration,
                                   GitHubUserListDataSource gitHubUserListDataSource,
                                   InternetConnection internetConnection) {
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.gitHubUserListDataSource = gitHubUserListDataSource;
        this.internetConnection = internetConnection;
        this.isViewLoadedAtLeastOnce = false;
        this.areWeLoadingSomething = false;
    }

    public void loadGitHubUserList(boolean isForced) {
        if (gitHubUserListHotSubscription == null || gitHubUserListHotSubscription.isUnsubscribed() || isForced) {
            /* This is not needed here, since pullToRefresh will not trigger onRefresh()
               a second time as long as we don't stop the animation, this is to demonstrate
               how it can be done */
            if (gitHubUserListHotSubscription != null && !gitHubUserListHotSubscription.isUnsubscribed()) {
                gitHubUserListHotSubscription.unsubscribe();
            }

            areWeLoadingSomething = true;

            gitHubUserListHotSubscription = gitHubUserListDataSource.getGitHubUsers(isForced)
                    .subscribe(userListSize -> {
                                if (userListSize == statusCodes.DEFAULT_RESPONSE) {
                                    if (!isViewLoadedAtLeastOnce) {
                                        weakReferenceView.get().setDataList(new ArrayList<>());
                                        waitForInternetToComeBack();
                                    }

                                    weakReferenceView.get().showSnackBar();
                                } else {
                                    isViewLoadedAtLeastOnce = true;
                                }

                                weakReferenceView.get().stopRefreshAnimation();
                                areWeLoadingSomething = false;
                            },
                            error -> {
                                if (!isViewLoadedAtLeastOnce) {
                                    weakReferenceView.get().setDataList(new ArrayList<>());
                                    waitForInternetToComeBack();
                                }

                                weakReferenceView.get().showSnackBar();
                                weakReferenceView.get().stopRefreshAnimation();
                                areWeLoadingSomething = false;
                            });
        }
    }

    public void bind(GitHubUserListFragment gitHubUserListFragment, boolean isForced) {
        weakReferenceView = new WeakReference<>(gitHubUserListFragment);
        realm = Realm.getDefaultInstance();

        if (!isViewLoadedAtLeastOnce || areWeLoadingSomething) {
            weakReferenceView.get().startRefreshAnimation();
        }

        if (subscription == null || subscription.isUnsubscribed()) {
            subscription = realm.where(GitHubUser.class).findAllAsync().asObservable()
                    .filter(RealmResults::isLoaded)
                    .filter(RealmResults::isValid)
                    .filter(realmResults -> realmResults.size() > 0)
                    .observeOn(rxSchedulerConfiguration.getMainThread())
                    .subscribe(gitHubUsers -> weakReferenceView.get().setDataList(gitHubUsers)
                            , error -> weakReferenceView.get().stopRefreshAnimation());
        }

        if (!isViewLoadedAtLeastOnce && !areWeLoadingSomething) {
            loadGitHubUserList(isForced);
        }
    }

    public void unBind() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

        realm.close();
        this.weakReferenceView = null;
    }

    private void waitForInternetToComeBack() {
        if (internetStatusSubscription == null || internetStatusSubscription.isUnsubscribed()) {
            internetStatusSubscription = internetConnection.getInternetStatusHotObservable()
                    .filter(internetConnectionStatus -> internetConnectionStatus)
                    .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                    .observeOn(rxSchedulerConfiguration.getComputationThread())
                    .subscribe(internetConnectionStatus -> {
                        weakReferenceView.get().startRefreshAnimation();
                        loadGitHubUserList(true);
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
        if (gitHubUserListHotSubscription != null && !gitHubUserListHotSubscription.isUnsubscribed()) {
            gitHubUserListHotSubscription.unsubscribe();
        }

        gitHubUserListDataSource.unSubscribe();
        stopWaitForInternetToComeBack();
    }
}