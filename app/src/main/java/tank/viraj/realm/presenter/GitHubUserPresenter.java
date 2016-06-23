package tank.viraj.realm.presenter;


import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import tank.viraj.realm.dataSource.GitHubUserListDataSource;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.ui.fragment.GitHubUserListFragment;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserPresenter {
    GitHubUserListFragment view;
    private GitHubUserListDataSource gitHubUserListDataSource;
    CompositeSubscription compositeSubscription;

    public GitHubUserPresenter(GitHubUserListDataSource gitHubUserListDataSource) {
        this.gitHubUserListDataSource = gitHubUserListDataSource;
        this.compositeSubscription = new CompositeSubscription();
    }

    public void loadGitHubUserList(boolean isForced) {
        checkCompositeSubscription();

        compositeSubscription.add(
                gitHubUserListDataSource.getGitHubUsers(isForced)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(() -> view.startRefreshAnimation())
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
    }

    public void unBind() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            view.stopRefreshAnimation();
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