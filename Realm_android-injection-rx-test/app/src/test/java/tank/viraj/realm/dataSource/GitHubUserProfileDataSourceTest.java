package tank.viraj.realm.dataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import tank.viraj.realm.dao.GitHubUserProfileDao;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.util.RxSchedulerConfiguration;

import static org.mockito.Mockito.when;

/**
 * Created by Viraj Tank, 20/06/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHubUserProfileDataSourceTest {
    @Mock
    GitHubApiInterface gitHubApiInterface;
    @Mock
    GitHubUserProfileDao gitHubUserProfileDao;
    @Mock
    RxSchedulerConfiguration rxSchedulerConfiguration;

    private GitHubUserProfileDataSource gitHubUserProfileDataSource;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        gitHubUserProfileDataSource = new GitHubUserProfileDataSource(gitHubApiInterface,
                gitHubUserProfileDao, rxSchedulerConfiguration);
        when(rxSchedulerConfiguration.getComputationThread()).thenReturn(Schedulers.immediate());
        when(rxSchedulerConfiguration.getMainThread()).thenReturn(Schedulers.immediate());
    }

    /* get getGitHubUserProfile from Realm */
    @Test
    public void getGitHubUsersFromDaoTest() {
        GitHubUserProfile gitHubUserProfile = new GitHubUserProfile("testLogin", "testName", "testEmail");

        Mockito.when(gitHubUserProfileDao.getProfile("testLogin")).thenReturn(gitHubUserProfile);
        Mockito.when(gitHubApiInterface.getGitHubUserProfile("testLogin"))
                .thenReturn(Observable.just(gitHubUserProfile));

        TestSubscriber<GitHubUserProfile> testSubscriber = new TestSubscriber<>();
        gitHubUserProfileDataSource.getGitHubUserProfile("testLogin", false)
                .subscribe(testSubscriber);

        Assert.assertEquals(1, testSubscriber.getOnNextEvents().size());
        Assert.assertEquals("testLogin", testSubscriber.getOnNextEvents().get(0).getLogin());
        Assert.assertEquals("testName", testSubscriber.getOnNextEvents().get(0).getName());
        Assert.assertEquals("testEmail", testSubscriber.getOnNextEvents().get(0).getEmail());
    }

    /* get getGitHubUserProfile from Retrofit2 and update on Realm */
    @Test
    public void getGitHubUsersFromRetrofitTest() {
        GitHubUserProfile gitHubUserProfile = new GitHubUserProfile("testLogin",
                "testName", "testEmail");

        Mockito.when(gitHubApiInterface.getGitHubUserProfile("testLogin"))
                .thenReturn(Observable.just(gitHubUserProfile));

        TestSubscriber<GitHubUserProfile> testSubscriber = new TestSubscriber<>();
        gitHubUserProfileDataSource.getGitHubUserProfile("testLogin", true)
                .subscribe(testSubscriber);

        Assert.assertEquals(1, testSubscriber.getOnNextEvents().size());
        Assert.assertEquals("testLogin", testSubscriber.getOnNextEvents().get(0).getLogin());
        Assert.assertEquals("testName", testSubscriber.getOnNextEvents().get(0).getName());
        Assert.assertEquals("testEmail", testSubscriber.getOnNextEvents().get(0).getEmail());
    }
}