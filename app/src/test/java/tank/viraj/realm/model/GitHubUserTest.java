package tank.viraj.realm.model;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import tank.viraj.realm.realmModel.GitHubUserRealm;

/**
 * Created by Viraj Tank, 20/06/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHubUserTest {

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createObjectTest() {
        GitHubUser gitHubUser = new GitHubUser(1, "testLogin", "testAvatarUrl");

        Assert.assertEquals(1, gitHubUser.getId());
        Assert.assertEquals("testLogin", gitHubUser.getLogin());
        Assert.assertEquals("testAvatarUrl", gitHubUser.getAvatar_url());
    }

    @Test
    public void realmToModelObjectTest() {
        GitHubUserRealm gitHubUserRealm = new GitHubUserRealm(1, "testLogin", "testAvatarUrl");
        GitHubUser gitHubUser = new GitHubUser(gitHubUserRealm);

        Assert.assertEquals(1, gitHubUser.getId());
        Assert.assertEquals("testLogin", gitHubUser.getLogin());
        Assert.assertEquals("testAvatarUrl", gitHubUser.getAvatar_url());
    }
}