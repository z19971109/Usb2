package com.dyx.voice.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * onFocusChanged监听控件焦点，配合AnimatorSet的动画放大缩小
 */
public class ScaleLayout extends RelativeLayout {
    public boolean isKeep = false;

    private float SCALE_MIN_VALUE = 1;
    private float SCALE_MAX_VALUE = 1.2f;

    public ScaleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setSPRING_MAX_VALUE(float SPRING_MAX_VALUE) {
        this.SCALE_MAX_VALUE = SPRING_MAX_VALUE;
    }

    public ScaleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleLayout(Context context) {
        super(context);
    }

    public void scaleOut() {

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY",
                SCALE_MIN_VALUE, SCALE_MAX_VALUE);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX",
                SCALE_MIN_VALUE, SCALE_MAX_VALUE);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY);
        animSet.setDuration(200);
        animSet.start();

    }

    public void scaleIn() {
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY",
                SCALE_MAX_VALUE, SCALE_MIN_VALUE);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX",
                SCALE_MAX_VALUE, SCALE_MIN_VALUE);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY);
        animSet.setDuration(200);
        animSet.start();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (isKeep)
            return;
        if (gainFocus) {
            scaleOut();
        } else {
            scaleIn();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

}