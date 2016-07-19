package tank.viraj.realm.presenter;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Subscription;
import tank.viraj.realm.dataSource.GitHubUserListDataSource;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.ui.fragment.GitHubUserListFragment;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListPresenter {
    private GitHubUserListFragment view;
    private Subscription subscription;
    private Subscription gitHubUserListHotSubscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;
    private Realm realm;
    private GitHubUserListDataSource gitHubUserListDataSource;

    public GitHubUserListPresenter(RxSchedulerConfiguration rxSchedulerConfiguration,
                                   GitHubUserListDataSource gitHubUserListDataSource) {
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.gitHubUserListDataSource = gitHubUserListDataSource;
    }

    public void loadGitHubUserList(boolean isForced) {
        if (gitHubUserListHotSubscription == null || isForced) {
            /* This is not needed here, since pullToRefresh will not trigger onRefresh()
               a second time as long as we don't stop the animation, this is to demonstrate
               how it can be done */
            if (gitHubUserListHotSubscription != null && !gitHubUserListHotSubscription.isUnsubscribed()) {
                gitHubUserListHotSubscription.unsubscribe();
            }

            gitHubUserListHotSubscription = gitHubUserListDataSource
                    .getGitHubUsers(isForced)
                    .subscribe(integer -> {},
                            error -> view.stopRefreshAnimation());
        }
    }

    public void bind(GitHubUserListFragment gitHubUserListFragment, boolean isForced) {
        this.view = gitHubUserListFragment;
        view.startRefreshAnimation();
        realm = Realm.getDefaultInstance();

        if (subscription == null || subscription.isUnsubscribed()) {
            subscription = realm.where(GitHubUser.class).findAllAsync().asObservable()
                    .filter(RealmResults::isLoaded)
                    .filter(RealmResults::isValid)
                    .observeOn(rxSchedulerConfiguration.getMainThread())
                    .subscribe(gitHubUsers -> {
                        view.setDataList(gitHubUsers);
                        view.stopRefreshAnimation();
                    }, error -> {
                        view.stopRefreshAnimation();
                    });
        }

        loadGitHubUserList(isForced);
    }

    public void unBind() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

        realm.close();
        this.view = null;
    }

    public void unSubscribe() {
        if (gitHubUserListHotSubscription != null && !gitHubUserListHotSubscription.isUnsubscribed()) {
            gitHubUserListHotSubscription.unsubscribe();
        }

        gitHubUserListDataSource.unSubscribe();
    }
}