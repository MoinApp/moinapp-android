package de.jhbruhn.moin.gui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import de.jhbruhn.moin.MoinApplication;

/**
 * Created by Jan-Henrik on 19.12.2014.
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MoinApplication.get(this).inject(this);

        setContentView(getLayoutId());

        ButterKnife.bind(this);
    }

    protected abstract int getLayoutId();
}
