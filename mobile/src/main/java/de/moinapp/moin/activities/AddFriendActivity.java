package de.moinapp.moin.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.andreabaccega.widget.FormEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.moinapp.moin.MoinApplication;
import de.moinapp.moin.R;
import de.moinapp.moin.events.UserFoundEvent;
import de.moinapp.moin.events.UserNotFoundEvent;
import de.moinapp.moin.jobs.SearchFriendJob;


public class AddFriendActivity extends Activity {

    @InjectView(R.id.add_friend_username)
    FormEditText mUsernameText;

    @InjectView(R.id.add_friend_submit)
    Button mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        ButterKnife.inject(this);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.add_friend_submit)
    public void searchFriend() {
        if (!mUsernameText.testValidity()) return;

        mSubmitButton.setEnabled(false);

        String username = mUsernameText.getText().toString();

        MoinApplication.getMoinApplication().getJobManager().addJob(new SearchFriendJob(username));
    }

    public void onEventMainThread(UserNotFoundEvent e) {
        mSubmitButton.setEnabled(true);
        mUsernameText.setError(getString(R.string.add_friend_error_not_found));
    }

    public void onEventMainThread(UserFoundEvent e) {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_friend, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
