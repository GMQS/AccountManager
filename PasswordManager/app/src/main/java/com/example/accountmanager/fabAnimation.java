package com.example.accountmanager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public class fabAnimation {

    /** view animation defines **/
    public interface InternalVisibilityChangedListener {
        void onShown();
    }
    private static final int SHOW_HIDE_ANIM_DURATION = 300;
    private static final Interpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR =
            new LinearOutSlowInInterpolator();

    public static void show(final View view, final InternalVisibilityChangedListener listener) {
        if (view.getVisibility() == View.VISIBLE) return;

        view.animate().cancel();

        view.setAlpha(0f);
        view.setScaleY(0f);
        view.setScaleX(0f);

        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(SHOW_HIDE_ANIM_DURATION)
                .setInterpolator(LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(listener!= null) listener.onShown();
                        super.onAnimationEnd(animation);
                    }
                });

    }
}
