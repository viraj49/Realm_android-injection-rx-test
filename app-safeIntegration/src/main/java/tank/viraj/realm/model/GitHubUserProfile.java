package tank.viraj.realm.model;

import lombok.Getter;
import tank.viraj.realm.realmModel.GitHubUserProfileRealm;

/**
 * Created by Viraj Tank, 18-06-2016.
 */

@Getter
public class GitHubUserProfile {
    private String login;
    private String name;
    private String email;

    public GitHubUserProfile() {
    }

    public GitHubUserProfile(String login, String name, String email) {
        this.login = login;
        this.name = name;
        this.email = email;
    }

    public GitHubUserProfile(GitHubUserProfileRealm gitHubUserProfileRealm) {
        this.login = gitHubUserProfileRealm.getLogin();
        this.name = gitHubUserProfileRealm.getName();
        this.email = gitHubUserProfileRealm.getEmail();
    }
}