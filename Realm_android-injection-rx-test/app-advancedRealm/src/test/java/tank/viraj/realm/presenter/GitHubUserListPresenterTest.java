package tank.viraj.realm.presenter;

import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import rx.Observable;
import rx.schedulers.Schedulers;
import tank.viraj.realm.BuildConfig;
import tank.viraj.realm.MainApplicationTest;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.retrofit.GitHubApiInterface;
import tank.viraj.realm.ui.fragment.GitHubUserListFragment;
import tank.viraj.realm.util.RxSchedulerConfiguration;

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
@Config(constants = BuildConfig.class, sdk = 21, application = MainApplicationTest.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@PrepareForTest({Realm.class, RealmConfiguration.class,
        RealmQuery.class, RealmResults.class, RealmCore.class})
public class GitHubUserListPresenterTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Mock
    GitHubApiInterface gitHubApiInterface;

    @Mock
    RxSchedulerConfiguration rxSchedulerConfiguration;

    @Mock
    GitHubUserListFragment gitHubUserListFragment;

    private GitHubUserListPresenter gitHubUserListPresenter;

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
        when(mockRealm.createObject(GitHubUser.class)).thenReturn(new GitHubUser());

        // GitHubUsers mock data
        GitHubUser p1 = new GitHubUser(1, "testLogin1", "testName1");
        GitHubUser p2 = new GitHubUser(2, "testLogin2", "testName2");
        List<GitHubUser> gitHubUserList = Arrays.asList(p1, p2);

        // Define how RealmQuery mock
        RealmQuery<GitHubUser> gitHubUserListQuery = mockRealmQuery();
        when(mockRealm.where(GitHubUser.class)).thenReturn(gitHubUserListQuery);

        // Result Mock
        mockStatic(RealmResults.class);
        RealmResults<GitHubUser> gitHubUserListResult = mockRealmResults();
        when(mockRealm.where(GitHubUser.class).findAll()).thenReturn(gitHubUserListResult);
        when(gitHubUserListResult.iterator()).thenReturn(gitHubUserList.iterator());
        when(gitHubUserListResult.size()).thenReturn(gitHubUserList.size());

        // Retrofit mock
        Mockito.when(gitHubApiInterface.getGitHubUsersList())
                .thenReturn(Observable.just(gitHubUserList));

        // RxScheduler mock
        Mockito.when(rxSchedulerConfiguration.getComputationThread())
                .thenReturn(Schedulers.immediate());
        Mockito.when(rxSchedulerConfiguration.getMainThread())
                .thenReturn(Schedulers.immediate());

        gitHubUserListPresenter = new GitHubUserListPresenter(gitHubApiInterface,
                rxSchedulerConfiguration);
    }

    /* getGitHubUsersList list from Realm */
    @Test
    public void getGitHubUserListTest() {
        gitHubUserListPresenter.loadGitHubUserList(false);
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