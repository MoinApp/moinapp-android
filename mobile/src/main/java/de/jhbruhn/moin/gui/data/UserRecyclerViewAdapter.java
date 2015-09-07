package de.jhbruhn.moin.gui.data;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Bind;
import de.jhbruhn.moin.R;
import de.jhbruhn.moin.api.GravatarApi;
import de.jhbruhn.moin.data.User;

/**
 * Created by Jan-Henrik on 20.12.2014.
 */
public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.ViewHolder> {

    public interface UserRecyclerViewClickListener {

        public void onUserClick(User user, int position, View view);
    }

    private List<User> mUsers;
    private final UserRecyclerViewClickListener mClickListener;
    private final Picasso mPicasso;

    public UserRecyclerViewAdapter(List<User> users, Picasso picasso, UserRecyclerViewClickListener clickListener) {
        this.mUsers = users;
        this.mPicasso = picasso;
        this.mClickListener = clickListener;
    }


    @Override
    public UserRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        v.setClickable(true);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserRecyclerViewAdapter.ViewHolder holder, final int i) {
        final User user = mUsers.get(i);

        holder.mUsernameTextView.setText(user.username);

        mPicasso.cancelRequest(holder.mAvatarImageView);
        if(user.email_hash != null && !user.email_hash.isEmpty()) {
            String url = GravatarApi.getGravatarURL(user.email_hash);
            try {
                mPicasso.load(url).error(R.drawable.default_avatar).into(holder.mAvatarImageView);
            } catch (NullPointerException e) { // We have to catch some weird, inexplicable nullpointer here.
                holder.mAvatarImageView.setImageResource(R.drawable.default_avatar);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onUserClick(user, i, view);
            }
        });
        holder.itemView.setTag(user);
    }

    public void setUsers(List<User> users) {
        this.mUsers = users;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_user_avatar_image)
        public ImageView mAvatarImageView;
        @Bind(R.id.item_user_username_text)
        public TextView mUsernameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
