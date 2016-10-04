package me.zhanglei.widgets.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import me.zhanglei.widgets.BannerView;
import me.zhanglei.widgets.R;

public class DrawHelper {

    private BannerView mBannerView;

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

    public DrawHelper(Context context, BannerView bannerView) {
        mPaint = new Paint();
        mOffset = 0;

        mBannerView = bannerView;

        mIndicatorRadius = context.getResources().getDimensionPixelSize(R.dimen.indicator_radius);
        mIndicatorDiameter = 2 * mIndicatorRadius;
        mIndicatorBorder = context.getResources().getDimensionPixelSize(R.dimen.indicator_border);
        mIndicatorPadding = context.getResources().getDimensionPixelSize(R.dimen.indicator_padding);
        mIndicatorMarginBottom = context.getResources().getDimensionPixelSize(R.dimen.indicator_margin_bottom);
    }

    void update(int offset, int firstIndex, int secondIndex) {
        mOffset = offset;
        mFirstIndex = firstIndex;
        mSecondIndex = secondIndex;
        mBannerView.invalidate();
    }

    public void draw(Canvas canvas) {
        drawImages(canvas);
        drawIndicator(canvas);
    }

    private void drawImages(Canvas canvas) {
        if (mBannerView.getAdapter().getCount() <= 0) {
            return;
        }

        Bitmap bitmap = mBannerView.getAdapter().getItemAt(mFirstIndex);
        int offset = mOffset;
        if (bitmap != null) {
            Rect srcRect = getSrcRect(bitmap);
            Rect destRect = getDestRect(bitmap, offset);
            canvas.drawBitmap(bitmap, srcRect, destRect, mPaint);
            offset += mBannerView.getWidth();
        }

        bitmap = mBannerView.getAdapter().getItemAt(mSecondIndex);
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

        if (mBannerView.getAdapter() == null || mBannerView.getAdapter().getCount() <= 0) {
            return;
        }

        if (mIndicatorPaint == null) {
            mIndicatorPaint = new Paint();
            mIndicatorPaint.setColor(Color.WHITE);
            mIndicatorPaint.setStrokeWidth(mIndicatorBorder);
            mIndicatorPaint.setAntiAlias(true);
        }

        int count = mBannerView.getAdapter().getCount();
        int width = mBannerView.getWidth();
        int height = mBannerView.getHeight();

        int firstCX = (width - mIndicatorDiameter * count - mIndicatorPadding * (count - 1)) / 2 + mIndicatorRadius;
        int firstCY = height - mIndicatorMarginBottom - mIndicatorRadius;

        for (int i = 0; i < count; ++i) {
            mIndicatorPaint.setStyle(mBannerView.getCurrentIndex() == i ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);

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
        rect.right = rect.left + mBannerView.getWidth();
        int height = (int) (bitmap.getHeight() * mBannerView.getWidth() / (double) bitmap.getWidth());
        rect.bottom = rect.top + height;
        return rect;
    }
}