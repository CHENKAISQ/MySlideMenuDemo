package ck.com.slidemenudemo;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * 通过继承系统已有的布局，目的是为了让它帮我们实现onMeasure，
 * 一般我们会选择FrameLayout来继承，因为FrameLayout最轻量级
 */

public class SlideMenu extends FrameLayout {
    ViewDragHelper dragHelper;
    private View menu;
    private View main;

    FloatEvaluator floatEval = new FloatEvaluator();//浮点计算器对象
    ArgbEvaluator argbEval = new ArgbEvaluator();//颜色计算器
    private int maxLeft;//main界面的left的最大值

    public SlideMenu(Context context) {
        this(context, null);
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        dragHelper = ViewDragHelper.create(this,callback);

    }

    /**
     * 当完成布局填充之后执行，也就是当前VIew在xml的布局文件的结束标签读取完毕后执行，
     * 因此此时此刻是知道自己有几个子View的，但是此时并没有进行测量完，所以是获取不到宽高的
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        menu = getChildAt(0);
        main = getChildAt(1);
    }

    /**
     * 就是onMeasure执行完毕之后执行该方法，因此可以在该方法中获取到宽高
     * 执行顺序:构造->onFinishInflate->onMeasure->onSizeChanged->onLayout
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        maxLeft = (int) (getMeasuredWidth()*0.6f);
    }


    //    /**
//     * 我们通过继承系统已有的布局，就可以不用自己实现onMeasure了，因为已有的布局已经实现了
//     * @param widthMeasureSpec
//     * @param heightMeasureSpec
//     */
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        //自己来实现测量孩子
//        View menu = getChildAt(0);
//        View main = getChildAt(1);
//
//        measureChild(menu,widthMeasureSpec,heightMeasureSpec);
//        measureChild(main,widthMeasureSpec,heightMeasureSpec);
//    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //让ViewDragHelper帮助我们判断是否应该拦截
        boolean result = dragHelper.shouldInterceptTouchEvent(ev);

        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //让ViewDragHelper帮我们处理触摸事件
        dragHelper.processTouchEvent(event);

        return true;
    }

    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        /**
         * 是否捕获View的触摸事件
         * @param child   当前你所触摸的子View
         * @param pointerId     多点触摸的时候每一个触摸点的索引
         * @return  true就是捕获， false就是不捕获
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child==main || child==menu;
        }

        /**
         * 比较鸡肋的方法，从方法名来看好像是限制VIew水平拖拽范围的，然后并没有什么用，
         * 目前该方法的范围值被用来作为你是否想横向滑动的条件之一，如果返回值大于0，表示想
         * 强制水平滑动，反之不想强制水平滑动
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        /**
         * 修正View水平方向的位置
         * @param child  当前触摸的子View
         * @param left  表示ViewDragHelper帮我计算好的当前child最新的left值，left=child.getLeft()+dx
         * @param dx    表示本次移动的距离
         * @return  表示我们真正想让child的left变成的值
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //对main界面限制
            if(child==main){
                left = clampLeft(left);
            }
//            else if(child==menu){
//                left = 0;
//            }

            return left;
        }

        /**
         * 修正View垂直方向的位置
         * @param child  当前触摸的子View
         * @param top  表示ViewDragHelper帮我计算好的当前child最新的top值，top=child.getTop()+dy
         * @param dy    表示本次移动的距离
         * @return  表示我们真正想让child的top变成的值
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }

        /**
         * 当View位置改变之后调用
         * @param changedView   位置改变的View
         * @param left  改变的最新的left
         * @param top   改变的最新的top
         * @param dx    本次水平移动的距离
         * @param dy    本次垂直移动的距离
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

//            Log.e("tag","left: "+left  + "  dx: "+dx);

            //如果移动的是menu，让main进行伴随移动,but菜单不要动
            if(changedView==menu){
                //通过让manu固定在原点位置实现不动
                menu.layout(0,0,menu.getMeasuredWidth(),menu.getMeasuredHeight());

                int newLeft = main.getLeft()+dx;//main当前的left+本次移动的距离
                newLeft = clampLeft(newLeft);
                main.layout(newLeft,main.getTop(),newLeft+main.getMeasuredWidth()
                    ,main.getBottom());
            }


            //执行伴随动画
            //1.计算动画执行的百分比
            float fraction = main.getLeft()*1f/maxLeft;
            //2.根据百分比执行动画
            executeAnim(fraction);

            //3.回调接口的方法
            if(main.getLeft()==0){
                //回调关闭的方法
                if(listener!=null){
                    listener.onClose();
                }
            }else if(main.getLeft()==maxLeft){
                //回调打开的方法
                if(listener!=null){
                    listener.onOpen();
                }
            }
            //回调dragging
            if(listener!=null){
                listener.onDraging(fraction);
            }
        }

        /**
         * 当抬起手指的时候执行
         * @param releasedChild  你抬起的那个子View
         * @param xvel  x方向滑动的速度
         * @param yvel  y方向滑动的速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if(main.getLeft()>maxLeft/2){
                //说明应该打开
                openMenu();

                //scroller的写法是这样滴：
//                scroller.startScroll();
//                invalidate();
            }else {
                //说明应该关闭
                closeMenu();
            }
        }

    };

    /**
     * 根据百分比执行伴随动画
     * @param fraction
     */
    private void executeAnim(float fraction) {
        //1.让main缩放
        //fraction:0 -> 1
        //scale   :1 -> 0.8

//        float scale = 1f + (0.8f-1f)*fraction;
        float scale = floatEval.evaluate(fraction,1f,0.8f);
        main.setScaleY(scale);
        main.setScaleX(scale);
        //翻转
//        main.setTranslationX(floatEval.evaluate(fraction,0,200));
//        main.setRotationY(floatEval.evaluate(fraction,0,90));
//        menu.setRotationY(floatEval.evaluate(fraction,-90,0));

        //2.让menu执行缩放以及平移动画
        menu.setScaleY(floatEval.evaluate(fraction,0.3f,1f));
        menu.setScaleX(floatEval.evaluate(fraction,0.3f,1f));
        menu.setTranslationX(floatEval.evaluate(fraction,-menu.getMeasuredWidth()/2,0));

        //3.给整个SlideMenu的背景图片添加颜色遮罩
        if(getBackground()!=null){
            //根据百分比计算一个渐变的颜色
            int color = (int) argbEval.evaluate(fraction,Color.BLACK,Color.TRANSPARENT);
            getBackground().setColorFilter(color, PorterDuff.Mode.SRC_OVER);
        }

    }

    /**
     * 打开菜单
     */
    public void openMenu() {
        dragHelper.smoothSlideViewTo(main,maxLeft,0);
        //需要刷新一下,相当于invalide，但是是能够兼容低版本的做法
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    /**
     * 关闭菜单
     */
    public void closeMenu() {
        dragHelper.smoothSlideViewTo(main,0,0);
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    Scroller scroller = null;

    @Override
    public void computeScroll() {
        super.computeScroll();
        //scroller的写法是这样滴：
//        if(scroller.computeScrollOffset()){
//            scrollTo(scroller.getCurrX(),scroller.getCurrY());
//            invalidate();
//        }

        //ViewDragHelper的写法是这样的：
        if(dragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
        }
    }

    /**
     * 修正left的值
     * @param newLeft
     * @return
     */
    private int clampLeft(int newLeft) {
        if(newLeft>maxLeft){
            newLeft = maxLeft;
        }else if(newLeft<0){
            newLeft = 0;
        }
        return newLeft;
    }

    private OnSlideChangeListener listener;
    public void setOnSlideChangeListener(OnSlideChangeListener listener){
        this.listener = listener;
    }

    public interface OnSlideChangeListener{
        void onOpen();
        void onClose();
        void onDraging(float fraction);
    }

}
