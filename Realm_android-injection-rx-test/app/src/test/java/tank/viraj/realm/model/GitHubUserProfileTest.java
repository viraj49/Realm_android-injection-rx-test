package tank.viraj.realm.model;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import tank.viraj.realm.realmModel.GitHubUserProfileRealm;

/**
 * Created by Viraj Tank, 20/06/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHubUserProfileTest {

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createObjectTest() {
        GitHubUserProfile gitHubUserProfile = new GitHubUserProfile("testLogin",
                "testName", "testEmail");

        Assert.assertEquals("testLogin", gitHubUserProfile.getLogin());
        Assert.assertEquals("testName", gitHubUserProfile.getName());
        Assert.assertEquals("testEmail", gitHubUserProfile.getEmail());
    }

    @Test
    public void RealmToModelObjectTest() {
        GitHubUserProfileRealm gitHubUserProfileRealm = new GitHubUserProfileRealm("testLogin",
                "testName", "testEmail");
        GitHubUserProfile gitHubUserProfile = new GitHubUserProfile(gitHubUserProfileRealm);

        Assert.assertEquals("testLogin", gitHubUserProfile.getLogin());
        Assert.assertEquals("testName", gitHubUserProfile.getName());
        Assert.assertEquals("testEmail", gitHubUserProfile.getEmail());
    }
}