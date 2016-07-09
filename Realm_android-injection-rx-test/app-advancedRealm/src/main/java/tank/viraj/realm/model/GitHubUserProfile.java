package tank.viraj.realm.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Viraj Tank, 18-06-2016.
 */

@Setter
@Getter
public class GitHubUserProfile extends RealmObject {
    @PrimaryKey
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
}