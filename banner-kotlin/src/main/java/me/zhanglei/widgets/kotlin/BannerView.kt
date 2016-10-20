package me.zhanglei.widgets.kotlin

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.LinearInterpolator
import me.zhanglei.widgets.R
import java.lang.ref.WeakReference

/**
 * Banner View
 * Created by zhanglei on 2016/9/17 0017.
 */
class BannerView : View {

    var adapter: BaseAdapter? = null
        set(adapter) {
            field = adapter
            if (this.adapter != null) {
                this.adapter!!.setBannerView(this)
            }
            mCurrentIndex = 0

            requestLayout()
        }
    private var mCurrentIndex = -1

    var onItemClickListener: OnItemClickListener? = null
    private var mOnCurrentChangeListener: OnCurrentChangeListener? = null

    private val mDrawHelper = DrawHelper()
    private val mAnimationHelper = AnimationHelper()
    private val mScrollHelper = ScrollHelper()
    private val mTouchHelper = TouchHelper()

    private var mInAnimation = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var maxHeight = 0
        for (i in 0..adapter!!.count - 1) { // find max height
            val bitmap = adapter!!.getItemAt(i)
            val height = (bitmap.height * (measuredWidth / bitmap.width.toDouble())).toInt()
            maxHeight = if (maxHeight < height) height else maxHeight
        }

        setMeasuredDimension(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.EXACTLY))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        mDrawHelper.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mTouchHelper.onTouch(event)
    }

    fun setOnCurrentChangeListener(onCurrentChangeListener: OnCurrentChangeListener) {
        mOnCurrentChangeListener = onCurrentChangeListener
    }

    fun startAutoScroll() {
        mScrollHelper.startAutoScroll()
    }

    fun stopAutoScroll() {
        mScrollHelper.stopAutoScroll()
    }

    val count: Int
        get() {
            if (adapter == null) {
                return 0
            }
            return adapter!!.count
        }

    private fun update() {
        mCurrentIndex = 0
        mScrollHelper.reset()
        requestLayout()
    }

    var currentIndex: Int
        get() = mCurrentIndex
        set(currentIndex) {
            val oldIndex = mCurrentIndex
            mCurrentIndex = currentIndex
            if (mOnCurrentChangeListener != null) {
                mOnCurrentChangeListener!!.onCurrentChanged(oldIndex, currentIndex)
            }
        }

    abstract class BaseAdapter {

        private var mBannerView: BannerView? = null

        internal fun setBannerView(bannerView: BannerView) {
            mBannerView = bannerView
        }

        abstract val count: Int

        abstract fun getItemAt(index: Int): Bitmap

        fun notifyDataSetChanged() {
            mBannerView!!.update()
        }
    }

    interface OnItemClickListener {
        fun onItemClick(index: Int)
    }

    interface OnCurrentChangeListener {
        fun onCurrentChanged(oldIndex: Int, newIndex: Int)
    }

    private inner class DrawHelper internal constructor() {

        private var mOffset: Int = 0
        private var mFirstIndex: Int = 0
        private var mSecondIndex: Int = 0

        private val mPaint: Paint
        private var mIndicatorPaint: Paint? = null

        private val mIndicatorRadius: Int
        private val mIndicatorDiameter: Int
        private val mIndicatorBorder: Int
        private val mIndicatorPadding: Int
        private val mIndicatorMarginBottom: Int

        init {
            mPaint = Paint()
            mOffset = 0

            mIndicatorRadius = resources.getDimensionPixelSize(R.dimen.indicator_radius)
            mIndicatorDiameter = 2 * mIndicatorRadius
            mIndicatorBorder = resources.getDimensionPixelSize(R.dimen.indicator_border)
            mIndicatorPadding = resources.getDimensionPixelSize(R.dimen.indicator_padding)
            mIndicatorMarginBottom = resources.getDimensionPixelSize(R.dimen.indicator_margin_bottom)
        }

        internal fun update(offset: Int, firstIndex: Int, secondIndex: Int) {
            mOffset = offset
            mFirstIndex = firstIndex
            mSecondIndex = secondIndex
            invalidate()
        }

        internal fun draw(canvas: Canvas) {
            drawImages(canvas)
            drawIndicator(canvas)
        }

        private fun drawImages(canvas: Canvas) {
            if (adapter!!.count <= 0) {
                return
            }

            var bitmap: Bitmap? = adapter!!.getItemAt(mFirstIndex)
            var offset = mOffset
            if (bitmap != null) {
                val srcRect = getSrcRect(bitmap)
                val destRect = getDestRect(bitmap, offset)
                canvas.drawBitmap(bitmap, srcRect, destRect, mPaint)
                offset += width
            }

            bitmap = adapter!!.getItemAt(mSecondIndex)

            val srcRect = getSrcRect(bitmap)
            val destRect = getDestRect(bitmap, offset)
            canvas.drawBitmap(bitmap, srcRect, destRect, mPaint)
        }

        private fun drawIndicator(canvas: Canvas?) {
            if (canvas == null) {
                return
            }

            if (adapter == null || adapter!!.count <= 0) {
                return
            }

            if (mIndicatorPaint == null) {
                mIndicatorPaint = Paint()
                mIndicatorPaint!!.color = Color.WHITE
                mIndicatorPaint!!.strokeWidth = mIndicatorBorder.toFloat()
                mIndicatorPaint!!.isAntiAlias = true
            }

            val count = adapter!!.count
            val width = width
            val height = height

            var firstCX = (width - mIndicatorDiameter * count - mIndicatorPadding * (count - 1)) / 2 + mIndicatorRadius
            val firstCY = height - mIndicatorMarginBottom - mIndicatorRadius

            for (i in 0..count - 1) {
                mIndicatorPaint!!.style = if (currentIndex == i) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE

                canvas.drawCircle(firstCX.toFloat(), firstCY.toFloat(), mIndicatorRadius.toFloat(), mIndicatorPaint!!)

                firstCX += mIndicatorPadding + mIndicatorDiameter
            }
        }

        private fun getSrcRect(bitmap: Bitmap): Rect {
            val rect = Rect()
            rect.top = 0
            rect.left = 0
            rect.right = rect.left + bitmap.width
            rect.bottom = rect.top + bitmap.height
            return rect
        }

        private fun getDestRect(bitmap: Bitmap, offset: Int): Rect {
            val rect = Rect()
            rect.left = offset
            rect.top = 0
            rect.right = rect.left + width
            val height = (bitmap.height * width / bitmap.width.toDouble()).toInt()
            rect.bottom = rect.top + height
            return rect
        }
    }

    private inner class ScrollHelper {

        private var mOffset = 0

        private fun scrollFromTo(from: Int, to: Int, direction: Int) {
            if (direction == LEFT_TO_RIGHT) {
                mAnimationHelper.startAnimator(from, to, -width + mOffset, 0)
            } else {
                mAnimationHelper.startAnimator(from, to, mOffset, -width)
            }
            mOffset = 0
        }

        private var mAutoScrollHandler: AutoScrollHandler? = null

        internal fun startAutoScroll() {
            if (count <= 1) {
                return
            }

            if (mAutoScrollHandler == null) {
                mAutoScrollHandler = AutoScrollHandler(this)
            }

            mAutoScrollHandler!!.removeMessages(MESSAGE_SCROLL) // 防止多次发送消息
            mAutoScrollHandler!!.sendEmptyMessageDelayed(MESSAGE_SCROLL, 2000)
        }

        internal fun stopAutoScroll() {
            if (mAutoScrollHandler != null) {
                mAutoScrollHandler!!.removeMessages(MESSAGE_SCROLL)
            }
        }

        internal fun scrollToNext() {
            if (count <= 1 || mInAnimation) {
                return
            }
            val newIndex = (currentIndex + 1) % count
            Log.d(TAG, "scroll to next: " + newIndex)
            mScrollHelper.scrollFromTo(currentIndex, newIndex, RIGHT_TO_LEFT)
            currentIndex = newIndex
        }

        internal fun scrollToPreview() {
            if (count <= 1 || mInAnimation) {
                return
            }
            Log.d(TAG, "scroll to preview")
            val newIndex = (currentIndex - 1 + count) % count
            mScrollHelper.scrollFromTo(newIndex, currentIndex, LEFT_TO_RIGHT)
            currentIndex = newIndex
        }

        internal fun reset() {
            mOffset = 0
        }

        internal fun scrollOffset(distanceX: Int) {
            mOffset = distanceX

            if (mOffset >= width) {
                mOffset -= width
                --mCurrentIndex
            } else if (mOffset <= -width) {
                mOffset += width
                ++mCurrentIndex
            }

            val firstIndex: Int
            val secondIndex: Int
            val offset: Int
            if (mOffset > 0) {
                firstIndex = (mCurrentIndex - 1 + count) % count
                secondIndex = mCurrentIndex
                offset = mOffset - width
            } else {
                firstIndex = mCurrentIndex
                secondIndex = (mCurrentIndex + 1) % count
                offset = mOffset
            }
            mDrawHelper.update(offset, firstIndex, secondIndex)
        }

        internal fun continueAutoScroll(isFling: Boolean, velocityX: Int) {

            if (isFling) {
                if (velocityX > 0) {
                    mScrollHelper.scrollToPreview()
                } else {
                    mScrollHelper.scrollToNext()
                }
                return
            }

            if (mOffset == 0) {
                return
            }

            val start: Int
            val end: Int
            val firstIndex: Int
            val secondIndex: Int
            val newCurrentIndex: Int
            if (mOffset > 0) {
                firstIndex = (mCurrentIndex - 1 + count) % count
                secondIndex = mCurrentIndex
                if (Math.abs(mOffset) > width / 2) { // LEFT_TO_RIGHT
                    start = mOffset - width
                    end = 0
                    newCurrentIndex = firstIndex
                } else { // RIGHT_TO_LEFT
                    start = mOffset - width
                    end = -width
                    newCurrentIndex = secondIndex
                }
            } else {
                firstIndex = mCurrentIndex
                secondIndex = (mCurrentIndex + 1) % count
                if (Math.abs(mOffset) > width / 2) { //
                    start = mOffset
                    end = -width
                    newCurrentIndex = secondIndex
                } else {
                    start = mOffset
                    end = 0
                    newCurrentIndex = firstIndex
                }
            }
            mAnimationHelper.startAnimator(firstIndex, secondIndex, start, end)
            currentIndex = newCurrentIndex
            mOffset = 0
        }
    }

    private class AutoScrollHandler internal constructor(scrollHelper: ScrollHelper) : Handler() {

        private val mBannerViewRef: WeakReference<ScrollHelper>

        init {
            mBannerViewRef = WeakReference(scrollHelper)
        }

        override fun handleMessage(msg: Message) {
            val scrollHelper = mBannerViewRef.get()
            if (scrollHelper != null) {
                scrollHelper.scrollToNext()
                scrollHelper.startAutoScroll()
            }
        }
    }

    private inner class AnimationHelper {

        private var mAnimator: ValueAnimator? = null

        internal fun startAnimator(firstIndex: Int, secondIndex: Int, start: Int, end: Int) {
            if (mAnimator != null) {
                mAnimator!!.cancel()
            }

            mAnimator = ValueAnimator.ofInt(start, end)
            mAnimator!!.addUpdateListener { animation ->
                val offset = animation.animatedValue as Int
                mDrawHelper.update(offset, firstIndex, secondIndex)
            }
            mAnimator!!.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    mInAnimation = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    mInAnimation = false
                }

                override fun onAnimationCancel(animation: Animator) {
                    mInAnimation = false
                }

                override fun onAnimationRepeat(animation: Animator) {
                    mInAnimation = false
                }
            })
            mAnimator!!.interpolator = LinearInterpolator()
            mAnimator!!.setDuration(200).start()

        }

        internal fun stopAnimator() {
            if (mAnimator != null) {
                mAnimator!!.cancel()
            }
        }
    }

    private inner class TouchHelper internal constructor() {
        private var mVelocityTracker: VelocityTracker? = null
        private var mInitialMotionX: Float = 0.toFloat()
        private var mInitialMotionY: Float = 0.toFloat()
        private var mLastMotionX: Float = 0.toFloat()
        private var mLastMotionY: Float = 0.toFloat()
        private val mMinimumVelocity: Float
        private val mMaximumVelocity: Float
        private var mActivePointerId: Int = 0
        private val mTouchSlop: Int

        private var mIsBeingDragged: Boolean = false

        init {
            mTouchSlop = resources.getDimensionPixelSize(R.dimen.touch_slop)
            mMinimumVelocity = resources.getDimensionPixelSize(R.dimen.minimum_velocity).toFloat()
            mMaximumVelocity = resources.getDimensionPixelSize(R.dimen.maximum_velocity).toFloat()
        }

        internal fun onTouch(event: MotionEvent): Boolean {

            if (mInAnimation) {
                return true
            }

            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain()
            }

            mVelocityTracker!!.addMovement(event)

            when (event.action and MotionEventCompat.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    mScrollHelper.stopAutoScroll()

                    mLastMotionX = event.x
                    mInitialMotionX = event.x
                    mLastMotionY = event.y
                    mInitialMotionY = event.y
                    mActivePointerId = event.getPointerId(0)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!mIsBeingDragged) {
                        val pointerIndex = event.findPointerIndex(mActivePointerId)
                        if (pointerIndex == -1) {
                            // reset();
                        }
                        val x = event.getX(pointerIndex)
                        val xDiff = Math.abs(x - mLastMotionX)
                        val y = event.getY(pointerIndex)
                        val yDiff = Math.abs(y - mLastMotionY)

                        if (xDiff > mTouchSlop && xDiff > yDiff) {
                            mIsBeingDragged = true
                            mLastMotionX = if (x - mInitialMotionX > 0)
                                mInitialMotionX + mTouchSlop
                            else
                                mInitialMotionX - mTouchSlop
                            mLastMotionY = y
                        }
                    }

                    if (mIsBeingDragged) {
                        val activePointerIndex = event.findPointerIndex(mActivePointerId)
                        val x = event.getX(activePointerIndex)
                        mScrollHelper.scrollOffset((x - mLastMotionX).toInt())
                    }
                }
                MotionEvent.ACTION_UP -> {
                    Log.d(TAG, "ACTION_UP")
                    if (mIsBeingDragged) {
                        val velocityTracker = mVelocityTracker
                        velocityTracker!!.computeCurrentVelocity(1000)
                        val initialVelocity = velocityTracker.getXVelocity(mActivePointerId).toInt()
                        Log.d(TAG, "velocity: " + initialVelocity)
                        val isFling = Math.abs(initialVelocity) > mMinimumVelocity
                        mScrollHelper.continueAutoScroll(isFling, initialVelocity)
                    } else {
                        if (onItemClickListener != null) {
                            onItemClickListener!!.onItemClick(currentIndex)
                        }
                    }
                    mScrollHelper.startAutoScroll()
                }
                else -> {
                }
            }
            return true
        }
    }

    companion object {
        private val LEFT_TO_RIGHT = 1
        private val RIGHT_TO_LEFT = 2
        private val MESSAGE_SCROLL = 0
        private val TAG = BannerView::class.java.simpleName
    }
}
