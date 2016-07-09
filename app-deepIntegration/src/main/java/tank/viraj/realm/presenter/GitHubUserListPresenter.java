package tank.viraj.realm.presenter;


import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.ui.fragment.GitHubUserListFragment;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListPresenter {
    private GitHubUserListFragment view;
    private CompositeSubscription compositeSubscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;
    private GitHubApiInterface gitHubApiInterface;
    private Realm realm;

    public GitHubUserListPresenter(GitHubApiInterface gitHubApiInterface,
                                   RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.gitHubApiInterface = gitHubApiInterface;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
    }

    public void loadGitHubUserList(boolean isForced) {
        compositeSubscription.add(Observable
                .concat(getGitHubUsersFromRealm(isForced), getGitHubUsersFromRetrofit())
                .first(gitHubUserListSize -> gitHubUserListSize > 0)
                .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .subscribe(aBoolean -> {
                    view.stopRefreshAnimation();
                }, throwable -> {
                    view.stopRefreshAnimation();
                }));
    }

    private void setDataList() {
        compositeSubscription.add(realm.where(GitHubUser.class).findAllAsync().asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid)
                .doOnSubscribe(() -> view.startRefreshAnimation())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .subscribe(gitHubUsers -> {
                    view.setDataList(gitHubUsers);
                    view.stopRefreshAnimation();
                }, throwable -> {
                    view.stopRefreshAnimation();
                }));
    }

    private Observable<Integer> getGitHubUsersFromRealm(boolean isForced) {
        return Observable.just(isForced)
                .filter(isForcedIn -> !isForcedIn)
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .flatMap(isForcedIn -> {
                    List<GitHubUser> gitHubUserList = realm
                            .where(GitHubUser.class).findAllAsync();
                    return Observable.just(gitHubUserList.size());
                });
    }

    private Observable<Integer> getGitHubUsersFromRetrofit() {
        return new InternetConnection().isInternetOn(view.getActivity())
                .filter(connectionStatus -> connectionStatus)
                .switchMap(connectionStatus -> gitHubApiInterface.getGitHubUsersList())
                .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .map(gitHubUserList -> {
                    realm.executeTransactionAsync(realm -> realm.copyToRealmOrUpdate(gitHubUserList));
                    return gitHubUserList.size();
                });
    }

    public void bind(GitHubUserListFragment gitHubUserListFragment) {
        this.view = gitHubUserListFragment;
        this.compositeSubscription = new CompositeSubscription();
        realm = Realm.getDefaultInstance();
        setDataList();
    }

    public void unBind() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }

        realm.close();
        view.stopRefreshAnimation();
        this.view = null;
    }
}