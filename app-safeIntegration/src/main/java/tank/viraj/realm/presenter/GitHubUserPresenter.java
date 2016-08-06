package tank.viraj.realm.presenter;


import java.lang.ref.WeakReference;
import java.util.ArrayList;

import rx.Subscription;
import tank.viraj.realm.dataSource.GitHubUserListDataSource;
import tank.viraj.realm.ui.fragment.GitHubUserListFragment;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserPresenter {
    private WeakReference<GitHubUserListFragment> weakReferenceView;
    private GitHubUserListDataSource gitHubUserListDataSource;
    private Subscription subscription;
    private Subscription internetStatusSubscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;
    private InternetConnection internetConnection;
    private boolean isViewLoadedAtLeastOnce;
    private boolean areWeLoadingSomething;

    public GitHubUserPresenter(GitHubUserListDataSource gitHubUserListDataSource,
                               RxSchedulerConfiguration rxSchedulerConfiguration,
                               InternetConnection internetConnection) {
        this.gitHubUserListDataSource = gitHubUserListDataSource;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.internetConnection = internetConnection;
        this.isViewLoadedAtLeastOnce = false;
        this.areWeLoadingSomething = false;
    }

    public void clearGitHubUserListFromRealm() {
        gitHubUserListDataSource.clearRealmData();
    }

    public void bind(GitHubUserListFragment gitHubUserListFragment, boolean isForced) {
        this.weakReferenceView = new WeakReference<>(gitHubUserListFragment);

        if (!isViewLoadedAtLeastOnce || areWeLoadingSomething) {
            weakReferenceView.get().startRefreshAnimation();
        }

        loadGitHubUserList();

        if (!isViewLoadedAtLeastOnce && !areWeLoadingSomething) {
            getGitHubUsers(isForced);
        }
    }

    public void getGitHubUsers(boolean isForced) {
        areWeLoadingSomething = true;
        gitHubUserListDataSource.getGitHubUsers(isForced);
    }

    private void loadGitHubUserList() {
        if (subscription == null || subscription.isUnsubscribed()) {
            subscription = gitHubUserListDataSource.getGitHubUserListHotSubscription()
                    .onBackpressureDrop()
                    .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                    .observeOn(rxSchedulerConfiguration.getMainThread())
                    .subscribe(gitHubUserList -> {
                        if (gitHubUserList.get(0).getId() == -1) {
                            if (!isViewLoadedAtLeastOnce) {
                                weakReferenceView.get().setDataList(new ArrayList<>());
                                waitForInternetToComeBack();
                            }

                            weakReferenceView.get().showSnackBar();
                        } else {
                            weakReferenceView.get().setDataList(gitHubUserList);
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

    public void unBind() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    private void waitForInternetToComeBack() {
        if (internetStatusSubscription == null || internetStatusSubscription.isUnsubscribed()) {
            internetStatusSubscription = internetConnection.getInternetStatusHotObservable()
                    .filter(internetConnectionStatus -> internetConnectionStatus)
                    .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                    .observeOn(rxSchedulerConfiguration.getComputationThread())
                    .subscribe(internetConnectionStatus -> {
                        weakReferenceView.get().startRefreshAnimation();
                        loadGitHubUserList();
                        getGitHubUsers(true);
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

    public void unSubscribeHotSubscription() {
        gitHubUserListDataSource.unSubscribeHotSubscription();
        stopWaitForInternetToComeBack();
    }
}