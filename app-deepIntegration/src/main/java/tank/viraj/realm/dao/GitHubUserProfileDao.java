package tank.viraj.realm.dao;

import io.realm.Realm;
import tank.viraj.realm.model.GitHubUserProfile;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileDao extends AbstractDao {

    public void storeOrUpdateProfile(GitHubUserProfile gitHubUserProfile) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(gitHubUserProfile));
        realm.close();
    }

    public Boolean getProfileStatus(String login) {
        Realm realm = Realm.getDefaultInstance();
        boolean dataStatus;
        dataStatus = realm.where(GitHubUserProfile.class)
                .equalTo("login", login)
                .findFirst() != null;
        realm.close();
        return dataStatus;
    }

    @Override
    public void clearDatabase() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.delete(GitHubUserProfile.class));
        realm.close();
    }
}