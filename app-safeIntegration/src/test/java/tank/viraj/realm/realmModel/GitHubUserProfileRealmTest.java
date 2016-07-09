package tank.viraj.realm.realmModel;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import tank.viraj.realm.model.GitHubUserProfile;

/**
 * Created by Viraj Tank, 20/06/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHubUserProfileRealmTest {

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createObjectTest() {
        GitHubUserProfileRealm gitHubUserProfileRealm = new GitHubUserProfileRealm("testLogin",
                "testName", "testEmail");

        Assert.assertEquals("testLogin", gitHubUserProfileRealm.getLogin());
        Assert.assertEquals("testName", gitHubUserProfileRealm.getName());
        Assert.assertEquals("testEmail", gitHubUserProfileRealm.getEmail());
    }

    @Test
    public void modelToRealmObjectTest() {
        GitHubUserProfile gitHubUserProfile = new GitHubUserProfile("testLogin", "testName",
                "testEmail");
        GitHubUserProfileRealm gitHubUserProfileRealm = new GitHubUserProfileRealm(gitHubUserProfile);

        Assert.assertEquals("testLogin", gitHubUserProfileRealm.getLogin());
        Assert.assertEquals("testName", gitHubUserProfileRealm.getName());
        Assert.assertEquals("testEmail", gitHubUserProfileRealm.getEmail());
    }
}