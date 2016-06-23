package tank.viraj.realm.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import tank.viraj.realm.MainApplication;
import tank.viraj.realm.R;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.presenter.GitHubUserProfilePresenter;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    @Inject
    GitHubUserProfilePresenter gitHubUserProfilePresenter;

    @BindView(R.id.profile_icon)
    ImageView profileIcon;

    @BindView(R.id.profile_name)
    TextView profileName;

    @BindView(R.id.profile_email)
    TextView profileEmail;

    @BindView(R.id.refresh_profile)
    SwipeRefreshLayout pullToRefreshLayout;

    private GitHubUser gitHubUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainApplication) getActivity().getApplication()).getApplicationComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        ButterKnife.bind(this, view);

        // pull to refresh
        pullToRefreshLayout.setOnRefreshListener(this);
        pullToRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        pullToRefreshLayout.canChildScrollUp();

        /* bind the view and load data from Realm */
        gitHubUserProfilePresenter.bind(this);
        gitHubUserProfilePresenter.loadGitHubUserProfile(gitHubUser.getLogin(), false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_realm:
                gitHubUserProfilePresenter.clearGitHubUserProfileFromRealm();
                Toast.makeText(getActivity(), R.string.clear_user_profile, Toast.LENGTH_SHORT).show();
                return true;
            case android.R.id.home:
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startRefreshAnimation() {
        pullToRefreshLayout.post(() -> pullToRefreshLayout.setRefreshing(true));
    }

    public void stopRefreshAnimation() {
        pullToRefreshLayout.post(() -> pullToRefreshLayout.setRefreshing(false));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        gitHubUserProfilePresenter.unBind();
    }

    public void setGitHubUser(GitHubUser gitHubUser) {
        this.gitHubUser = gitHubUser;
    }

    public void setData(GitHubUserProfile gitHubUserProfile) {
        Picasso.with(getActivity())
                .load(gitHubUser.getAvatar_url())
                .into(profileIcon);
        profileName.setText(String.format("%s%s", getActivity().getString(R.string.name), gitHubUserProfile.getName()));
        profileEmail.setText(String.format("%s%s", getActivity().getString(R.string.email), gitHubUserProfile.getEmail()));
    }

    @Override
    public void onRefresh() {
        /* load fresh data */
        gitHubUserProfilePresenter.loadGitHubUserProfile(gitHubUser.getLogin(), true);
    }
}