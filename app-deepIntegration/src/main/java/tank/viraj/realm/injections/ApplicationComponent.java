package tank.viraj.realm.injections;

import javax.inject.Singleton;

import dagger.Component;
import tank.viraj.realm.ui.fragment.GitHubUserListFragment;
import tank.viraj.realm.ui.fragment.GitHubUserProfileFragment;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(GitHubUserListFragment gitHubUserListFragment);
    void inject(GitHubUserProfileFragment gitHubUserProfileFragment);
}