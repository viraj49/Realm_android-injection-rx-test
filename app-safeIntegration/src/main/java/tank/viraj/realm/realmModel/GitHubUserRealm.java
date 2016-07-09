package tank.viraj.realm.realmModel;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.Getter;
import tank.viraj.realm.model.GitHubUser;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
@Getter
public class GitHubUserRealm extends RealmObject {
    @PrimaryKey
    private String login;
    private int id;
    private String avatar_url;

    public GitHubUserRealm() {
    }

    public GitHubUserRealm(int id, String login, String avatar_url) {
        this.id = id;
        this.login = login;
        this.avatar_url = avatar_url;
    }

    public GitHubUserRealm(GitHubUser gitHubUser) {
        this.id = gitHubUser.getId();
        this.login = gitHubUser.getLogin();
        this.avatar_url = gitHubUser.getAvatar_url();
    }
}