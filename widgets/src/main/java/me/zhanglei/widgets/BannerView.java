package me.zhanglei.widgets;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Banner View
 * Created by zhanglei on 2016/9/17 0017.
 */
public class BannerView extends View {

    private static final String TAG = BannerView.class.getSimpleName();

    private List<Bitmap> mBitmapList = new ArrayList<>();
    private int mCurrentIndex = -1;

    private OnItemClickListener mOnItemClickListener;

    private DrawHelper mDrawHelper;
    private AnimatorHelper mAnimatorHelper;
    private ScrollHelper mScrollHelper;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mDrawHelper = new DrawHelper();
        mAnimatorHelper = new AnimatorHelper();
        mScrollHelper = new ScrollHelper();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int maxHeight = 0;
        for (Bitmap bitmap : mBitmapList) { // find max height
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

//    private boolean mFakingDragging = false;
//    private VelocityTracker mVelocityTracker;
//    private float mLastMotionX;
//    private float mLastMotionY;
//    private float mInitialMotionX;
//    private float mInitialMotionY;
//    private int mActivePointerId;
//    private boolean mIsBeingDragged;
//    private float mMaximumVelocity;
//
//    @Override
//    public boolean onTouchEvent(MotionEvent ev){
//        if (mFakingDragging) {
//            return true;
//        }
//
//        if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
//            return false;
//        }
//
//        if (mBitmapList == null || mBitmapList.isEmpty()) {
//            return false;
//        }
//
//        if (mVelocityTracker == null) {
//            mVelocityTracker = VelocityTracker.obtain();
//        }
//        mVelocityTracker.addMovement(ev);
//
//        final int action = ev.getAction();
//        boolean needsInvalidate = false;
//
//        switch (action & MotionEventCompat.ACTION_MASK) {
//            case MotionEvent.ACTION_DOWN: {
//                mScroller.abortAnimation();
//                mPopulatedPending = false;
//                populate();
//
//                mLastMotionX = mInitialMotionX = ev.getX();
//                mLastMotionY = mInitialMotionY = ev.getY();
//                mActivePointerId = ev.getPointerId(0);
//                break;
//            }
//            case MotionEvent.ACTION_MOVE:
//                if (!mIsBeingDragged) {
//                    final int pointerIndex = ev.findPointerIndex(mActivePointerId);
//                    if (pointerIndex == -1) {
//                        needsInvalidate = resetTouch();
//                        break;
//                    }
//
//                    final float x = ev.getX(pointerIndex);
//                    final float xDiff = Math.abs(x - mLastMotionX);
//                    final float y = ev.getY(pointerIndex);
//                    final float yDiff = Math.abs(y - mLastMotionY);
//
//                    if (xDiff > mTouchSlop && xDiff > yDiff) {
//                        mIsBeingDragged = true;
//                        requestParentDisallowInterceptTouchEvent(true);
//                        mLastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX + mTouchSlop :
//                                mInitialMotionX - mTouchSlop;
//                        mLastMotionY = y;
//                        setScrollState(SCROLL_STATE_DRAGGING);
//                        setScrollinfCacheEnable(true);
//
//                        ViewParent parent = getParent();
//                        if (parent != null) {
//                            parent.requestDisallowInterceptTouchEvent(true);
//                        }
//                    }
//                }
//                if (mIsBeingDragged) {
//                    final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
//                    final float x = ev.getX(activePointerIndex);
//                    needsInvalidate |= performDrag(x);
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                if (mIsBeingDragged) {
//                    final VelocityTracker velocityTracker = mVelocityTracker;
//                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
//                    int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(velocityTracker,
//                            mActivePointerId);
//                    mPopulatePending = true;
//                    final int width = getClientWidth();
//                    final int scrollX = getScrollX();
//                    final ItemInfo ii = infoForCurrentScrollPosition();
//                    final float marginOffset = (float) mPageMargin / width;
//                    final int currentPage = ii.position;
//                    final float pageOffset = (((float) scrollX / width) - ii.offset)
//                            / (ii.widthFactor + marginOffset);
//                    final int activiePointerIndex = ev.findPointerIndex(mActivePointerId);
//                    final float x = ev.getX(activiePointerIndex);
//                    final int totalDelta = (int) (x - mInitialMotionX);
//                    int nextPage = determineTargetPage(currentPage, pageOffset, initialVelocity,
//                            totalDelta);
//                    setCurrentItemInternal(nextpage, true, true, initialVelocity);
//
//                    needsInvalidate = resetTouch();
//                }
//                break;
//            case MotionEvent.ACTION_CANCEL:
//                if (mIsBeingDragged) {
//                    scrollToItem(mCurItem, true, 0, false);
//                    needInvalidate = resetTouch();
//                }
//                break;;
//            case MotionEventCompat.ACTION_POINTER_DOWN: {
//                final int index = MotionEventCompat.getActionIndex(e);
//                final float x = ev.getX(index);
//                mLastMotionX = x;
//                mActivePointerId = ev.getPointerId(index);
//                break;
//            }
//            case MotionEventCompat.ACTION_POINTER_UP:
//                opnSecondaryPointerUp(ev);
//                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
//                break;
//        }
//        if (needsInvalidate) {
//            ViewCompat.postInvalidateOnAnimation(this);
//        }
//
//        return true;
//    }
//
//    private boolean resetTouch() {
//        boolean needsInvalidate;
//        mActivePointerId = INVALID_POINTER;
//        endDrag();
//        needsInvalidate = mLeftEdge.onRelease() | mRightEdge.onRelease();
//        return needsInvalidate;
//    }
//
//    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
//        final ViewParent parent = getParent();
//        if (parent != null) {
//            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
//        }
//    }
//
//    private boolean performDrag(float x) {
//        boolean needInvalidate = false;
//
//        final float deltaX = mLastMotionX - x;
//        mLastMotionX = x;
//
//        float oldScrollX = getScrollX();
//        float scrollX = oldScrollX + deltaX;
//        final int width = getClientWidth();
//
//        float leftBound = width * mFirstOffset;
//        float rightBound = width * mLastOffset;
//        boolean leftAbsolute = true;
//        boolean rightAbsolute = true;
//
//        final ItemInfo
//    }

    public void update(List<Bitmap> bitmapList) {
        mBitmapList.clear();

        if (bitmapList != null) {
            mBitmapList.addAll(bitmapList);
            mCurrentIndex = 0;
        }

        requestLayout();
    }

    private static final int MESSAGE_SCROLL = 0;

    private WeakReference<Handler> mHandlerRef;

    public void startAutoScroll() {
        int newIndex = (mCurrentIndex + 1) % mBitmapList.size();
        mScrollHelper.scrollToIndex(newIndex);
        mCurrentIndex = newIndex;

        setHandler();

        Handler handler = mHandlerRef.get();
        if (handler != null) {
            handler.removeMessages(MESSAGE_SCROLL);
            handler.sendEmptyMessageDelayed(MESSAGE_SCROLL, 2000);
        }
    }

    private void setHandler() {
        if (mHandlerRef == null || mHandlerRef.get() == null) {
            mHandlerRef = new WeakReference<Handler>(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    startAutoScroll();
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private class DrawHelper {
        private int mOffset;
        private int mFirstIndex;
        private int mSecondIndex;

        private Paint mPaint;

        public DrawHelper() {
            mPaint = new Paint();
            mOffset = 0;
        }

        public void update(int offset, int firstIndex, int secondIndex) {
            mOffset = offset;
            mFirstIndex = firstIndex;
            mSecondIndex = secondIndex;
            invalidate();
        }

        public void draw(Canvas canvas) {
            drawImages(canvas);
            drawIndicator(canvas);
        }

        private void drawImages(Canvas canvas) {
            Bitmap bitmap = getBitmap(mFirstIndex);
            int offset = mOffset;
            if (bitmap != null) {
                Rect srcRect = getSrcRect(bitmap);
                Rect destRect = getDestRect(bitmap, offset);
                canvas.drawBitmap(bitmap, srcRect, destRect, mPaint);
                offset += getWidth();
            }

            bitmap = getBitmap(mSecondIndex);
            if (bitmap != null) {
                Rect srcRect = getSrcRect(bitmap);
                Rect destRect = getDestRect(bitmap, offset);
                canvas.drawBitmap(bitmap, srcRect, destRect, mPaint);
            }
        }

        private void drawIndicator(Canvas canvas) {

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

        private Bitmap getBitmap(int index) {
            if (mBitmapList != null && index >= 0 && index < mBitmapList.size()) {
                return mBitmapList.get(index);
            }

            return null;
        }
    }

    private class AnimatorHelper {
        public void startAnimator(final int firstIndex, final int secondIndex) {
            ValueAnimator animator = ValueAnimator.ofInt(0, -getWidth());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int offset = (int) animation.getAnimatedValue();
                    mDrawHelper.update(offset, firstIndex, secondIndex);
                }
            });
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(200).start();
        }
    }

    private class ScrollHelper {
        static final int LEFT_TO_RIGHT = 0x1000;
        static final int RIGHT_TO_LEFT = 0x1001;

        private int mFromIndex;
        private int mToIndex;

        public void scrollToIndex(int index) {
            scrollFromTo(mCurrentIndex, index);
        }

        public void scrollFromTo(int from, int to) {
            mFromIndex = from;
            mToIndex = to;
            scroll();
        }

        private void scroll() {
            if (mFromIndex == mToIndex) {
                return;
            }

            if (mFromIndex > mToIndex) {
                mAnimatorHelper.startAnimator(mFromIndex, mFromIndex - 1);
                --mFromIndex;
            } else {
                mAnimatorHelper.startAnimator(mFromIndex, mFromIndex + 1);
                ++mFromIndex;
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int index);
    }
}
