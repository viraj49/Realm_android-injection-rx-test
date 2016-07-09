package tank.viraj.realm.presenter;


import android.util.Log;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmObject;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.ui.fragment.GitHubUserProfileFragment;
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
                .takeFirst(profile -> profile)
                .subscribeOn(rxSchedulerConfiguration.getComputationThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .subscribe());
    }

    private Observable<Boolean> getGitHubUserProfileFromRealm(String login, boolean isForced) {
        return Observable.just(isForced)
                .filter(isForcedIn -> !isForcedIn)
                .subscribeOn(rxSchedulerConfiguration.getMainThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .map(isForcedIn -> {
                    return realm.where(GitHubUserProfile.class)
                            .equalTo("login", login)
                            .findFirst() != null;
                });
    }

    private Observable<Boolean> getGitHubUserProfileFromRetrofit(String login) {
        return gitHubApiInterface.getGitHubUserProfile(login)
                .subscribeOn(rxSchedulerConfiguration.getMainThread())
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
                .filter(new Func1<RealmObject, Boolean>() {
                    @Override
                    public Boolean call(RealmObject realmObject) {
                        return realmObject.isLoaded();
                    }
                })
                .filter(new Func1<RealmObject, Boolean>() {
                    @Override
                    public Boolean call(RealmObject realmObject) {
                        return realmObject.isValid();
                    }
                })
                .doOnSubscribe(() -> view.startRefreshAnimation())
                .subscribeOn(rxSchedulerConfiguration.getMainThread())
                .observeOn(rxSchedulerConfiguration.getMainThread())
                .subscribe(new Observer<GitHubUserProfile>() {
                    @Override
                    public void onCompleted() {
                        /* do nothing */
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.stopRefreshAnimation();
                    }

                    @Override
                    public void onNext(GitHubUserProfile gitHubUserProfile) {
                        Log.e("VIRAJ", "Profile actually setting data");
                        view.setData(gitHubUserProfile);
                        view.stopRefreshAnimation();
                    }
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