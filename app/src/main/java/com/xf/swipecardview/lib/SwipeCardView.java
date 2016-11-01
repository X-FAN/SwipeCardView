package com.xf.swipecardview.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xf.swipecardview.R;

import java.util.List;

/**
 * Created by X-FAN on 2016/10/17.
 */

public class SwipeCardView extends ViewGroup {

    private int mInitX = 0;//最顶层view相对父view左上角x坐标
    private int mOffSet = 50;
    private int mRecordCount = 0;
    private int mRealOffset = 0;
    private int mDuration;
    private float mScale = 0.05f;
    private boolean mReLayout = false;//是否再次重新布局
    private BindData mBindData;

    private LayoutInflater mInflater;
    private View mTopView;//最顶上的View
    private View mRemovedView;
    private OnTopClickListener mOnTopClickListener;


    public SwipeCardView(Context context) {
        this(context, null);
    }

    public SwipeCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeCardView, defStyleAttr, 0);
        mRealOffset = a.getDimensionPixelSize(R.styleable.SwipeCardView_offset, 20);
        mDuration = a.getInteger(R.styleable.SwipeCardView_animatorDuration, 500);
        mScale = a.getFloat(R.styleable.SwipeCardView_scale, 0.05f);
        a.recycle();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        int defaultWidth = 200;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(defaultWidth, widthSize);
        } else {
            width = defaultWidth;
        }

        int defaultHeight = 200;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(defaultHeight, heightSize);
        } else {
            height = defaultHeight;
        }
        setMeasuredDimension(width, height);

        int count = getChildCount();
        for (int i = 0; i < count; i++) {//测量子view
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }

        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren(l, t, r, b);
    }

    private void layoutChildren(int left, final int top, int right, int bottom) {
        if (mReLayout) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View view = getChildAt(i);
                scaleUpChildView(view, count);
            }
            mReLayout = false;
            resetBottomView(count);
            addView(mRemovedView, 0);//将删除的View重新添加到最底端
            mRemovedView = null;
        } else {
            int count = getChildCount();
            mTopView = getChildAt(count - 1);
            mTopView.setTag(true);//开始默认可以滑动
            int width = mTopView.getMeasuredWidth();
            int height = mTopView.getMeasuredHeight();
            mOffSet = (int) (width * mScale / 2 + mRealOffset);//需要向左移动距离
            float totalWidth = width + mRealOffset * (count - 1);//整个子view加起来所占的宽度
            mInitX = (int) (getMeasuredWidth() - totalWidth) / 2;
            int initY = (getMeasuredHeight() - height) / 2;
            for (int i = 0; i < count; i++) {
                View view = getChildAt(i);
                view.layout(mInitX, initY, width + mInitX, height + initY);
                scaleChildView(view, count - 1 - i);
            }
        }
        setTopView();

    }

    /**
     * 配置顶层view
     */
    private void setTopView() {
        mTopView = getChildAt(getChildCount() - 1);//获取最上层的View
        if (mTopView != null) {
            mTopView.setOnTouchListener(new SwipeCardListener(mTopView, mInitX) {
                @Override
                void leftOut(View view) {
                    mRemovedView = view;
                    mReLayout = true;
                    removeView(view);
                }

                @Override
                void onClick(View view) {
                    if (mOnTopClickListener != null) {
                        mOnTopClickListener.onTopClickListener(view);
                    }
                }
            });

        }
    }

    /**
     * 给最底层的View重新配置合适的值
     *
     * @param count
     */
    private void resetBottomView(int count) {
        mRemovedView.setX(mInitX);
        mRemovedView.offsetLeftAndRight(count * mOffSet);
        mRemovedView.setScaleX(1 - count * mScale);
        mRemovedView.setScaleY(1 - count * mScale);
    }

    /**
     * 初始化SwipeCard
     *
     * @param layoutId
     * @param datas
     * @param <T>
     */
    public <T> void initSwipeCard(@LayoutRes int layoutId, List<T> datas) {
        int count = datas.size();
        for (int i = 0; i < count; i++) {//添加view并绑定数据
            View view = mInflater.inflate(layoutId, this, false);
            mBindData.bindData(view, datas.get(i));
            addView(view, 0);//添加到最低端
        }
    }


    public interface BindData<T> {
        void bindData(View view, T data);
    }

    /**
     * 缩放并平移子view
     */
    private void scaleChildView(View view, int index) {
        view.offsetLeftAndRight(mOffSet * index);
        view.setScaleX(1 - index * mScale);
        view.setScaleY(1 - index * mScale);
    }

    /**
     * 慢慢放到上层view的位置
     *
     * @param view
     */
    private void scaleUpChildView(final View view, final int count) {
        float scaleX = view.getScaleX();
        float scaleY = view.getScaleY();
        view.animate().scaleX(scaleX + mScale)
                .scaleY(scaleY + mScale)
                .x(view.getX() - mOffSet)
                .setDuration(mDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mRecordCount++;
                        if (count == mRecordCount) {
                            mTopView.setTag(true);//设置为可以滑动
                            mRecordCount = 0;
                        }
                    }
                })
                .start();
    }

    public <T> void setBindDataListener(BindData bindData) {
        mBindData = bindData;
    }


    public void setOnTopClickListener(OnTopClickListener onTopClickListener) {
        mOnTopClickListener = onTopClickListener;
    }

    public interface OnTopClickListener {
        void onTopClickListener(View view);
    }


}
