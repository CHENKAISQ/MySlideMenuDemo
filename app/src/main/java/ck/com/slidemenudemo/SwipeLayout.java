package ck.com.slidemenudemo;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;


public class SwipeLayout extends FrameLayout {
    public boolean openItem = false;//记录条目滑动删除的状态 false条目关闭 true条目打开
    private View content;
    private View delete;
    private ViewDragHelper dragHelper;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        dragHelper = ViewDragHelper.create(this, callback);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        content = getChildAt(0);
        delete = getChildAt(1);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
        content.layout(0, 0, content.getMeasuredWidth(), content.getMeasuredHeight());
        int L = content.getMeasuredWidth();
        delete.layout(L, 0, L + delete.getMeasuredWidth(), delete.getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = dragHelper.shouldInterceptTouchEvent(ev);
        return result;
    }

    float downX, downY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                if(listener!=null){
                    listener.onTouchDown(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                //1.获取移动的dx和dy
                float dx = moveX - downX;
                float dy = moveY - downY;
                //2.判断谁大就是偏向于谁的方向
                if(Math.abs(dx)>Math.abs(dy) && ((!openItem && dx < 0) || openItem) && !MainActivity.stopChild){
                    //说明偏向于水平，我们就认为是想滑动条目，则请求listview不要拦截
                    //侧滑菜单为关闭状态, 条目为关闭状态且水平向左滑动或者条目为打开状态 请求父view不要拦截
                    requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        dragHelper.processTouchEvent(event);
        return true;
    }

    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        /**
         * 修正和限制移动的
         * @param child
         * @param left
         * @param dx
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == content) {
                //限制content
                if (left > 0) {
                    left = 0;
                } else if (left < -delete.getMeasuredWidth()) {
                    left = -delete.getMeasuredWidth();
                }
            } else if (child == delete) {
                //限制delete
                if (left > content.getMeasuredWidth()) {
                    left = content.getMeasuredWidth();
                } else if (left < (content.getMeasuredWidth() - delete.getMeasuredWidth())) {
                    left = (content.getMeasuredWidth() - delete.getMeasuredWidth());
                }
            }

            return left;
        }

        /**
         * 伴随移动
         * @param changedView
         * @param left
         * @param dx
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            //如果移动是content，那么让delete进行伴随移动
            if (changedView == content) {
                int newLeft = delete.getLeft() + dx;
                delete.layout(newLeft, 0, newLeft + delete.getMeasuredWidth(), delete.getBottom());
            } else if (changedView == delete) {
                //如果移动是delete，那么让content进行伴随移动
                int newLeft = content.getLeft() + dx;
                content.layout(newLeft, 0, newLeft + content.getMeasuredWidth(), content.getBottom());
            }

            //回调接口的方法
            if(content.getLeft()==0){
                //说明关闭
                if(listener!=null){
                    listener.onClose(SwipeLayout.this);
                }
            }else if(content.getLeft()==-delete.getMeasuredWidth()){
                //说明打开
                if(listener!=null){
                    listener.onOpen(SwipeLayout.this);
                }
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (content.getLeft() > -delete.getMeasuredWidth() / 2) {
                //关闭
                close();
            } else {
                //打开
                open();
            }

        }
    };

    /**
     * 打开
     */
    public void open() {
        dragHelper.smoothSlideViewTo(content, -delete.getMeasuredWidth(), 0);
        ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
    }

    /**
     * 关闭
     */
    public void close() {
        dragHelper.smoothSlideViewTo(content, 0, 0);
        ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
    }
    /**
     * 改变条目的打开或者关闭状态
     */
    public void changeState(boolean openItem) {
        this.openItem = openItem;
    }
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (dragHelper.continueSettling(true)) {
            //再次刷新
            ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
        }
    }

    private OnSwipeListener listener;
    public void setOnSwipeListener(OnSwipeListener listener){
        this.listener = listener;
    }
    public interface OnSwipeListener{
        void onOpen(SwipeLayout swipeLayout);
        void onClose(SwipeLayout swipeLayout);
        void onTouchDown(SwipeLayout swipeLayout);
    }
}
