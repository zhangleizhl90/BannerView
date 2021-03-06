package me.zhanglei.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.view.MotionEvent;
import android.view.VelocityTracker;
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
    private OnCurrentChangeListener mOnCurrentChangeListener;

    private DrawHelper mDrawHelper;
    private AnimationHelper mAnimationHelper;
    private ScrollHelper mScrollHelper;
    private TouchHelper mTouchHelper;

    private boolean mInAnimation = false;

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
        mAnimationHelper = new AnimationHelper();
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

    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnCurrentChangeListener(OnCurrentChangeListener onCurrentChangeListener) {
        mOnCurrentChangeListener = onCurrentChangeListener;
    }

    public void startAutoScroll() {
        mScrollHelper.startAutoScroll();
    }

    public void stopAutoScroll() {
        mScrollHelper.stopAutoScroll();
    }

    public int getCount() {
        if (mAdapter == null) {
            return 0;
        }
        return mAdapter.getCount();
    }

    private void update() {
        mCurrentIndex = 0;
        mScrollHelper.reset();
        requestLayout();
    }

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        int oldIndex = mCurrentIndex;
        mCurrentIndex = currentIndex;
        if (mOnCurrentChangeListener != null) {
            mOnCurrentChangeListener.onCurrentChanged(oldIndex, currentIndex);
        }
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

    public interface OnItemClickListener {
        void onItemClick(int index);
    }

    public interface OnCurrentChangeListener {
        void onCurrentChanged(int oldIndex, int newIndex);
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
                mIndicatorPaint.setStyle(getCurrentIndex() == i ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);

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

    private class ScrollHelper {

        static final int LEFT_TO_RIGHT = 1;
        static final int RIGHT_TO_LEFT = 2;

        private int mOffset = 0;

        private void scrollFromTo(int from, int to, int direction) {
            if (direction == LEFT_TO_RIGHT) {
                mAnimationHelper.startAnimator(from, to, -getWidth() + mOffset, 0);
            } else {
                mAnimationHelper.startAnimator(from, to, mOffset, -getWidth());
            }
            mOffset = 0;
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
            if (getCount() <= 1 || mInAnimation) {
                return;
            }
            int newIndex = (getCurrentIndex() + 1) % getCount();
            Log.d(TAG, "scroll to next: " + newIndex);
            mScrollHelper.scrollFromTo(getCurrentIndex(), newIndex, ScrollHelper.RIGHT_TO_LEFT);
            setCurrentIndex(newIndex);
        }

        void scrollToPreview() {
            if (getCount() <= 1 || mInAnimation) {
                return;
            }
            Log.d(TAG, "scroll to preview");
            int newIndex = (getCurrentIndex() - 1 + getCount()) % getCount();
            mScrollHelper.scrollFromTo(newIndex, getCurrentIndex(), ScrollHelper.LEFT_TO_RIGHT);
            setCurrentIndex(newIndex);
        }

        void reset() {
            mOffset = 0;
        }

        void scrollOffset(int distanceX) {
            mOffset = distanceX;

            if (mOffset >= getWidth()) {
                mOffset -= getWidth();
                --mCurrentIndex;
            } else if (mOffset <= -getWidth()) {
                mOffset += getWidth();
                ++mCurrentIndex;
            }

            int firstIndex;
            int secondIndex;
            int offset;
            if (mOffset > 0) {
                firstIndex = (mCurrentIndex - 1 + getCount()) % getCount();
                secondIndex = mCurrentIndex;
                offset = mOffset - getWidth();
            } else {
                firstIndex = mCurrentIndex;
                secondIndex = (mCurrentIndex + 1) % getCount();
                offset = mOffset;
            }
            mDrawHelper.update(offset, firstIndex, secondIndex);
        }

        void continueAutoScroll(boolean isFling, int velocityX) {

            if (isFling) {
                if (velocityX > 0) {
                    mScrollHelper.scrollToPreview();
                } else {
                    mScrollHelper.scrollToNext();
                }
                return;
            }

            if (mOffset == 0) {
                return;
            }

            int start;
            int end;
            int firstIndex;
            int secondIndex;
            int newCurrentIndex;
            if (mOffset > 0) {
                firstIndex = (mCurrentIndex - 1 + getCount()) % getCount();
                secondIndex = mCurrentIndex;
                if (Math.abs(mOffset) > getWidth() / 2) { // LEFT_TO_RIGHT
                    start = mOffset - getWidth();
                    end = 0;
                    newCurrentIndex = firstIndex;
                } else { // RIGHT_TO_LEFT
                    start = mOffset - getWidth();
                    end = -getWidth();
                    newCurrentIndex = secondIndex;
                }
            } else {
                firstIndex = mCurrentIndex;
                secondIndex = (mCurrentIndex + 1) % getCount();
                if (Math.abs(mOffset) > getWidth() / 2) { //
                    start = mOffset;
                    end = -getWidth();
                    newCurrentIndex = secondIndex;
                } else {
                    start = mOffset;
                    end = 0;
                    newCurrentIndex = firstIndex;
                }
            }
            mAnimationHelper.startAnimator(firstIndex, secondIndex, start, end);
            setCurrentIndex(newCurrentIndex);
            mOffset = 0;
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

    private class AnimationHelper {

        private ValueAnimator mAnimator;

        void startAnimator(final int firstIndex, final int secondIndex, int start, final int end) {
            if (mAnimator != null) {
                mAnimator.cancel();
            }


            mAnimator = ValueAnimator.ofInt(start, end);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int offset = (int) animation.getAnimatedValue();
                    mDrawHelper.update(offset, firstIndex, secondIndex);
                }
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mInAnimation = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mInAnimation = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mInAnimation = false;
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

    private class TouchHelper {

//        private GestureDetector mGestureDetector;

        private VelocityTracker mVelocityTracker;
        private float mInitialMotionX;
        private float mInitialMotionY;
        private float mLastMotionX;
        private float mLastMotionY;
        private float mMinimumVelocity;
        private float mMaximumVelocity;
        private int mActivePointerId;
        private int mTouchSlop;

        private boolean mIsBeingDragged;

        TouchHelper() {
//            mGestureDetector = new GestureDetector(getContext(), mOnGestureListener);
            mTouchSlop = getResources().getDimensionPixelSize(R.dimen.touch_slop);
            mMinimumVelocity = getResources().getDimensionPixelSize(R.dimen.minimum_velocity);
            mMaximumVelocity = getResources().getDimensionPixelSize(R.dimen.maximum_velocity);
        }

        boolean onTouch(MotionEvent event) {

            if (mInAnimation) {
                return true;
            }

            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }

            mVelocityTracker.addMovement(event);

            switch (event.getAction() & MotionEventCompat.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mScrollHelper.stopAutoScroll();

                    mLastMotionX = mInitialMotionX = event.getX();
                    mLastMotionY = mInitialMotionY = event.getY();
                    mActivePointerId = event.getPointerId(0);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mIsBeingDragged) {
                        final int pointerIndex = event.findPointerIndex(mActivePointerId);
                        if (pointerIndex == -1) {
                            // reset();
                            break;
                        }
                        final float x = event.getX(pointerIndex);
                        final float xDiff = Math.abs(x - mLastMotionX);
                        final float y = event.getY(pointerIndex);
                        final float yDiff = Math.abs(y - mLastMotionY);

                        if (xDiff > mTouchSlop && xDiff > yDiff) {
                            mIsBeingDragged = true;
                            mLastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX + mTouchSlop :
                                    mInitialMotionX - mTouchSlop;
                            mLastMotionY = y;
                        }
                    }

                    if (mIsBeingDragged) {
                        final int activePointerIndex = event.findPointerIndex(mActivePointerId);
                        if (activePointerIndex != -1) {
                            final float x = event.getX(activePointerIndex);
                            mScrollHelper.scrollOffset((int) (x - mLastMotionX));
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP");
                    if (mIsBeingDragged) {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000);
                        int initialVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);
                        Log.d(TAG, "velocity: " + initialVelocity);
                        boolean isFling = Math.abs(initialVelocity) > mMinimumVelocity;
                        mScrollHelper.continueAutoScroll(isFling, initialVelocity);
                    } else {
                        if (getOnItemClickListener() != null) {
                            getOnItemClickListener().onItemClick(getCurrentIndex());
                        }
                    }
                    mScrollHelper.startAutoScroll();
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}
