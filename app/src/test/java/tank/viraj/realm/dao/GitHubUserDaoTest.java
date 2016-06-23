package tank.viraj.realm.dao;

import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.RealmCore;
import tank.viraj.realm.BuildConfig;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.realmModel.GitHubUserRealm;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created by Viraj Tank, 20/06/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@PrepareForTest({Realm.class, RealmConfiguration.class, RealmQuery.class, RealmResults.class, RealmCore.class})
public class GitHubUserDaoTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    private GitHubUserDao gitHubUserDao;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        // Setup Realm + Mock
        mockStatic(Realm.class);
        mockStatic(RealmConfiguration.class);
        mockStatic(RealmCore.class);

        final Realm mockRealm = mock(Realm.class);
        final RealmConfiguration mockRealmConfig = mock(RealmConfiguration.class);
        doNothing().when(RealmCore.class);
        RealmCore.loadLibrary(any(Context.class));
        whenNew(RealmConfiguration.class).withAnyArguments().thenReturn(mockRealmConfig);
        when(Realm.getInstance(any(RealmConfiguration.class))).thenReturn(mockRealm);
        when(mockRealm.createObject(GitHubUserRealm.class)).thenReturn(new GitHubUserRealm());

        // GitHubUsers mock data
        GitHubUserRealm p1 = new GitHubUserRealm(1, "testLogin1", "testName1");
        GitHubUserRealm p2 = new GitHubUserRealm(2, "testLogin2", "testName2");
        List<GitHubUserRealm> gitHubUserList = Arrays.asList(p1, p2);

        // Define how RealmQuery mock
         RealmQuery<GitHubUserRealm> gitHubUserListQuery = mockRealmQuery();
         when(mockRealm.where(GitHubUserRealm.class)).thenReturn(gitHubUserListQuery);

        // Result Mock
        mockStatic(RealmResults.class);
        RealmResults<GitHubUserRealm> gitHubUserListResult = mockRealmResults();
        when(mockRealm.where(GitHubUserRealm.class).findAll()).thenReturn(gitHubUserListResult);
        when(gitHubUserListResult.iterator()).thenReturn(gitHubUserList.iterator());
        when(gitHubUserListResult.size()).thenReturn(gitHubUserList.size());

        gitHubUserDao = new GitHubUserDao(mockRealmConfig);
    }

    /* getGitHubUsersList list from Realm */
    @Test
    public void getGitHubUserListTest() {
        List<GitHubUser> gitHubUserListResult = gitHubUserDao.getGitHubUserList();

        Assert.assertEquals(1, gitHubUserListResult.get(0).getId());
        Assert.assertEquals("testLogin1", gitHubUserListResult.get(0).getLogin());
        Assert.assertEquals("testName1", gitHubUserListResult.get(0).getAvatar_url());
        Assert.assertEquals(2, gitHubUserListResult.get(1).getId());
        Assert.assertEquals("testLogin2", gitHubUserListResult.get(1).getLogin());
        Assert.assertEquals("testName2", gitHubUserListResult.get(1).getAvatar_url());
    }

    /* clear list of gitHubUserList */
    @Test
    public void clearGitHubUserListTest() {
        List<GitHubUser> gitHubUserListResult = gitHubUserDao.getGitHubUserList();

        Assert.assertEquals(2, gitHubUserListResult.size());
        Assert.assertEquals(1, gitHubUserListResult.get(0).getId());
        Assert.assertEquals("testLogin1", gitHubUserListResult.get(0).getLogin());
        Assert.assertEquals("testName1", gitHubUserListResult.get(0).getAvatar_url());
        Assert.assertEquals(2, gitHubUserListResult.get(1).getId());
        Assert.assertEquals("testLogin2", gitHubUserListResult.get(1).getLogin());
        Assert.assertEquals("testName2", gitHubUserListResult.get(1).getAvatar_url());

        gitHubUserDao.clearDatabase();

        gitHubUserListResult = gitHubUserDao.getGitHubUserList();

        Assert.assertEquals(0, gitHubUserListResult.size());
    }

    @SuppressWarnings("unchecked")
    private <T extends RealmObject> RealmQuery<T> mockRealmQuery() {
        return mock(RealmQuery.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends RealmObject> RealmResults<T> mockRealmResults() {
        return mock(RealmResults.class);
    }
}