package com.vibal.utilities.ui.viewPager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class CashBoxViewPager extends ViewPager {
    private static final boolean PAGING_ENABLED = true;

    public CashBoxViewPager(@NonNull Context context) {
        super(context);
    }

    public CashBoxViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return PAGING_ENABLED && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return PAGING_ENABLED && super.onInterceptTouchEvent(event);
    }
}
