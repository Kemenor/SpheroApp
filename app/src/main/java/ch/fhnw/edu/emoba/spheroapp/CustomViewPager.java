package ch.fhnw.edu.emoba.spheroapp;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * disables touch scrolling on touch page
 * source: https://stackoverflow.com/questions/32597759/how-do-i-disable-swiping-on-fragmentstatepageradapter
 */
public class CustomViewPager extends ViewPager {

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch(getCurrentItem()){
            case 1:
                return false;
            default:
                return super.onInterceptTouchEvent(event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(getCurrentItem()){
            case 1:
                return false;
            default:
                return super.onTouchEvent(event);
        }
    }
}
