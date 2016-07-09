package tank.viraj.realm.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tank.viraj.realm.R;
import tank.viraj.realm.model.GitHubUser;

/**
 * Adapter for RecyclerView
 * Created by Viraj Tank, 18-06-2016.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.GitHubViewHolder> {
    private Context context;
    private List<GitHubUser> gitHubUserList;
    private OnItemClickListener mItemClickListener;

    public MainAdapter(Context context) {
        this.context = context;
        gitHubUserList = new ArrayList<>();
    }

    @Override
    public GitHubViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.item_layout, viewGroup, false);
        return new GitHubViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GitHubViewHolder gitHubViewHolder, int position) {
        gitHubViewHolder.vUserLogin.setText(gitHubUserList.get(position).getLogin());
        gitHubViewHolder.vUserId.setText(String.format(context.getString(R.string.userId),
                gitHubUserList.get(position).getId()));
        Picasso.with(context)
                .load(gitHubUserList.get(position).getAvatar_url())
                .resize(48, 48)
                .centerCrop()
                .into(gitHubViewHolder.vUserIcon);
    }

    @Override
    public int getItemCount() {
        return gitHubUserList.size();
    }

    public void setDataList(List<GitHubUser> gitHubUserList) {
        this.gitHubUserList.clear();
        this.gitHubUserList.addAll(gitHubUserList);
        notifyDataSetChanged();
    }

    public void reset() {
        this.gitHubUserList.clear();
    }

    /* viewHolder */
    class GitHubViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView vUserIcon;
        TextView vUserLogin;
        TextView vUserId;

        GitHubViewHolder(View v) {
            super(v);
            vUserIcon = (ImageView) v.findViewById(R.id.user_icon);
            vUserLogin = (TextView) v.findViewById(R.id.user_name);
            vUserId = (TextView) v.findViewById(R.id.user_type);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, gitHubUserList.get(getAdapterPosition()));
            }
        }
    }

    /* onClick listener */
    public interface OnItemClickListener {
        void onItemClick(View v, GitHubUser gitHubUser);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mItemClickListener = onItemClickListener;
    }
}