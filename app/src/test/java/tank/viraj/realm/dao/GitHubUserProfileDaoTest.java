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
import tank.viraj.realm.jsonModel.GitHubUserProfile;
import tank.viraj.realm.realmModel.GitHubUserProfileRealm;
import tank.viraj.realm.realmModel.GitHubUserRealm;

import static org.junit.Assert.assertEquals;
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
        final RealmConfiguration mockRealmConfig = mock(RealmConfiguration.class);
        doNothing().when(RealmCore.class);
        RealmCore.loadLibrary(any(Context.class));
        whenNew(RealmConfiguration.class).withAnyArguments().thenReturn(mockRealmConfig);
        when(Realm.getInstance(any(RealmConfiguration.class))).thenReturn(mockRealm);
        when(mockRealm.createObject(GitHubUserRealm.class)).thenReturn(new GitHubUserRealm());

        // profile1
        GitHubUserProfileRealm profile1 = new GitHubUserProfileRealm("testLogin1", "testName1", "testEmail1");
        GitHubUserProfileRealm profile2 = new GitHubUserProfileRealm("testLogin2", "testName2", "testEmail2");
        GitHubUserProfileRealm profile3 = null;

        RealmQuery<GitHubUserProfileRealm> profileQuery = mockRealmQuery();
        RealmQuery<GitHubUserProfileRealm> profileQuery1 = mockRealmQuery();
        RealmQuery<GitHubUserProfileRealm> profileQuery2 = mockRealmQuery();
        RealmQuery<GitHubUserProfileRealm> profileQuery3 = mockRealmQuery();

        when(mockRealm.where(GitHubUserProfileRealm.class)).thenReturn(profileQuery);
        when(profileQuery.equalTo("login", "testLogin1")).thenReturn(profileQuery1);
        when(profileQuery.equalTo("login", "testLogin2")).thenReturn(profileQuery2);
        when(profileQuery.equalTo("login", "testLogin3")).thenReturn(profileQuery3);
        when(profileQuery1.findFirst()).thenReturn(profile1);
        when(profileQuery2.findFirst()).thenReturn(profile2);
        when(profileQuery3.findFirst()).thenReturn(profile3);

        gitHubUserProfileDao = new GitHubUserProfileDao(mockRealmConfig);
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
}