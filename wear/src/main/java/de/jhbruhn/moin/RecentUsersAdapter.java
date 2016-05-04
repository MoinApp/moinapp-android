package de.jhbruhn.moin;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.jhbruhn.moin.data.User;

/**
 * Created by Jan-Henrik on 23.12.2014.
 */
public class RecentUsersAdapter extends WearableListView.Adapter {
    private List<User> mDataset;
    private final LayoutInflater mInflater;

    public RecentUsersAdapter(Context context, List<User> dataset) {
        mInflater = LayoutInflater.from(context);
        mDataset = dataset;
    }

    public void setUsers(List<User> recents) {
        this.mDataset = recents;
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView textView;
        private ImageView imageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.name);
            imageView = (ImageView) itemView.findViewById(R.id.circle);
        }
    }


    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.item_user, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        User user = mDataset.get(position);

        ItemViewHolder itemHolder = (ItemViewHolder) holder;

        itemHolder.textView.setText(user.name);

        if(user.avatar != null) {
            itemHolder.imageView.setImageBitmap(user.avatar);
        } else {
            itemHolder.imageView.setImageResource(R.drawable.default_avatar);
        }

        holder.itemView.setTag(user);
    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}