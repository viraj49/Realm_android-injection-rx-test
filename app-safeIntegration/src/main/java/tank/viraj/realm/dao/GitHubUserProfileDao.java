package tank.viraj.realm.dao;

import io.realm.Realm;
import tank.viraj.realm.jsonModel.GitHubUserProfile;
import tank.viraj.realm.realmModel.GitHubUserProfileRealm;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileDao extends AbstractDao {

    public void storeOrUpdateProfile(GitHubUserProfile gitHubUserProfile) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 ->
                realm1.copyToRealmOrUpdate(fromModelToRealm(gitHubUserProfile)));
        realm.close();
    }

    public GitHubUserProfile getProfile(String login) {
        Realm realm = Realm.getDefaultInstance();
        GitHubUserProfileRealm gitHubUserProfileRealm = realm.where(GitHubUserProfileRealm.class)
                .equalTo("login", login)
                .findFirst();
        GitHubUserProfile gitHubUserProfile = fromRealmToModel(gitHubUserProfileRealm);
        realm.close();
        return gitHubUserProfile;
    }

    @Override
    public void clearDatabase() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.delete(GitHubUserProfileRealm.class));
        realm.close();
    }

    /* Conversion methods */
    private GitHubUserProfile fromRealmToModel(GitHubUserProfileRealm gitHubUserProfileRealm) {
        if (gitHubUserProfileRealm != null) {
            return new GitHubUserProfile(gitHubUserProfileRealm);
        } else {
            return null;
        }
    }

    private GitHubUserProfileRealm fromModelToRealm(GitHubUserProfile gitHubUserProfile) {
        if (gitHubUserProfile != null) {
            return new GitHubUserProfileRealm(gitHubUserProfile);
        } else {
            return null;
        }
    }
}