package tank.viraj.realm.presenter;


import rx.subscriptions.CompositeSubscription;
import tank.viraj.realm.dataSource.GitHubUserProfileDataSource;
import tank.viraj.realm.ui.fragment.GitHubUserProfileFragment;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfilePresenter {
    private GitHubUserProfileFragment view;
    private GitHubUserProfileDataSource gitHubUserProfileDataSource;
    private CompositeSubscription compositeSubscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserProfilePresenter(GitHubUserProfileDataSource gitHubUserProfileDataSource,
                                      RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.gitHubUserProfileDataSource = gitHubUserProfileDataSource;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
    }

    public void loadGitHubUserProfile(String login, boolean isForced) {
        compositeSubscription.add(
                gitHubUserProfileDataSource.getGitHubUserProfile(login, isForced)
                        .doOnSubscribe(() -> view.startRefreshAnimation())
                        .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                        .observeOn(rxSchedulerConfiguration.getMainThread())
                        .subscribe(gitHubUserProfile -> {
                            view.setData(gitHubUserProfile);
                            view.stopRefreshAnimation();
                        }, throwable -> {
                            view.stopRefreshAnimation();
                        })
        );
    }

    public void clearGitHubUserProfileFromRealm() {
        gitHubUserProfileDataSource.clearRealmData();
    }

    public void bind(GitHubUserProfileFragment gitHubUserProfileFragment) {
        this.view = gitHubUserProfileFragment;
        this.compositeSubscription = new CompositeSubscription();
    }

    public void unBind() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }

        view.stopRefreshAnimation();
        this.view = null;
    }
}