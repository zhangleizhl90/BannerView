package me.zhanglei.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.lang.ref.WeakReference;

/**
 * Banner View
 * Created by zhanglei on 2016/9/17 0017.
 */
public class BannerView extends View {

    private static final String TAG = BannerView.class.getSimpleName();

    private BaseAdapter mAdapter;
    private int mCurrentIndex = -1;

    private OnItemClickListener mOnItemClickListener;

    private DrawHelper mDrawHelper;
    private AnimatorHelper mAnimatorHelper;
    private ScrollHelper mScrollHelper;
    private TouchHelper mTouchHelper;

    public BannerView(Context context) {
        super(context);
        init();
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDrawHelper = new DrawHelper();
        mAnimatorHelper = new AnimatorHelper();
        mScrollHelper = new ScrollHelper();
        mTouchHelper = new TouchHelper();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int maxHeight = 0;
        for (int i = 0; i < mAdapter.getCount(); ++i) { // find max height
            Bitmap bitmap = mAdapter.getItemAt(i);
            int height = (int) (bitmap.getHeight() * (getMeasuredWidth() / (double) bitmap.getWidth()));
            maxHeight = maxHeight < height ? height : maxHeight;
        }

        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mDrawHelper.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mScrollHelper.stopAutoScroll();
                break;
            case MotionEvent.ACTION_UP:
                mScrollHelper.startAutoScroll();
                break;
        }
        return mTouchHelper.onTouch(event);
    }

    public void setAdapter(BaseAdapter adapter) {
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.setBannerView(this);
        }
        mCurrentIndex = 0;

        requestLayout();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void startAutoScroll() {
        mScrollHelper.startAutoScroll();
    }

    private int getCount() {
        if (mAdapter == null) {
            return 0;
        }
        return mAdapter.getCount();
    }

    private void update() {
        mCurrentIndex = 0;
        mScrollHelper.reset();
        mScrollHelper.startAutoScroll();
        requestLayout();
    }

    abstract static public class  BaseAdapter {

        private BannerView mBannerView;

        void setBannerView(BannerView bannerView) {
            mBannerView = bannerView;
        }

        abstract public int getCount();

        abstract public Bitmap getItemAt(int index);
        final public void notifyDataSetChanged() {
            mBannerView.update();
        }
    }


    private class DrawHelper {
        private int mOffset;
        private int mFirstIndex;
        private int mSecondIndex;

        private Paint mPaint;
        private Paint mIndicatorPaint;

        private int mIndicatorRadius;
        private int mIndicatorDiameter;
        private int mIndicatorBorder;
        private int mIndicatorPadding;
        private int mIndicatorMarginBottom;

        DrawHelper() {
            mPaint = new Paint();
            mOffset = 0;

            mIndicatorRadius = getResources().getDimensionPixelSize(R.dimen.indicator_radius);
            mIndicatorDiameter = 2 * mIndicatorRadius;
            mIndicatorBorder = getResources().getDimensionPixelSize(R.dimen.indicator_border);
            mIndicatorPadding = getResources().getDimensionPixelSize(R.dimen.indicator_padding);
            mIndicatorMarginBottom = getResources().getDimensionPixelSize(R.dimen.indicator_margin_bottom);
        }

        void update(int offset, int firstIndex, int secondIndex) {
            mOffset = offset;
            mFirstIndex = firstIndex;
            mSecondIndex = secondIndex;
            invalidate();
        }

        void draw(Canvas canvas) {
            drawImages(canvas);
            drawIndicator(canvas);
        }

        private void drawImages(Canvas canvas) {
            if (mAdapter.getCount() <= 0) {
                return;
            }

            Bitmap bitmap = mAdapter.getItemAt(mFirstIndex);
            int offset = mOffset;
            if (bitmap != null) {
                Rect srcRect = getSrcRect(bitmap);
                Rect destRect = getDestRect(bitmap, offset);
                canvas.drawBitmap(bitmap, srcRect, destRect, mPaint);
                offset += getWidth();
            }

            bitmap = mAdapter.getItemAt(mSecondIndex);
            if (bitmap != null) {
                Rect srcRect = getSrcRect(bitmap);
                Rect destRect = getDestRect(bitmap, offset);
                canvas.drawBitmap(bitmap, srcRect, destRect, mPaint);
            }
        }

        private void drawIndicator(Canvas canvas) {
            if (canvas == null) {
                return;
            }

            if (mAdapter == null || mAdapter.getCount() <= 0) {
                return;
            }

            if (mIndicatorPaint == null) {
                mIndicatorPaint = new Paint();
                mIndicatorPaint.setColor(Color.WHITE);
                mIndicatorPaint.setStrokeWidth(mIndicatorBorder);
                mIndicatorPaint.setAntiAlias(true);
            }

            int count = mAdapter.getCount();
            int width = getWidth();
            int height = getHeight();

            int firstCX = (width - mIndicatorDiameter * count - mIndicatorPadding * (count - 1)) / 2 + mIndicatorRadius;
            int firstCY = height - mIndicatorMarginBottom - mIndicatorRadius;

            for (int i = 0; i < count; ++i) {
                mIndicatorPaint.setStyle(mCurrentIndex == i ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);

                canvas.drawCircle(firstCX, firstCY, mIndicatorRadius, mIndicatorPaint);

                firstCX += mIndicatorPadding + mIndicatorDiameter;
            }
        }

        private Rect getSrcRect(Bitmap bitmap) {
            Rect rect = new Rect();
            rect.top = 0;
            rect.left = 0;
            rect.right = rect.left + bitmap.getWidth();
            rect.bottom = rect.top + bitmap.getHeight();
            return rect;
        }

        private Rect getDestRect(Bitmap bitmap, int offset) {
            Rect rect = new Rect();
            rect.left = offset;
            rect.top = 0;
            rect.right = rect.left + getWidth();
            int height = (int) (bitmap.getHeight() * getWidth() / (double) bitmap.getWidth());
            rect.bottom = rect.top + height;
            return rect;
        }
    }

    private class AnimatorHelper {
        ValueAnimator mAnimator;

        void startAnimator(final int firstIndex, final int secondIndex, final int direction) {
            if (mAnimator != null) {
                mAnimator.cancel();
            }

            if (ScrollHelper.LEFT_TO_RIGHT == direction) {
                mAnimator = ValueAnimator.ofInt(0, -getWidth());
            } else if (ScrollHelper.RIGHT_TO_LEFT == direction) {
                mAnimator = ValueAnimator.ofInt(-getWidth(), 0);
            }
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int offset = (int) animation.getAnimatedValue();
                    if (ScrollHelper.LEFT_TO_RIGHT == direction) {
                        mDrawHelper.update(offset, firstIndex, secondIndex);
                    } else if (ScrollHelper.RIGHT_TO_LEFT == direction) {
                        mDrawHelper.update(offset, secondIndex, firstIndex);
                    }
                }
            });
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.setDuration(200).start();
        }

        void stopAnimator() {
            if (mAnimator != null) {
                mAnimator.cancel();
            }
        }
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

    private class ScrollHelper {

        static final int LEFT_TO_RIGHT = 1;
        static final int RIGHT_TO_LEFT = 2;

        private int mFromIndex;
        private int mToIndex;
        private int mDirection;

        void scrollToIndex(int index, int direction) {
            scrollFromTo(mCurrentIndex, index, direction);
        }

        void scrollFromTo(int from, int to, int direction) {
            mFromIndex = from;
            mToIndex = to;
            mDirection = direction;
            scroll();
        }

        private void scroll() {
            if (LEFT_TO_RIGHT == mDirection) {
                int toIndex = (mFromIndex + 1) % getCount();
                mAnimatorHelper.startAnimator(mFromIndex, toIndex, LEFT_TO_RIGHT);
                ++mFromIndex;
            } else if (RIGHT_TO_LEFT == mDirection) {
                int toIndex = (mFromIndex - 1 + getCount()) % getCount();
                mAnimatorHelper.startAnimator(mFromIndex, toIndex, RIGHT_TO_LEFT);
                --mFromIndex;
            }
        }

        private static final int MESSAGE_SCROLL = 0;
        private AutoScrollHandler mAutoScrollHandler;

        void startAutoScroll() {
            if (getCount() <= 1) {
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
            if (getCount() <= 1) {
                return;
            }
            int newIndex = (mCurrentIndex + 1) % getCount();
            Log.d(TAG, "scroll to next: " + newIndex);
            mScrollHelper.scrollToIndex(newIndex, ScrollHelper.LEFT_TO_RIGHT);
            mCurrentIndex = newIndex;
        }

        void scrollToPreview() {
            if (getCount() <= 1) {
                return;
            }
            Log.d(TAG, "scroll to preview");
            int newIndex = (mCurrentIndex - 1 + getCount()) % getCount();
            mScrollHelper.scrollToIndex(newIndex, ScrollHelper.RIGHT_TO_LEFT);
            mCurrentIndex = newIndex;
        }

        void reset() {
            mFromIndex = 0;
            mToIndex = 0;
            mDirection = 0;
        }
    }

    private class TouchHelper {

        private GestureDetector mGestureDetector;

        TouchHelper() {
            mGestureDetector = new GestureDetector(getContext(), mOnGestureListener);
        }

        boolean onTouch(MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }

        private GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                Log.d(TAG, "onDown");
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d(TAG, "onSingleTapConfirmed");
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mCurrentIndex);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.d(TAG, "onScroll");
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d(TAG, "onFling: " + velocityX + ", " + velocityY);
                float tempX = Math.abs(velocityX);
                float tempY = Math.abs(velocityY);

                if (tempY > tempX) {
                    return false;
                } else {
                    if (velocityX > 0) {
                        mScrollHelper.scrollToPreview();
                    } else {
                        mScrollHelper.scrollToNext();
                    }
                    return true;
                }
            }
        };
    }

    public interface OnItemClickListener {
        void onItemClick(int index);
    }
}
