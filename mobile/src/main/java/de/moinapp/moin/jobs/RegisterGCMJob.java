package de.moinapp.moin.jobs;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.moinapp.moin.MoinApplication;
import de.moinapp.moin.api.GCMID;
import de.moinapp.moin.api.MoinClient;
import de.moinapp.moin.api.MoinService;
import de.moinapp.moin.auth.AccountGeneral;

/**
 * Created by jhbruhn on 03.08.14.
 */
public class RegisterGCMJob extends Job {

    private String gcmId;

    public RegisterGCMJob(String gcmId) {
        super(new Params(1).persist().requireNetwork());

        this.gcmId = gcmId;
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
        moin.addGCMId(new GCMID(this.gcmId), authToken);
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
