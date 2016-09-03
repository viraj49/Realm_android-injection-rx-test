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
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(gitHubUserList));
        realm.close();
    }

    public Boolean getGitHubUserListStatus() {
        Realm realm = Realm.getDefaultInstance();
        boolean dataStatus;
        dataStatus = (realm.where(GitHubUser.class).count() > 0);
        realm.close();
        return dataStatus;
    }

    @Override
    public void clearDatabase() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.delete(GitHubUser.class));
        realm.close();
    }
}