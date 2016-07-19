package tank.viraj.realm.realmModel;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.Getter;
import tank.viraj.realm.jsonModel.GitHubUserProfile;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
@Getter
public class GitHubUserProfileRealm extends RealmObject {
    @PrimaryKey
    private String login;
    private String name;
    private String email;

    public GitHubUserProfileRealm() {
    }

    public GitHubUserProfileRealm(String login, String name, String email) {
        this.login = login;
        this.name = name;
        this.email = email;
    }

    public GitHubUserProfileRealm(GitHubUserProfile gitHubUserProfile) {
        this.login = gitHubUserProfile.getLogin();
        this.name = gitHubUserProfile.getName();
        this.email = gitHubUserProfile.getEmail();
    }
}