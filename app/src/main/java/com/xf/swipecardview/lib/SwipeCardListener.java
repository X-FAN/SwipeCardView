package com.xf.swipecardview.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by X-FAN on 2016/10/19.
 * 处理view的滑动
 */

abstract class SwipeCardListener implements View.OnTouchListener {

    private final float mOutDistance;//定义滑动多少距离后,触发view从界面左面离开动作

    private int mWidth;//view的宽度
    private float mInitX;//view初始的x坐标
    private float mTouchDownX;//按下时的手指x坐标
    private float mRecordX;//记录移动后view的x坐标


    private View mView;
    private GestureDetectorCompat mGestureDetector;


    SwipeCardListener(View view, int initX) {
        mView = view;
        mInitX = initX;
        mWidth = view.getWidth();
        mOutDistance = mWidth / 4;
        mGestureDetector = new GestureDetectorCompat(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onClick(mView);
                return super.onSingleTapUp(e);
            }
        });
    }


    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        if (mView.getTag() == null || !(boolean) mView.getTag()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = event.getRawX();
                return true;
            case MotionEvent.ACTION_MOVE:
                float d = event.getRawX() - mTouchDownX;
                if (Math.abs(d) > 0) {
                    mTouchDownX = event.getRawX();
                    mRecordX += d;
                    mView.setX(mInitX + mRecordX);//移动View
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (mRecordX < 0 && Math.abs(mRecordX) > mOutDistance) {
                    mView.animate().x(-mWidth).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (mView != null) {
                                mView.setTag(false);
                                mView.setOnTouchListener(null);
                                mView.clearAnimation();
                                leftOut(mView);
                                mView = null;
                            }
                        }
                    }).start();//滑出父view的范围
                } else {
                    mView.animate().x(mInitX).start();//让View回滚到初始位置
                }
                mRecordX = 0;
                break;
        }
        return false;
    }

    abstract void leftOut(View view);

    abstract void onClick(View view);
}
