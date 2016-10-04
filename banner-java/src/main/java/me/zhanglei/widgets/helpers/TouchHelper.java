package me.zhanglei.widgets.helpers;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;

import me.zhanglei.widgets.BannerView;

public class TouchHelper {

    private BannerView mBannerView;

    private GestureDetector mGestureDetector;

    public TouchHelper(Context context, BannerView bannerView) {
        mGestureDetector = new GestureDetector(context, mOnGestureListener);
        mBannerView = bannerView;
    }

    public boolean onTouch(MotionEvent event) {
        switch (event.getAction() & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mBannerView.getScrollHelper().stopAutoScroll();
                break;
            case MotionEvent.ACTION_UP:
                mBannerView.getScrollHelper().startAutoScroll();
                break;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    private GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mBannerView.getOnItemClickListener() != null) {
                mBannerView.getOnItemClickListener().onItemClick(mBannerView.getCurrentIndex());
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float tempX = Math.abs(velocityX);
            float tempY = Math.abs(velocityY);

            if (tempY > tempX) {
                return false;
            } else {
                if (velocityX > 0) {
                    mBannerView.getScrollHelper().scrollToPreview();
                } else {
                    mBannerView.getScrollHelper().scrollToNext();
                }
                return true;
            }
        }
    };
}