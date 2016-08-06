package tank.viraj.realm.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import butterknife.Unbinder;
import tank.viraj.realm.MainApplication;
import tank.viraj.realm.R;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.presenter.GitHubUserProfilePresenter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserProfileFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {
    @Inject
    GitHubUserProfilePresenter gitHubUserProfilePresenter;

    @BindView(R.id.profile_icon)
    ImageView profileIcon;

    @BindView(R.id.profile_name)
    TextView profileName;

    @BindView(R.id.profile_email)
    TextView profileEmail;

    @BindView(R.id.error_message)
    TextView errorMessage;

    @BindView(R.id.refresh_profile)
    SwipeRefreshLayout pullToRefreshLayout;

    private GitHubUser gitHubUser;
    private Unbinder unbinder;
    private GitHubUserProfile gitHubUserProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((MainApplication) getActivity().getApplication()).getApplicationComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        unbinder = ButterKnife.bind(this, view);

        // pull to refresh
        pullToRefreshLayout.setOnRefreshListener(this);
        pullToRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        pullToRefreshLayout.canChildScrollUp();

        /* bind the view and load data from Realm */
        gitHubUserProfilePresenter.bind(this, gitHubUser.getLogin(), false);
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
        unbinder.unbind();
        gitHubUserProfilePresenter.unBind();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        gitHubUserProfilePresenter.unSubscribe();
        super.onDestroy();
    }

    public void setGitHubUser(GitHubUser gitHubUser) {
        this.gitHubUser = gitHubUser;
    }

    public void setData(GitHubUserProfile gitHubUserProfile) {
        this.gitHubUserProfile = gitHubUserProfile;
        loadData();
    }

    public void loadData() {
        if (gitHubUserProfile.getName().contains("default")) {
            errorMessage.setVisibility(VISIBLE);
            profileIcon.setVisibility(GONE);
            profileName.setVisibility(GONE);
            profileEmail.setVisibility(GONE);
        } else {
            errorMessage.setVisibility(GONE);
            profileIcon.setVisibility(VISIBLE);
            profileName.setVisibility(VISIBLE);
            profileEmail.setVisibility(VISIBLE);
            Picasso.with(getActivity())
                    .load(gitHubUser.getAvatar_url())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(profileIcon);
            profileName.setText(String.format("%s%s", getActivity().getString(R.string.name), gitHubUserProfile.getName()));
            profileEmail.setText(String.format("%s%s", getActivity().getString(R.string.email), gitHubUserProfile.getEmail()));
        }
    }

    public void showSnackBar() {
        Snackbar.make(pullToRefreshLayout, "Error loading data!", Snackbar.LENGTH_LONG)
                .setAction("RETRY", view -> {
                    startRefreshAnimation();
                    gitHubUserProfilePresenter.getGitHubUserProfile(gitHubUser.getLogin(), true);
                }).show();
    }

    @Override
    public void onRefresh() {
        /* load fresh data */
        gitHubUserProfilePresenter.getGitHubUserProfile(gitHubUser.getLogin(), true);
    }
}