package tank.viraj.realm.injections;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import tank.viraj.realm.adapter.MainAdapter;
import tank.viraj.realm.dao.GitHubUserDao;
import tank.viraj.realm.dao.GitHubUserProfileDao;
import tank.viraj.realm.dataSource.GitHubUserListDataSource;
import tank.viraj.realm.dataSource.GitHubUserProfileDataSource;
import tank.viraj.realm.presenter.GitHubUserPresenter;
import tank.viraj.realm.presenter.GitHubUserProfilePresenter;
import tank.viraj.realm.retrofit.GitHubApiInterface;

/**
 * Created by Viraj Tank, 18-06-2016.
 */

@Module
public class ApplicationModule {
    private static final String baseUrl = "https://api.github.com/";

    private Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    /* Provides singleton Realm configuration for all Dao class */
    @Provides
    @Singleton
    public RealmConfiguration provideRealmConfiguration() {
        return new RealmConfiguration.Builder(application.getApplicationContext())
                .deleteRealmIfMigrationNeeded()
                .build();
    }

    /* DAO (data access object) for GitHubUser model */
    @Provides
    @Singleton
    GitHubUserDao provideGitHubUserDao(RealmConfiguration realmConfiguration) {
        return new GitHubUserDao(realmConfiguration);
    }

    /* DAO (data access object) for GitHubUser model */
    @Provides
    @Singleton
    GitHubUserProfileDao provideGitHubUserProfileDao(RealmConfiguration realmConfiguration) {
        return new GitHubUserProfileDao(realmConfiguration);
    }

    /* Presenter for GitHubUser */
    @Provides
    @Singleton
    GitHubUserPresenter provideGitHubPresenter(GitHubUserListDataSource gitHubUserListDataSource) {
        return new GitHubUserPresenter(gitHubUserListDataSource);
    }

    /* Presenter for GitHubUserProfile */
    @Provides
    @Singleton
    GitHubUserProfilePresenter provideProfilePresenter(GitHubUserProfileDataSource gitHubUserProfileDataSource) {
        return new GitHubUserProfilePresenter(gitHubUserProfileDataSource);
    }

    /* Data source for GitHubUserListDataSource */
    @Provides
    @Singleton
    GitHubUserListDataSource provideGitHubUserListDataSource(GitHubApiInterface gitHubApiInterface, GitHubUserDao gitHubUserDao) {
        return new GitHubUserListDataSource(gitHubApiInterface, gitHubUserDao);
    }

    /* Data source for GitHubUserProfileDataSource */
    @Provides
    @Singleton
    GitHubUserProfileDataSource provideGitHubUserProfileDataSource(GitHubApiInterface gitHubApiInterface, GitHubUserProfileDao gitHubUserProfileDao) {
        return new GitHubUserProfileDataSource(gitHubApiInterface, gitHubUserProfileDao);
    }

    /* OkHttpclient for retrofit2 */
    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    /* retrofit2 */
    @Provides
    @Singleton
    GitHubApiInterface provideGitHubApiInterface(OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        return retrofit.create(GitHubApiInterface.class);
    }

    /* Data adapter for recycler view */
    @Provides
    @Singleton
    MainAdapter provideMainAdapter() {
        return new MainAdapter(application.getApplicationContext());
    }
}