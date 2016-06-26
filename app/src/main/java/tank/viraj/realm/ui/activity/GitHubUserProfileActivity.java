package tank.viraj.realm.ui.activity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setActionBarTitle();

        /* get the GitHubUser object,
         * create the fragment and load it in frame layout */
        GitHubUserProfileFragment gitHubUserProfileFragment = new GitHubUserProfileFragment();
        GitHubUser gitHubUser = (GitHubUser) getIntent().getSerializableExtra(getString(R.string.github_user_key));
        gitHubUserProfileFragment.setGitHubUser(gitHubUser);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_profile, gitHubUserProfileFragment)
                .commit();
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