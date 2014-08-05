package de.moinapp.moin.views;

import android.content.Context;
import android.util.AttributeSet;

import me.grantland.widget.AutofitTextView;

/**
 * Created by jhbruhn on 03.08.14.
 */
public class SquareTextView extends AutofitTextView {


    public SquareTextView(Context context) {
        super(context);
    }

    public SquareTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }


}