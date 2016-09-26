package me.zhanglei.widgets.kotlin

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Message
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

import java.lang.ref.WeakReference

/**
 * Banner View
 * Created by zhanglei on 2016/9/17 0017.
 */

val LEFT_TO_RIGHT = -1
val RIGHT_TO_LEFT = 1

val MESSAGE_SCROLL = 0

class BannerView : View {

    private var mAdapter: BaseAdapter? = null
    private var mCurrentIndex = -1

    private var mOnItemClickListener: (Int) -> Unit = {}

    private var mDrawHelper: DrawHelper? = null
    private var mAnimatorHelper: AnimatorHelper? = null
    private var mScrollHelper: ScrollHelper? = null
    private var mTouchHelper: TouchHelper? = null

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
        mDrawHelper = DrawHelper()
        mAnimatorHelper = AnimatorHelper()
        mScrollHelper = ScrollHelper()
        mTouchHelper = TouchHelper()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var maxHeight = 0
        for (i in 0..mAdapter!!.count - 1) { // find max height
            val bitmap = mAdapter!!.getItemAt(i)
            val height = (bitmap.height * (measuredWidth / bitmap.width.toDouble())).toInt()
            maxHeight = if (maxHeight < height) height else maxHeight
        }

        setMeasuredDimension(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.EXACTLY))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        mDrawHelper!!.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEventCompat.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> mScrollHelper!!.stopAutoScroll()
            MotionEvent.ACTION_UP -> mScrollHelper!!.startAutoScroll()
        }
        return mTouchHelper!!.onTouch(event)
    }

    fun setAdapter(adapter: BaseAdapter) {
        mAdapter = adapter
        if (mAdapter != null) {
            mAdapter!!.setBannerView(this)
        }
        mCurrentIndex = 0

        requestLayout()
    }

    fun setOnItemClickListener(onItemClickListener: (Int) -> Unit) {
        mOnItemClickListener = onItemClickListener
    }

    fun startAutoScroll() {
        mScrollHelper!!.startAutoScroll()
    }

    private val count: Int
        get() {
            if (mAdapter == null) {
                return 0
            }
            return mAdapter!!.count
        }

    private fun update() {
        mCurrentIndex = 0
        mScrollHelper!!.reset()
        mScrollHelper!!.startAutoScroll()
        requestLayout()
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


    private inner class DrawHelper internal constructor() {
        private var mOffset: Int = 0
        private var mFirstIndex: Int = 0
        private var mSecondIndex: Int = 0

        private val mPaint: Paint

        init {
            mPaint = Paint()
            mOffset = 0
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
            if (mAdapter!!.count <= 0) {
                return
            }

            var bitmap: Bitmap? = mAdapter!!.getItemAt(mFirstIndex)
            var offset = mOffset
            if (bitmap != null) {
                val srcRect = getSrcRect(bitmap)
                val destRect = getDestRect(bitmap, offset)
                canvas.drawBitmap(bitmap, srcRect, destRect, mPaint)
                offset += width
            }

            bitmap = mAdapter!!.getItemAt(mSecondIndex)
            val srcRect = getSrcRect(bitmap)
            val destRect = getDestRect(bitmap, offset)
            canvas.drawBitmap(bitmap, srcRect, destRect, mPaint)
        }

        private fun drawIndicator(canvas: Canvas) {

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

    private inner class AnimatorHelper {
        internal var mAnimator: ValueAnimator? = null

        internal fun startAnimator(firstIndex: Int, secondIndex: Int, direction: Int) {
            if (mAnimator != null) {
                mAnimator!!.cancel()
            }

            if (LEFT_TO_RIGHT == direction) {
                mAnimator = ValueAnimator.ofInt(0, -width)
            } else if (RIGHT_TO_LEFT == direction) {
                mAnimator = ValueAnimator.ofInt(-width, 0)
            }
            mAnimator!!.addUpdateListener { animation ->
                val offset = animation.animatedValue as Int
                if (LEFT_TO_RIGHT == direction) {
                    mDrawHelper!!.update(offset, firstIndex, secondIndex)
                } else if (RIGHT_TO_LEFT == direction) {
                    mDrawHelper!!.update(offset, secondIndex, firstIndex)
                }
            }
            mAnimator!!.interpolator = LinearInterpolator()
            mAnimator!!.setDuration(200).start()
        }

        internal fun stopAnimator() {
            if (mAnimator != null) {
                mAnimator!!.cancel()
            }
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

    private inner class ScrollHelper {

        private var mFromIndex: Int = 0
        private var mToIndex: Int = 0
        private var mDirection: Int = 0

        internal fun scrollToIndex(index: Int, direction: Int) {
            scrollFromTo(mCurrentIndex, index, direction)
        }

        internal fun scrollFromTo(from: Int, to: Int, direction: Int) {
            mFromIndex = from
            mToIndex = to
            mDirection = direction
            scroll()
        }

        private fun scroll() {
            if (LEFT_TO_RIGHT == mDirection) {
                val toIndex = (mFromIndex + 1) % count
                mAnimatorHelper!!.startAnimator(mFromIndex, toIndex, LEFT_TO_RIGHT)
                ++mFromIndex
            } else if (RIGHT_TO_LEFT == mDirection) {
                val toIndex = (mFromIndex - 1 + count) % count
                mAnimatorHelper!!.startAnimator(mFromIndex, toIndex, RIGHT_TO_LEFT)
                --mFromIndex
            }
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
            if (count <= 1) {
                return
            }
            val newIndex = (mCurrentIndex + 1) % count
            Log.d(TAG, "scroll to next: " + newIndex)
            mScrollHelper!!.scrollToIndex(newIndex, LEFT_TO_RIGHT)
            mCurrentIndex = newIndex
        }

        internal fun scrollToPreview() {
            if (count <= 1) {
                return
            }
            Log.d(TAG, "scroll to preview")
            val newIndex = (mCurrentIndex - 1 + count) % count
            mScrollHelper!!.scrollToIndex(newIndex, RIGHT_TO_LEFT)
            mCurrentIndex = newIndex
        }

        internal fun reset() {
            mFromIndex = 0
            mToIndex = 0
            mDirection = 0
        }
    }

    private inner class TouchHelper internal constructor() {

        private val mOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                Log.d(TAG, "onDown")
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                Log.d(TAG, "onSingleTapConfirmed")
                mOnItemClickListener(mCurrentIndex)
                return true
            }

            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                Log.d(TAG, "onScroll")
                return true
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                Log.d(TAG, "onFling: $velocityX, $velocityY")
                val tempX = Math.abs(velocityX)
                val tempY = Math.abs(velocityY)

                if (tempY > tempX) {
                    return false
                } else {
                    if (velocityX > 0) {
                        mScrollHelper!!.scrollToPreview()
                    } else {
                        mScrollHelper!!.scrollToNext()
                    }
                    return true
                }
            }
        }

        private val mGestureDetector: GestureDetector

        init {
            mGestureDetector = GestureDetector(context, mOnGestureListener)
        }

        internal fun onTouch(event: MotionEvent): Boolean {
            return mGestureDetector.onTouchEvent(event)
        }
    }

    companion object {
        private val TAG = BannerView::class.java.simpleName
    }
}
