package tank.viraj.realm.model;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by Viraj Tank, 20/06/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHubUserProfileTest {
    @Test
    public void createObjectTest() {
        GitHubUserProfile gitHubUserProfile = new GitHubUserProfile("testLogin",
                "testName", "testEmail");

        Assert.assertEquals("testLogin", gitHubUserProfile.getLogin());
        Assert.assertEquals("testName", gitHubUserProfile.getName());
        Assert.assertEquals("testEmail", gitHubUserProfile.getEmail());
    }
}