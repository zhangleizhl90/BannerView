package me.zhanglei.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import me.zhanglei.widgets.helpers.AnimatorHelper;
import me.zhanglei.widgets.helpers.DrawHelper;
import me.zhanglei.widgets.helpers.ScrollHelper;
import me.zhanglei.widgets.helpers.TouchHelper;

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
        mDrawHelper = new DrawHelper(getContext(), this);
        mAnimatorHelper = new AnimatorHelper(getContext(), this);
        mScrollHelper = new ScrollHelper(this);
        mTouchHelper = new TouchHelper(getContext(), this);
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

    public DrawHelper getDrawHelper() {
        return mDrawHelper;
    }

    public AnimatorHelper getAnimatorHelper() {
        return mAnimatorHelper;
    }

    public ScrollHelper getScrollHelper() {
        return mScrollHelper;
    }

    public TouchHelper getTouchHelper() {
        return mTouchHelper;
    }

    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void startAutoScroll() {
        mScrollHelper.startAutoScroll();
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
        mScrollHelper.startAutoScroll();
        requestLayout();
    }

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
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
}
