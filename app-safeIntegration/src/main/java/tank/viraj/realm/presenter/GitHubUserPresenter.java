package tank.viraj.realm.presenter;


import rx.Subscription;
import tank.viraj.realm.dataSource.GitHubUserListDataSource;
import tank.viraj.realm.ui.fragment.GitHubUserListFragment;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserPresenter {
    private GitHubUserListFragment view;
    private GitHubUserListDataSource gitHubUserListDataSource;
    private Subscription subscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserPresenter(GitHubUserListDataSource gitHubUserListDataSource,
                               RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.gitHubUserListDataSource = gitHubUserListDataSource;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
    }

    public void loadGitHubUserList(boolean isForced) {
        gitHubUserListDataSource.getGitHubUsers(isForced);
    }

    public void clearGitHubUserListFromRealm() {
        gitHubUserListDataSource.clearRealmData();
    }

    public void bind(GitHubUserListFragment gitHubUserListFragment, boolean isForced) {
        this.view = gitHubUserListFragment;
        view.startRefreshAnimation();

        if (subscription == null || subscription.isUnsubscribed()) {
            subscription = gitHubUserListDataSource.getGitHubUserListHotSubscription()
                    .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                    .observeOn(rxSchedulerConfiguration.getMainThread())
                    .subscribe(gitHubUserList -> {
                        view.setDataList(gitHubUserList);
                        view.stopRefreshAnimation();
                    }, error -> view.stopRefreshAnimation());
        }
        loadGitHubUserList(isForced);
    }

    public void unBind() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        this.view = null;
    }

    public void unSubscribe() {
        gitHubUserListDataSource.unSubscribeHotSubscription();
    }
}