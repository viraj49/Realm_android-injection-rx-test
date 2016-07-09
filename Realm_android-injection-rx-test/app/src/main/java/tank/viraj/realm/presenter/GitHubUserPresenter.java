package tank.viraj.realm.presenter;


import java.util.List;

import rx.Observer;
import rx.subscriptions.CompositeSubscription;
import tank.viraj.realm.dataSource.GitHubUserListDataSource;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.ui.fragment.GitHubUserListFragment;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserPresenter {
    private GitHubUserListFragment view;
    private GitHubUserListDataSource gitHubUserListDataSource;
    private CompositeSubscription compositeSubscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserPresenter(GitHubUserListDataSource gitHubUserListDataSource) {
        this.gitHubUserListDataSource = gitHubUserListDataSource;
        this.rxSchedulerConfiguration = new RxSchedulerConfiguration();
    }

    public void loadGitHubUserList(boolean isForced) {
        compositeSubscription.add(
                gitHubUserListDataSource.getGitHubUsers(isForced)
                        .doOnSubscribe(() -> view.startRefreshAnimation())
                        .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                        .observeOn(rxSchedulerConfiguration.getMainThread())
                        .subscribe(new Observer<List<GitHubUser>>() {
                            @Override
                            public void onCompleted() {
                                view.stopRefreshAnimation();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                view.stopRefreshAnimation();
                            }

                            @Override
                            public void onNext(List<GitHubUser> gitHubUserList) {
                                view.setDataList(gitHubUserList);
                            }
                        })
        );
    }

    public void clearGitHubUserListFromRealm() {
        gitHubUserListDataSource.clearRealmData();
    }

    public void bind(GitHubUserListFragment gitHubUserListFragment) {
        this.view = gitHubUserListFragment;
        this.compositeSubscription = new CompositeSubscription();
    }

    public void unBind() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }

        view.stopRefreshAnimation();
        this.view = null;
    }
}