package tank.viraj.realm.dao;

import io.realm.Realm;
import tank.viraj.realm.model.GitHubUserProfile;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileDao extends AbstractDao {

    public void storeOrUpdateProfile(GitHubUserProfile gitHubUserProfile) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(gitHubUserProfile));
        realm.close();
    }

    public GitHubUserProfile getProfile(String login) {
        Realm realm = Realm.getDefaultInstance();
        GitHubUserProfile gitHubUserProfile = realm.where(GitHubUserProfile.class)
                .equalTo("login", login)
                .findFirst();
        if (gitHubUserProfile != null) {
            gitHubUserProfile = realm.copyFromRealm(gitHubUserProfile);
        }
        realm.close();
        return gitHubUserProfile;
    }

    @Override
    public void clearDatabase() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.delete(GitHubUserProfile.class));
        realm.close();
    }
}