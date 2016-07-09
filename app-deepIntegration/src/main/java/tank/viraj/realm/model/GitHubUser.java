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
public class GitHubUser extends RealmObject {
    @PrimaryKey
    private String login;
    private int id;
    private String avatar_url;

    public GitHubUser() {
    }

    public GitHubUser(int id, String login, String avatar_url) {
        this.id = id;
        this.login = login;
        this.avatar_url = avatar_url;
    }
}