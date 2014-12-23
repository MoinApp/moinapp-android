package de.jhbruhn.moin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Jan-Henrik on 23.12.2014.
 */
public class RecentUsersWearableListItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {

    private ImageView mCircle;
    private TextView mName;

    private final float mFadedTextAlpha;

    public RecentUsersWearableListItemLayout(Context context) {
        this(context, null);
    }

    public RecentUsersWearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentUsersWearableListItemLayout(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);

        mFadedTextAlpha = 75 / 100f;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCircle = (ImageView) findViewById(R.id.circle);
        mName = (TextView) findViewById(R.id.name);
    }

    @Override
    public void onCenterPosition(boolean b) {
        mName.setAlpha(1f);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
        mName.setAlpha(mFadedTextAlpha);
    }
}