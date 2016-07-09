package tank.viraj.realm.model;

import java.io.Serializable;

import lombok.Getter;
import tank.viraj.realm.realmModel.GitHubUserRealm;

/**
 * GitHubUser class should be Parcelable but for readability purpose Serializable is used
 * Created by Viraj Tank, 18-06-2016.
 */

@Getter
public class GitHubUser implements Serializable {
    private int id;
    private String login;
    private String avatar_url;

    public GitHubUser() {
    }

    public GitHubUser(int id, String login, String avatar_url) {
        this.id = id;
        this.login = login;
        this.avatar_url = avatar_url;
    }

    public GitHubUser(GitHubUserRealm gitHubUserRealm) {
        this.id = gitHubUserRealm.getId();
        this.login = gitHubUserRealm.getLogin();
        this.avatar_url = gitHubUserRealm.getAvatar_url();
    }
}