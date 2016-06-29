package tank.viraj.realm.presenter;


import rx.Observer;
import rx.subscriptions.CompositeSubscription;
import tank.viraj.realm.dataSource.GitHubUserProfileDataSource;
import tank.viraj.realm.jsonModel.GitHubUserProfile;
import tank.viraj.realm.ui.fragment.GitHubUserProfileFragment;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfilePresenter {
    GitHubUserProfileFragment view;
    GitHubUserProfileDataSource gitHubUserProfileDataSource;
    CompositeSubscription compositeSubscription;
    RxSchedulerConfiguration rxSchedulerConfiguration;

    public GitHubUserProfilePresenter(GitHubUserProfileDataSource gitHubUserProfileDataSource) {
        this.gitHubUserProfileDataSource = gitHubUserProfileDataSource;
        this.rxSchedulerConfiguration = new RxSchedulerConfiguration();
    }

    public void loadGitHubUserProfile(String login, boolean isForced) {
        compositeSubscription.add(
                gitHubUserProfileDataSource.getGitHubUserProfile(login, isForced)
                        .subscribeOn(rxSchedulerConfiguration.getSubscribeOn())
                        .observeOn(rxSchedulerConfiguration.getObserveOn())
                        .doOnSubscribe(() -> view.startRefreshAnimation())
                        .subscribe(new Observer<GitHubUserProfile>() {
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
                            public void onNext(GitHubUserProfile gitHubUserProfile) {
                                view.setData(gitHubUserProfile);
                            }
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