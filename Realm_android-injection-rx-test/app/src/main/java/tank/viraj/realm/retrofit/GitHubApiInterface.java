package tank.viraj.realm.retrofit;


import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.model.GitHubUserProfile;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public interface GitHubApiInterface {
    @GET("/users")
    Observable<List<GitHubUser>> getGitHubUsersList();

    @GET("/users/{login}")
    Observable<GitHubUserProfile> getGitHubUserProfile(@Path("login") String login);
}