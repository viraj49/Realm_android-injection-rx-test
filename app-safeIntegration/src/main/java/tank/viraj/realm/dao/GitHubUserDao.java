package tank.viraj.realm.dao;

import java.util.List;

import io.realm.Realm;
import tank.viraj.realm.model.GitHubUser;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserDao extends AbstractDao {

    public void storeOrUpdateGitHubUserList(List<GitHubUser> gitHubUserList) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(gitHubUserList));
        realm.close();
    }

    public List<GitHubUser> getGitHubUserList() {
        Realm realm = Realm.getDefaultInstance();
        List<GitHubUser> gitHubUserList = realm.copyFromRealm(realm.where(GitHubUser.class).findAll());
        realm.close();
        return gitHubUserList;
    }

    @Override
    public void clearDatabase() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.delete(GitHubUser.class));
        realm.close();
    }
}