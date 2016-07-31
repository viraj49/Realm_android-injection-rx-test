package tank.viraj.realm.dao;

import android.content.Context;

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

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.RealmCore;
import tank.viraj.realm.BuildConfig;
import tank.viraj.realm.MainApplicationTest;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.model.GitHubUserProfile;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by Viraj Tank, 20/06/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, application = MainApplicationTest.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@PrepareForTest({Realm.class, RealmConfiguration.class,
        RealmQuery.class, RealmResults.class, RealmCore.class})
public class GitHubUserProfileDaoTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    private GitHubUserProfileDao gitHubUserProfileDao;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        // Setup Realm to be mocked
        mockStatic(Realm.class);
        mockStatic(RealmConfiguration.class);
        mockStatic(RealmCore.class);

        // Create the mock
        final Realm mockRealm = mock(Realm.class);
        doNothing().when(RealmCore.class);
        RealmCore.loadLibrary(any(Context.class));
        when(Realm.getDefaultInstance()).thenReturn(mockRealm);
        when(mockRealm.createObject(GitHubUser.class)).thenReturn(new GitHubUser());

        // profile1
        GitHubUserProfile profile1 = new GitHubUserProfile("testLogin1",
                "testName1", "testEmail1");
        GitHubUserProfile profile2 = new GitHubUserProfile("testLogin2",
                "testName2", "testEmail2");
        GitHubUserProfile profile3 = null;

        RealmQuery<GitHubUserProfile> profileQuery = mockRealmQuery();
        RealmQuery<GitHubUserProfile> profileQuery1 = mockRealmQuery();
        RealmQuery<GitHubUserProfile> profileQuery2 = mockRealmQuery();
        RealmQuery<GitHubUserProfile> profileQuery3 = mockRealmQuery();

        when(mockRealm.where(GitHubUserProfile.class)).thenReturn(profileQuery);
        when(profileQuery.equalTo("login", "testLogin1")).thenReturn(profileQuery1);
        when(profileQuery.equalTo("login", "testLogin2")).thenReturn(profileQuery2);
        when(profileQuery.equalTo("login", "testLogin3")).thenReturn(profileQuery3);
        when(profileQuery1.findFirst()).thenReturn(profile1);
        when(profileQuery2.findFirst()).thenReturn(profile2);
        when(profileQuery3.findFirst()).thenReturn(profile3);

        // copyFromRealm mock
        when(mockRealm.copyFromRealm(profile1)).thenReturn(profile1);
        when(mockRealm.copyFromRealm(profile2)).thenReturn(profile2);
        when(mockRealm.copyFromRealm(profile3)).thenReturn(profile3);

        gitHubUserProfileDao = new GitHubUserProfileDao();
    }

    /* getGitHubUserProfile from Realm */
    @Test
    public void getGitHubUserProfileTest1() {
        GitHubUserProfile gitHubUserProfile = gitHubUserProfileDao.getProfile("testLogin1");

        assertEquals("testLogin1", gitHubUserProfile.getLogin());
        assertEquals("testName1", gitHubUserProfile.getName());
        assertEquals("testEmail1", gitHubUserProfile.getEmail());
    }

    /* clear getGitHubUserProfile from Realm */
    @Test
    public void getGitHubUserProfileTest2() {
        GitHubUserProfile gitHubUserProfile = gitHubUserProfileDao.getProfile("testLogin2");

        assertEquals("testLogin2", gitHubUserProfile.getLogin());
        assertEquals("testName2", gitHubUserProfile.getName());
        assertEquals("testEmail2", gitHubUserProfile.getEmail());
    }

    /* get getGitHubUserProfile from Realm */
    @Test
    public void getNullGitHubUserProfileTestTest() {
        GitHubUserProfile gitHubUserProfile = gitHubUserProfileDao.getProfile("testLogin3");

        assertEquals(null, gitHubUserProfile);
    }

    /* get getGitHubUsersList list from Realm */
    @Test
    public void clearGitHubUserProfileTest() {
        GitHubUserProfile gitHubUserProfile = gitHubUserProfileDao.getProfile("testLogin1");

        assertEquals("testLogin1", gitHubUserProfile.getLogin());
        assertEquals("testName1", gitHubUserProfile.getName());
        assertEquals("testEmail1", gitHubUserProfile.getEmail());

        gitHubUserProfileDao.clearDatabase();

        gitHubUserProfile = gitHubUserProfileDao.getProfile("testLogin1");

        assertEquals("testLogin1", gitHubUserProfile.getLogin());
        assertEquals("testName1", gitHubUserProfile.getName());
        assertEquals("testEmail1", gitHubUserProfile.getEmail());
    }

    @SuppressWarnings("unchecked")
    private <T extends RealmObject> RealmQuery<T> mockRealmQuery() {
        return mock(RealmQuery.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends RealmObject> RealmObject mockRealmResults() {
        return mock(RealmObject.class);
    }
}