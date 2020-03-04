package com.vibal.utilities.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class CashBoxViewPager extends ViewPager {
    private static boolean PAGING_ENABLED = false;

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
