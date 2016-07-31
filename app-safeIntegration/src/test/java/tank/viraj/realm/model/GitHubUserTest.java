package tank.viraj.realm.model;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * Created by Viraj Tank, 20/06/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHubUserTest {
    @Test
    public void createObjectTest() {
        GitHubUser gitHubUser = new GitHubUser(1, "testLogin", "testAvatarUrl");

        Assert.assertEquals(1, gitHubUser.getId());
        Assert.assertEquals("testLogin", gitHubUser.getLogin());
        Assert.assertEquals("testAvatarUrl", gitHubUser.getAvatar_url());
    }
}