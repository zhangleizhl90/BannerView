package me.zhanglei.widgets.helpers;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import me.zhanglei.widgets.BannerView;

public class ScrollHelper {

    private static final String TAG = ScrollHelper.class.getSimpleName();

    static final int LEFT_TO_RIGHT = 1;
    static final int RIGHT_TO_LEFT = 2;

    private int mFromIndex;
    private int mToIndex;
    private int mDirection;

    private BannerView mBannerView;

    public ScrollHelper(BannerView bannerView) {
        mBannerView = bannerView;
    }

    private void scrollToIndex(int index, int direction) {
        scrollFromTo(mBannerView.getCurrentIndex(), index, direction);
    }

    private void scrollFromTo(int from, int to, int direction) {
        mFromIndex = from;
        mToIndex = to;
        mDirection = direction;
        scroll();
    }

    private void scroll() {
        if (LEFT_TO_RIGHT == mDirection) {
            int toIndex = (mFromIndex + 1) % mBannerView.getCount();
            mBannerView.getAnimatorHelper().startAnimator(mFromIndex, toIndex, LEFT_TO_RIGHT);
            ++mFromIndex;
        } else if (RIGHT_TO_LEFT == mDirection) {
            int toIndex = (mFromIndex - 1 + mBannerView.getCount()) % mBannerView.getCount();
            mBannerView.getAnimatorHelper().startAnimator(mFromIndex, toIndex, RIGHT_TO_LEFT);
            --mFromIndex;
        }
    }

    private static final int MESSAGE_SCROLL = 0;
    private AutoScrollHandler mAutoScrollHandler;

    public void startAutoScroll() {
        if (mBannerView.getCount() <= 1) {
            return;
        }

        if (mAutoScrollHandler == null) {
            mAutoScrollHandler = new AutoScrollHandler(this);
        }

        mAutoScrollHandler.removeMessages(MESSAGE_SCROLL); // 防止多次发送消息
        mAutoScrollHandler.sendEmptyMessageDelayed(MESSAGE_SCROLL, 2000);
    }

    void stopAutoScroll() {
        if (mAutoScrollHandler != null) {
            mAutoScrollHandler.removeMessages(MESSAGE_SCROLL);
        }
    }

    void scrollToNext() {
        if (mBannerView.getCount() <= 1) {
            return;
        }
        int newIndex = (mBannerView.getCurrentIndex() + 1) % mBannerView.getCount();
        Log.d(TAG, "scroll to next: " + newIndex);
        mBannerView.getScrollHelper().scrollToIndex(newIndex, ScrollHelper.LEFT_TO_RIGHT);
        mBannerView.setCurrentIndex(newIndex);
    }

    void scrollToPreview() {
        if (mBannerView.getCount() <= 1) {
            return;
        }
        Log.d(TAG, "scroll to preview");
        int newIndex = (mBannerView.getCurrentIndex() - 1 + mBannerView.getCount()) % mBannerView.getCount();
        mBannerView.getScrollHelper().scrollToIndex(newIndex, ScrollHelper.RIGHT_TO_LEFT);
        mBannerView.setCurrentIndex(newIndex);
    }

    public void reset() {
        mFromIndex = 0;
        mToIndex = 0;
        mDirection = 0;
    }

    private static class AutoScrollHandler extends Handler {

        private WeakReference<ScrollHelper> mBannerViewRef;

        AutoScrollHandler(ScrollHelper scrollHelper) {
            mBannerViewRef = new WeakReference<>(scrollHelper);
        }

        @Override
        public void handleMessage(Message msg) {
            ScrollHelper scrollHelper = mBannerViewRef.get();
            if (scrollHelper != null) {
                scrollHelper.scrollToNext();
                scrollHelper.startAutoScroll();
            }
        }
    }
}