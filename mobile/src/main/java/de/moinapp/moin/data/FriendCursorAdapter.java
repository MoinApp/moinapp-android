package de.moinapp.moin.data;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.moinapp.moin.R;
import de.moinapp.moin.db.FriendDao;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class FriendCursorAdapter extends SimpleCursorAdapter {
    public FriendCursorAdapter(Context context, Cursor c, int flags) {
        super(context, R.layout.list_item_friend, c, new String[0], new int[0], flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();

        if (holder == null) {
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        String username = cursor.getString(cursor.getColumnIndexOrThrow(FriendDao.Properties.Username.columnName));
        String emailHash = cursor.getString(cursor.getColumnIndexOrThrow(FriendDao.Properties.Email.columnName));
        String gravatarUrl = "http://www.gravatar.com/avatar/" + emailHash + ".jpg?s=256";


        holder.usernameTextView.setText(username);
        if (!TextUtils.isEmpty(emailHash)) {
            Picasso.with(view.getContext()).load(gravatarUrl).fit().centerCrop().into(holder.avaterImageView);
        }
    }

    public class ViewHolder {

        @InjectView(R.id.list_item_friend_imageview)
        ImageView avaterImageView;

        @InjectView(R.id.list_item_friend_username)
        TextView usernameTextView;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
