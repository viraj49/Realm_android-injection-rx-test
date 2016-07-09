package tank.viraj.realm.dataSource;

import android.content.Context;

import java.util.List;

import rx.Observable;
import tank.viraj.realm.dao.GitHubUserDao;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.InternetConnection;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListDataSource {
    private Context context;
    private GitHubApiInterface gitHubApiInterface;
    private GitHubUserDao gitHubUserDao;
    private InternetConnection internetConnection;

    public GitHubUserListDataSource(Context context, GitHubApiInterface gitHubApiInterface,
                                    GitHubUserDao gitHubUserDao, InternetConnection internetConnection) {
        this.context = context;
        this.gitHubApiInterface = gitHubApiInterface;
        this.gitHubUserDao = gitHubUserDao;
        this.internetConnection = internetConnection;
    }

    public Observable<List<GitHubUser>> getGitHubUsers(boolean isForced) {
        return Observable.concat(getGitHubUsersFromRealm(isForced),
                getGitHubUsersFromRetrofit())
                .takeFirst(gitHubUserList -> gitHubUserList != null && gitHubUserList.size() > 0);
    }

    private Observable<List<GitHubUser>> getGitHubUsersFromRealm(boolean isForced) {
        return !isForced ?
                Observable.just(gitHubUserDao.getGitHubUserList()) :
                Observable.empty();
    }

    private Observable<List<GitHubUser>> getGitHubUsersFromRetrofit() {
        return internetConnection.isInternetOn(context)
                .switchMap(connectionStatus -> connectionStatus ? gitHubApiInterface.getGitHubUsersList()
                        .map(gitHubUserList -> {
                            gitHubUserDao.storeOrUpdateGitHubUserList(gitHubUserList);
                            return gitHubUserList;
                        }) : Observable.empty());
    }

    public void clearRealmData() {
        gitHubUserDao.clearDatabase();
    }
}