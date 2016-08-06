package tank.viraj.realm.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import tank.viraj.realm.R;
import tank.viraj.realm.ui.fragment.GitHubUserListFragment;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListActivity extends AppCompatActivity {
    private static final String FLAG_COMMIT_FRAGMENT = "gitHubUserListFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActionBarTitle();

        /* create/find the fragment and load it in frame layout */
        GitHubUserListFragment gitHubUserListFragment = (GitHubUserListFragment) getSupportFragmentManager().findFragmentByTag(FLAG_COMMIT_FRAGMENT);
        if (gitHubUserListFragment == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_main, new GitHubUserListFragment(), FLAG_COMMIT_FRAGMENT)
                    .commit();
        }
    }

    private void setActionBarTitle() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getResources().getString(R.string.user_list));
        }
    }
}
