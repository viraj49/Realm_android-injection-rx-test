package tank.viraj.realm.presenter;


import rx.Subscription;
import tank.viraj.realm.dataSource.GitHubUserProfileDataSource;
import tank.viraj.realm.ui.fragment.GitHubUserProfileFragment;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfilePresenter {
    private GitHubUserProfileFragment view;
    private GitHubUserProfileDataSource gitHubUserProfileDataSource;
    private RxSchedulerConfiguration rxSchedulerConfiguration;
    private Subscription subscription;

    public GitHubUserProfilePresenter(GitHubUserProfileDataSource gitHubUserProfileDataSource,
                                      RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.gitHubUserProfileDataSource = gitHubUserProfileDataSource;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
    }

    public void loadGitHubUserProfile(String login, boolean isForced) {
        gitHubUserProfileDataSource.getGitHubUserProfile(login, isForced);
    }

    public void clearGitHubUserProfileFromRealm() {
        gitHubUserProfileDataSource.clearRealmData();
    }

    public void bind(GitHubUserProfileFragment gitHubUserProfileFragment,
                     String login,
                     boolean isForced) {
        this.view = gitHubUserProfileFragment;
        view.startRefreshAnimation();

        if (subscription == null || subscription.isUnsubscribed()) {
            subscription = gitHubUserProfileDataSource.getGitHubUserListHotSubscription()
                    .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                    .observeOn(rxSchedulerConfiguration.getMainThread())
                    .subscribe(gitHubUserProfile -> {
                        view.setData(gitHubUserProfile);
                        view.stopRefreshAnimation();
                    }, error -> view.stopRefreshAnimation());
        }

        loadGitHubUserProfile(login, isForced);
    }

    public void unBind() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        this.view = null;
    }

    public void unSubscribe() {
        gitHubUserProfileDataSource.unSubscribe();
    }
}