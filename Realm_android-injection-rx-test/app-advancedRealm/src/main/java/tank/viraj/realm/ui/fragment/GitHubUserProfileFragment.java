package tank.viraj.realm.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import tank.viraj.realm.MainApplication;
import tank.viraj.realm.R;
import tank.viraj.realm.model.GitHubUserProfile;
import tank.viraj.realm.presenter.GitHubUserProfilePresenter;

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

    @BindView(R.id.button_name)
    Button buttonName;

    @BindView(R.id.edit_name)
    EditText editName;

    @BindView(R.id.button_id)
    Button buttonId;

    @BindView(R.id.edit_id)
    EditText editId;

    @BindView(R.id.refresh_profile)
    SwipeRefreshLayout pullToRefreshLayout;

    private String login;
    private String avatarUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainApplication) getActivity().getApplication()).getApplicationComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        ButterKnife.bind(this, view);

        // pull to refresh
        pullToRefreshLayout.setOnRefreshListener(this);
        pullToRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        pullToRefreshLayout.canChildScrollUp();

        buttonId.setOnClickListener(view1 -> {
            try {
                gitHubUserProfilePresenter.editUserId(login, Integer
                        .parseInt(editId.getText().toString()));
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Enter a valid value", Toast.LENGTH_SHORT).show();
            }
        });

        buttonName.setOnClickListener(view1 -> {
            gitHubUserProfilePresenter.editUserName(login, editName.getText().toString());
        });

        /* bind the view and load data from Realm */
        gitHubUserProfilePresenter.bind(this, login);
        gitHubUserProfilePresenter.loadGitHubUserProfile(login, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        gitHubUserProfilePresenter.unBind();
        super.onDestroyView();
    }

    public void setGitHubUserData(String login, String avatarUrl) {
        this.login = login;
        this.avatarUrl = avatarUrl;
    }

    public void setData(GitHubUserProfile gitHubUserProfile) {
        Picasso.with(GitHubUserProfileFragment.this.getActivity())
                .load(avatarUrl)
                .into(profileIcon);
        profileName.setText(String.format("%s%s", GitHubUserProfileFragment.this.getActivity().getString(R.string.name), gitHubUserProfile.getName()));
        profileEmail.setText(String.format("%s%s", GitHubUserProfileFragment.this.getActivity().getString(R.string.email), gitHubUserProfile.getEmail()));
    }

    @Override
    public void onRefresh() {
        /* load fresh data */
        gitHubUserProfilePresenter.loadGitHubUserProfile(login, true);
    }
}