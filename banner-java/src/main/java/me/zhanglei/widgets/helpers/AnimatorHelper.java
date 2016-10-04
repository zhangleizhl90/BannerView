package me.zhanglei.widgets.helpers;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.animation.LinearInterpolator;

import me.zhanglei.widgets.BannerView;

public class AnimatorHelper {

    private ValueAnimator mAnimator;
    private BannerView mBannerView;

    public AnimatorHelper(Context context, BannerView bannerView) {
        mBannerView = bannerView;
    }

    void startAnimator(final int firstIndex, final int secondIndex, final int direction) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        if (ScrollHelper.LEFT_TO_RIGHT == direction) {
            mAnimator = ValueAnimator.ofInt(0, -mBannerView.getWidth());
        } else if (ScrollHelper.RIGHT_TO_LEFT == direction) {
            mAnimator = ValueAnimator.ofInt(-mBannerView.getWidth(), 0);
        }
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int offset = (int) animation.getAnimatedValue();
                if (ScrollHelper.LEFT_TO_RIGHT == direction) {
                    mBannerView.getDrawHelper().update(offset, firstIndex, secondIndex);
                } else if (ScrollHelper.RIGHT_TO_LEFT == direction) {
                    mBannerView.getDrawHelper().update(offset, secondIndex, firstIndex);
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