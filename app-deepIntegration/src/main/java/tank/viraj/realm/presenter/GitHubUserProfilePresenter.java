package tank.viraj.realm.presenter;


import io.realm.Realm;
import rx.Subscription;
import tank.viraj.realm.dataSource.GitHubUserProfileDataSource;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.ui.fragment.GitHubUserProfileFragment;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfilePresenter {
    private GitHubUserProfileFragment view;
    private Subscription subscription;
    private Subscription gitHubUserProfileHotSubscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;
    private Realm realm;
    private GitHubUserProfileDataSource gitHubUserProfileDataSource;

    public GitHubUserProfilePresenter(RxSchedulerConfiguration rxSchedulerConfiguration,
                                      GitHubUserProfileDataSource gitHubUserProfileDataSource) {
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
        this.gitHubUserProfileDataSource = gitHubUserProfileDataSource;
    }

    public void loadGitHubUserProfile(String login, boolean isForced) {
        if (gitHubUserProfileHotSubscription == null || isForced) {
            /* This is not needed here, since pullToRefresh will not trigger onRefresh()
               a second time as long as we don't stop the animation, this is to demonstrate
               how it can be done */
            if (gitHubUserProfileHotSubscription != null && !gitHubUserProfileHotSubscription.isUnsubscribed()) {
                gitHubUserProfileHotSubscription.unsubscribe();
            }

            gitHubUserProfileHotSubscription = gitHubUserProfileDataSource
                    .GetGitHubUserProfile(login, isForced)
                    .subscribe(integer -> {},
                            error -> view.stopRefreshAnimation());
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
        this.view = gitHubUserProfileFragment;
        view.startRefreshAnimation();
        realm = Realm.getDefaultInstance();

        subscription = realm.where(GitHubUserProfile.class)
                .equalTo("login", login)
                .findFirstAsync()
                .asObservable()
                .cast(GitHubUserProfile.class)
                .filter(realmObject -> realmObject.isLoaded())
                .filter(realmObject -> realmObject.isValid())
                .subscribeOn(rxSchedulerConfiguration.getMainThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .subscribe(gitHubUserProfile -> {
                    view.setData(gitHubUserProfile);
                    view.stopRefreshAnimation();
                }, error -> {
                    view.stopRefreshAnimation();
                });

        loadGitHubUserProfile(login, isForce);
    }

    public void unBind() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

        realm.close();
        this.view = null;
    }

    public void unSubscribe() {
        if (gitHubUserProfileHotSubscription != null && !gitHubUserProfileHotSubscription.isUnsubscribed()) {
            gitHubUserProfileHotSubscription.unsubscribe();
        }

        gitHubUserProfileDataSource.unSubscribe();
    }
}