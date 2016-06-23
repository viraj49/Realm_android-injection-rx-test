package tank.viraj.realm.realmModel;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import tank.viraj.realm.model.GitHubUser;

/**
 * Created by Viraj Tank, 20/06/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHubUserRealmTest {

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createObjectTest() {
        GitHubUserRealm gitHubUserRealm = new GitHubUserRealm(1, "testLogin", "testAvatarUrl");

        Assert.assertEquals(1, gitHubUserRealm.getId());
        Assert.assertEquals("testLogin", gitHubUserRealm.getLogin());
        Assert.assertEquals("testAvatarUrl", gitHubUserRealm.getAvatar_url());
    }

    @Test
    public void modelToRealmObjectTest() {
        GitHubUser gitHubUser = new GitHubUser(1, "testLogin", "testAvatarUrl");
        GitHubUserRealm gitHubUserRealm = new GitHubUserRealm(gitHubUser);

        Assert.assertEquals(1, gitHubUserRealm.getId());
        Assert.assertEquals("testLogin", gitHubUserRealm.getLogin());
        Assert.assertEquals("testAvatarUrl", gitHubUserRealm.getAvatar_url());
    }
}