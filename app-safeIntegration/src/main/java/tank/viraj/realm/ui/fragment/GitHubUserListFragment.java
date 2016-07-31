package tank.viraj.realm.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tank.viraj.realm.MainApplication;
import tank.viraj.realm.R;
import tank.viraj.realm.adapter.MainAdapter;
import tank.viraj.realm.model.GitHubUser;
import tank.viraj.realm.presenter.GitHubUserPresenter;
import tank.viraj.realm.ui.activity.GitHubUserProfileActivity;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class GitHubUserListFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {
    @Inject
    GitHubUserPresenter gitHubUserPresenter;

    @Inject
    MainAdapter mainAdapter;

    @BindView(R.id.mainRecyclerView)
    RecyclerView mainRecyclerView;

    @BindView(R.id.refresh_list)
    SwipeRefreshLayout pullToRefreshLayout;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((MainApplication) getActivity().getApplication()).getApplicationComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        unbinder = ButterKnife.bind(this, view);

        mainAdapter.setOnItemClickListener((v, gitHubUser) -> {
            Intent switchToUserProfile = new Intent(GitHubUserListFragment.this.getActivity(), GitHubUserProfileActivity.class);
            switchToUserProfile.putExtra(GitHubUserListFragment.this.getActivity().getString(R.string.github_user_key), gitHubUser);
            GitHubUserListFragment.this.getActivity().startActivity(switchToUserProfile);
        });

        // pullToRefresh
        pullToRefreshLayout.setOnRefreshListener(this);
        pullToRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        pullToRefreshLayout.canChildScrollUp();

        // Recycler view
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mainRecyclerView.setLayoutManager(llm);
        mainRecyclerView.setHasFixedSize(true);
        mainRecyclerView.setAdapter(mainAdapter);

        mainRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (llm.findFirstCompletelyVisibleItemPosition() == 0) {
                    pullToRefreshLayout.setEnabled(true);
                } else {
                    pullToRefreshLayout.setEnabled(false);
                }
            }
        });

        /* bind the view and load data from Realm or Retrofit2 */
        gitHubUserPresenter.bind(this, false);
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
                gitHubUserPresenter.clearGitHubUserListFromRealm();
                Toast.makeText(getActivity(), R.string.clear_user_list, Toast.LENGTH_SHORT).show();
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
        gitHubUserPresenter.unBind();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        gitHubUserPresenter.unSubscribeHotSubscription();
        mainAdapter.reset();
        super.onDestroy();
    }

    public void setDataList(List<GitHubUser> gitHubUserList) {
        mainAdapter.setDataList(gitHubUserList);
    }

    public void showSnackBar() {
        Snackbar.make(pullToRefreshLayout, "No internet connection!", Snackbar.LENGTH_LONG)
                .setAction("RETRY", view -> {
                    startRefreshAnimation();
                    gitHubUserPresenter.getGitHubUsers(true);
                }).show();
    }

    @Override
    public void onRefresh() {
        /* load fresh data, when pullToRefresh is called */
        gitHubUserPresenter.getGitHubUsers(true);
    }
}