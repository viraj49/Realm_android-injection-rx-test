package tank.viraj.realm.dao;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import tank.viraj.realm.jsonModel.GitHubUser;
import tank.viraj.realm.realmModel.GitHubUserRealm;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserDao extends AbstractDao {

    public void storeOrUpdateGitHubUserList(List<GitHubUser> gitHubUserList) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 ->
                realm1.copyToRealmOrUpdate(fromModelToRealm(gitHubUserList)));
        realm.close();
    }

    public List<GitHubUser> getGitHubUserList() {
        Realm realm = Realm.getDefaultInstance();
        List<GitHubUser> gitHubUserList = fromRealmToModel(realm.where(GitHubUserRealm.class)
                .findAll());
        realm.close();
        return gitHubUserList;
    }

    @Override
    public void clearDatabase() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.delete(GitHubUserRealm.class));
        realm.close();
    }

    /* Conversion methods */
    private List<GitHubUser> fromRealmToModel(List<GitHubUserRealm> gitHubUserRealmList) {
        List<GitHubUser> gitHubUserList = new ArrayList<>();
        for (GitHubUserRealm gitHubUserRealm : gitHubUserRealmList) {
            gitHubUserList.add(new GitHubUser(gitHubUserRealm));
        }
        return gitHubUserList;
    }

    private List<GitHubUserRealm> fromModelToRealm(List<GitHubUser> gitHubUserList) {
        List<GitHubUserRealm> gitHubUserRealmList = new ArrayList<>();
        for (GitHubUser gitHubUser : gitHubUserList) {
            gitHubUserRealmList.add(new GitHubUserRealm(gitHubUser));
        }
        return gitHubUserRealmList;
    }
}