package me.zhanglei.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by zhanglei on 2016/9/17 0017.
 */
public class BannerView extends View {

    private List<Bitmap> mBitmapList = new ArrayList<>();
    private int mCurrentIndex = -1;

    private DrawHelper mDrawHelper;

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mDrawHelper.draw(canvas);
    }

    private class DrawHelper {
        private int mOffset;
        private int mFirstIndex;
        private int mSecondIndex;

        private Paint mPaint;

        public DrawHelper() {
            mPaint = new Paint();
        }

        public void update(int offset, int firstIndex, int secondIndex) {
            mOffset = offset;
            mFirstIndex = firstIndex;
            mSecondIndex = secondIndex;
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
            int height = (int) (bitmap.getHeight() * bitmap.getWidth() / (double) getWidth());
            rect.bottom = rect.top +height;
            return rect;
        }

        private Bitmap getBitmap(int index) {
            if (mBitmapList != null && index >= 0 && index < mBitmapList.size()) {
                return mBitmapList.get(index);
            }

            return null;
        }
    }

    private class ScrollHelper {
        static final int LEFT_TO_RIGHT = 0x1000;
        static final int RIGHT_TO_LEFT = 0x1001;
    }
}
