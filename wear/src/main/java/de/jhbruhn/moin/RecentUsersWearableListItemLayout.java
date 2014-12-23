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
    private final int mFadedCircleColor;
    private final int mChosenCircleColor;

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
        mFadedCircleColor = Color.GRAY;
        mChosenCircleColor = Color.WHITE;
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
        mCircle.getDrawable().setColorFilter(mChosenCircleColor, PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
        mCircle.getDrawable().setColorFilter(mFadedCircleColor, PorterDuff.Mode.MULTIPLY);
        mName.setAlpha(mFadedTextAlpha);
    }
}