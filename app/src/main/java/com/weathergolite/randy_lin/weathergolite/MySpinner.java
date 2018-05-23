package com.weathergolite.randy_lin.weathergolite;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.Spinner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MySpinner extends android.support.v7.widget.AppCompatSpinner {
    public MySpinner(Context context) {
        super(context);
    }

    public MySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MySpinner(Context context, AttributeSet attrs, int defStyle, int mode) {
        super(context, attrs, defStyle, mode);
    }

    public MySpinner(Context context, int mode) {
        super(context, mode);
    }

    @Override
    public boolean performClick() {
        boolean bClicked = super.performClick();

        try {
            Field mPopupField = Spinner.class.getDeclaredField("mPopup");
            mPopupField.setAccessible(true);
            ListPopupWindow pop = (ListPopupWindow) mPopupField.get(this);
            DisplayMetrics monitorsize = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(monitorsize);
            pop.setHeight(monitorsize.heightPixels / 3);
            ListView listview = pop.getListView();
            Field mScrollCacheField = View.class.getDeclaredField("mScrollCache");
            mScrollCacheField.setAccessible(true);
            Object mScrollCache = mScrollCacheField.get(listview);
            Field scrollBarField = mScrollCache.getClass().getDeclaredField("scrollBar");
            scrollBarField.setAccessible(true);
            Object scrollBar = scrollBarField.get(mScrollCache);
            Method method = scrollBar.getClass().getDeclaredMethod("setVerticalThumbDrawable", Drawable.class);
            method.setAccessible(true);
            method.invoke(scrollBar, getResources().getDrawable(R.drawable.scrollbar_style));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bClicked;
    }
}