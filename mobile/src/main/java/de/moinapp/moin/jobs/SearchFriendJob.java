package de.moinapp.moin.jobs;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;
import de.moinapp.moin.MoinApplication;
import de.moinapp.moin.api.MoinClient;
import de.moinapp.moin.api.MoinService;
import de.moinapp.moin.api.User;
import de.moinapp.moin.auth.AccountGeneral;
import de.moinapp.moin.db.Friend;
import de.moinapp.moin.db.FriendDao;
import de.moinapp.moin.events.UserFoundEvent;
import de.moinapp.moin.events.UserNotFoundEvent;
import retrofit.RetrofitError;

/**
 * Created by jhbruhn on 03.08.14.
 */
public class SearchFriendJob extends Job {

    private String username;

    public SearchFriendJob(String username) {
        super(new Params(10).requireNetwork());
        this.username = username;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        AccountManager accountManager = AccountManager.get(MoinApplication.getMoinApplication());
        Account[] accounts = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        if (accounts.length == 0) return;

        String authToken = accountManager.blockingGetAuthToken(accounts[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, true);

        MoinService moin = MoinClient.getMoinService(MoinApplication.getMoinApplication());

        User user = null;

        try {
            user = moin.getUser(username, authToken);
        } catch (RetrofitError e) {
            if (e.getResponse().getStatus() == 404) {
                EventBus.getDefault().post(new UserNotFoundEvent());
                return;
            }
        }

        FriendDao friendDao = MoinApplication.getMoinApplication().getDaoSession().getFriendDao();
        if (friendDao.queryBuilder().where(FriendDao.Properties.Uuid.eq(user.id)).count() == 0) {
            Friend friend = new Friend();
            friend.setEmail(user.email_hash);
            friend.setUuid(user.id);
            friend.setUsername(user.username);
            friendDao.insertOrReplace(friend);
        }
        EventBus.getDefault().post(new UserFoundEvent());

    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
