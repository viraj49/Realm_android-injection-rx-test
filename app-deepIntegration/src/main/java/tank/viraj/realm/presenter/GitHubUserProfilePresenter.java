package tank.viraj.realm.presenter;


import io.realm.Realm;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.ui.fragment.GitHubUserProfileFragment;
import tank.viraj.realm.util.InternetConnection;
import tank.viraj.realm.util.RxSchedulerConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfilePresenter {
    private GitHubUserProfileFragment view;
    private GitHubApiInterface gitHubApiInterface;
    private CompositeSubscription compositeSubscription;
    private RxSchedulerConfiguration rxSchedulerConfiguration;
    private Realm realm;

    public GitHubUserProfilePresenter(GitHubApiInterface gitHubApiInterface,
                                      RxSchedulerConfiguration rxSchedulerConfiguration) {
        this.gitHubApiInterface = gitHubApiInterface;
        this.rxSchedulerConfiguration = rxSchedulerConfiguration;
    }

    public void loadGitHubUserProfile(String login, boolean isForced) {
        compositeSubscription.add(Observable.concat(getGitHubUserProfileFromRealm(login, isForced),
                getGitHubUserProfileFromRetrofit(login))
                .first(profile -> profile)
                .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .subscribe(aBoolean -> {
                    view.stopRefreshAnimation();
                }, throwable -> {
                    view.stopRefreshAnimation();
                }));
    }

    private Observable<Boolean> getGitHubUserProfileFromRealm(String login, boolean isForced) {
        return Observable.just(isForced)
                .filter(isForcedIn -> !isForcedIn)
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .map(isForcedIn -> realm.where(GitHubUserProfile.class)
                        .equalTo("login", login)
                        .findFirst() != null);
    }

    private Observable<Boolean> getGitHubUserProfileFromRetrofit(String login) {
        return new InternetConnection().isInternetOn(view.getActivity())
                .filter(connectionStatus -> connectionStatus)
                .switchMap(connectionStatus -> gitHubApiInterface.getGitHubUserProfile(login))
                .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .map(gitHubUserProfile -> {
                    realm.executeTransactionAsync(realm1 ->
                            realm1.copyToRealmOrUpdate(gitHubUserProfile));
                    return gitHubUserProfile != null;
                });
    }

    private void setDataList(String login) {
        compositeSubscription.add(realm.where(GitHubUserProfile.class)
                .equalTo("login", login)
                .findFirstAsync()
                .asObservable()
                .cast(GitHubUserProfile.class)
                .filter(realmObject -> realmObject.isLoaded())
                .filter(realmObject -> realmObject.isValid())
                .doOnSubscribe(() -> view.startRefreshAnimation())
                .subscribeOn(rxSchedulerConfiguration.getMainThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .subscribe(gitHubUserProfile -> {
                    view.setData(gitHubUserProfile);
                    view.stopRefreshAnimation();
                }, throwable -> {
                    view.stopRefreshAnimation();
                }));
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

    public void bind(GitHubUserProfileFragment gitHubUserProfileFragment, String login) {
        this.view = gitHubUserProfileFragment;
        this.compositeSubscription = new CompositeSubscription();
        realm = Realm.getDefaultInstance();
        setDataList(login);
    }

    public void unBind() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }

        realm.close();
        view.stopRefreshAnimation();
        this.view = null;
    }
}