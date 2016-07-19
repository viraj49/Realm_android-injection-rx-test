package tank.viraj.realm.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import tank.viraj.realm.R;
import tank.viraj.realm.jsonModel.GitHubUser;
import tank.viraj.realm.ui.fragment.GitHubUserProfileFragment;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileActivity extends AppCompatActivity {
    private static final String FLAG_COMMIT_FRAGMENT = "gitHubUserProfileFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setActionBarTitle();

        GitHubUserProfileFragment gitHubUserProfileFragment;

        /* create the fragment and load it in frame layout */
        Intent intent = getIntent();
        boolean commitFragment = intent.getBooleanExtra(FLAG_COMMIT_FRAGMENT, true);
        if (savedInstanceState == null && commitFragment) {
            gitHubUserProfileFragment = new GitHubUserProfileFragment();
            GitHubUser gitHubUser = (GitHubUser) getIntent()
                    .getSerializableExtra(getString(R.string.github_user_key));
            gitHubUserProfileFragment.setGitHubUser(gitHubUser);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_profile, gitHubUserProfileFragment)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setActionBarTitle() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getResources().getString(R.string.user_profile));
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }
    }
}
