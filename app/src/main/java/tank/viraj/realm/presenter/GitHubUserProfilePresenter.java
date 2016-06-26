package tank.viraj.realm.presenter;


import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import tank.viraj.realm.dataSource.GitHubUserProfileDataSource;
import tank.viraj.realm.jsonModel.GitHubUserProfile;
import tank.viraj.realm.ui.fragment.GitHubUserProfileFragment;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfilePresenter {
    private GitHubUserProfileFragment view;
    private GitHubUserProfileDataSource gitHubUserProfileDataSource;
    private CompositeSubscription compositeSubscription;

    public GitHubUserProfilePresenter(GitHubUserProfileDataSource gitHubUserProfileDataSource) {
        this.gitHubUserProfileDataSource = gitHubUserProfileDataSource;
        this.compositeSubscription = new CompositeSubscription();
    }

    public void loadGitHubUserProfile(String login, boolean isForced) {
        checkCompositeSubscription();

        compositeSubscription.add(
                gitHubUserProfileDataSource.getGitHubUserProfile(login, isForced)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
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
    }

    public void unBind() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
        this.view = null;
    }

    private void checkCompositeSubscription() {
        if (compositeSubscription == null || compositeSubscription.isUnsubscribed()) {
            compositeSubscription = new CompositeSubscription();
        }
    }
}