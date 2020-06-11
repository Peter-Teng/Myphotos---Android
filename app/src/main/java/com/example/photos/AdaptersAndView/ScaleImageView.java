package com.example.photos.AdaptersAndView;
/**
 * @author DHP
 *
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Matrix;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.example.photos.Activities.ImgActivity;

//实现图片放大、拖动查看、上下滑动结束aty的自定义View，继承自ImageView
public class ScaleImageView extends androidx.appcompat.widget.AppCompatImageView implements MyMovingListener
{
    //手势检测器
    private GestureDetector gestureDetector;
    //放大缩小相关图像矩阵
    private Matrix matrix = new Matrix();
    //图像的长宽
    private float imgWidth;
    private float imgHeight;
    //移动监听
    private MyMovingListener moveListener;
    //放大倍数
    private float scale;
    //底部工具栏
    private LinearLayout tools = null;

    public void setOnMovingListener(MyMovingListener listener)
    {
        moveListener=listener;
    }

    //设置当前的图像Bitmap
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        //大小为0 表示当前控件大小未测量  设置监听函数  在绘制前赋值
        if(getWidth()==0){
            ViewTreeObserver vto = getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
            {
                public boolean onPreDraw()
                {
                    initData();
                    //赋值结束后，移除该监听函数
                    ScaleImageView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
        }else {
            initData();
        }
    }


    //初始化相关数据
    private void initData() {
        matrix.set(getImageMatrix());
        float[] values=new float[9];
        matrix.getValues(values);
        imgWidth=getWidth()/values[Matrix.MSCALE_X];
        imgHeight=(getHeight()-values[Matrix.MTRANS_Y]*2)/values[Matrix.MSCALE_Y];
        scale=values[Matrix.MSCALE_X];
    }

    //手势监听
    private class  GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        private final MatrixTouchListener listener;

        public GestureListener(MatrixTouchListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            listener.onDoubleClick();
            return true;
        }
    }

    //构造函数
    public ScaleImageView(Context context,LinearLayout layout)
    {
        super(context);
        this.tools = layout;
        MatrixTouchListener mListener=new MatrixTouchListener();
        setOnTouchListener(mListener);
        gestureDetector = new GestureDetector(getContext(), new GestureListener(mListener));
        //隐藏或者显示底部工具栏
        if(tools.getVisibility()==VISIBLE)
            setBackgroundColor(Color.WHITE);
        else
            setBackgroundColor(Color.BLACK);
        setScaleType(ScaleType.FIT_CENTER);
    }

    //放大拖动相关，告诉ImageViewPager不要给我滑到下一张图片了
    @Override
    public void startDrag() {
        if(moveListener!=null) moveListener.startDrag();
    }

    //图片恢复正常大小，告诉ImageViewPager这个时候左右滑动可以移到别的图片
    @Override
    public void stopDrag() {
        if(moveListener!=null) moveListener.stopDrag();
    }

    //设置touchListerner，实现手势相关操作
    public class MatrixTouchListener implements OnTouchListener
    {
        //图片的各种模式
        private static final int DRAG = 1;
        private static final int ZOOM = 2;
        private static final int UNABLE = 3;
        //图片最大放大倍数
        private float maxScale = 6;
        //双击图片时的放大倍数
        private float doubleClickScale = 2;
        //图片当前处于什么模式
        private int mode = 0;
        //拖动开始的距离
        private float startDis;
        //图片放大矩阵
        private Matrix currentMatrix = new Matrix();
        private PointF startPoint = new PointF();
        //判断图片是否可以左右拖动
        boolean mLeftDragable;
        boolean mRightDragable;
        boolean mFirstMove=false;

        //重新点击事件方法
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch (event.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                    //手指按下，开始拖动
                    mode = DRAG;
                    startPoint.set(event.getX(),event.getY());//记录开始拖动的点
                    isMatrixEnable();
                    startDrag();
                    checkDragable();
                    break;
                case MotionEvent.ACTION_UP://手指抬起
                    exitAty(event);//计算用户是否为上下大幅度滑动，若是则调用该函数退出大图
                    toolsDeal();//隐藏或者显示工具栏
                case MotionEvent.ACTION_CANCEL://按下退出键
                    reSetMatrix();//图片恢复原来大小
                    stopDrag();
                    break;
                case MotionEvent.ACTION_MOVE://手指移动
                    if(mode == ZOOM)//继续放大图片
                    {
                        setZoomMatrix(event);
                    }
                    else if(mode == DRAG)//拖动图片
                    {
                        setDragMatrix(event);
                    }
                    else
                    {
                        stopDrag();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if(mode == UNABLE)
                        return true;
                    mode = ZOOM;
                    startDis = distance(event);
                    break;
                default:
                    break;
            }
            return gestureDetector.onTouchEvent(event);
        }

        //上下滑动退出大图详情相关函数
        private void exitAty(MotionEvent event)
        {
            if(!isZoomChanged())//假如图片不处于放大状态
            {
                float dy = Math.abs(event.getY() - startPoint.y);
                if(dy > 450f)//假如上滑幅度超过一定距离
                {
                    ((ImgActivity)getContext()).setResult(0);
                    ((ImgActivity)getContext()).finish();//退出ImgActivity
                }
            }
        }

        //显示或者隐藏工具栏调用函数
        private void toolsDeal()
        {
            if(tools != null && !isZoomChanged())
            {
                toggleTools();
            }
            else if(tools!=null && isZoomChanged())
            {
                tools.setVisibility(GONE);
            }
        }

        //检测图片是否被拖动到左右边界
        private void checkDragable() {
            mLeftDragable=true;
            mRightDragable=true;
            mFirstMove=true;
            float[] values=new float[9];
            getImageMatrix().getValues(values);
            //图片左边缘离开左边界，表示不可右移
            if(values[Matrix.MTRANS_X]>=0)
                mRightDragable=false;
            //图片右边缘离开右边界，表示不可左移
            if((imgWidth)*values[Matrix.MSCALE_X]+values[Matrix.MTRANS_X]<=getWidth()){
                mLeftDragable=false;
            }
        }

        //检测是否可以缩放
        private void isMatrixEnable() {
            //当加载出错时，不可缩放
            if(getScaleType()!=ScaleType.CENTER){
                setScaleType(ScaleType.MATRIX);
            }else {
                mode=UNABLE;//设置为不支持手势
            }
        }

        //显示或者隐藏工具栏
        private void toggleTools()
        {
            if (tools.getVisibility() == VISIBLE)
            {
                tools.setVisibility(INVISIBLE);
                setBackgroundColor(Color.BLACK);
            }
            else if (tools.getVisibility() == INVISIBLE)
            {
                tools.setVisibility(VISIBLE);
                setBackgroundColor(Color.WHITE);
            }
            else
                tools.setVisibility(VISIBLE);
        }

        //计算两次操作之间点的距离
        private float distance(MotionEvent event)
        {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        //放大缩小图片
        public void setDragMatrix(MotionEvent event) {
            if(isZoomChanged()){
                float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
                //避免和双击冲突,大于10f才算是拖动
                if(Math.sqrt(dx*dx+dy*dy)>10f){
                    startPoint.set(event.getX(), event.getY());
                    //在当前基础上移动
                    currentMatrix.set(getImageMatrix());
                    float[] values=new float[9];
                    currentMatrix.getValues(values);
                    dy=checkDyBound(values,dy);
                    dx=checkDxBound(values,dx,dy);

                    currentMatrix.postTranslate(dx, dy);
                    setImageMatrix(currentMatrix);
                }
            }else {
                stopDrag();
            }
        }


        private float checkDxBound(float[] values,float dx,float dy) {
            float width=getWidth();
            if(!mLeftDragable&&dx<0){
                //加入和y轴的对比，表示在监听到垂直方向的手势时不切换Item
                if(Math.abs(dx)*0.4f>Math.abs(dy)&&mFirstMove){
                    stopDrag();
                }
                return 0;
            }
            if(!mRightDragable&&dx>0){
                //加入和y轴的对比，表示在监听到垂直方向的手势时不切换Item
                if(Math.abs(dx)*0.4f>Math.abs(dy)&&mFirstMove){
                    stopDrag();
                }
                return 0;
            }
            mLeftDragable=true;
            mRightDragable=true;
            if(mFirstMove) mFirstMove=false;
            if(imgWidth*values[Matrix.MSCALE_X]<width){
                return 0;

            }
            if(values[Matrix.MTRANS_X]+dx>0){
                dx=-values[Matrix.MTRANS_X];
            }
            else if(values[Matrix.MTRANS_X]+dx<-(imgWidth*values[Matrix.MSCALE_X]-width)){
                dx=-(imgWidth*values[Matrix.MSCALE_X]-width)-values[Matrix.MTRANS_X];
            }
            return dx;
        }
        //设置放大缩小相关矩阵
        private void setZoomMatrix(MotionEvent event) {
            if (event.getPointerCount() < 2) return;
            float endDis = distance(event);
            if (endDis > 10f) {
                float scale = endDis / startDis;
                startDis = endDis;
                currentMatrix.set(getImageMatrix());
                float[] values = new float[9];
                currentMatrix.getValues(values);
                scale = checkMaxScale(scale, values);
                setImageMatrix(currentMatrix);
            }
        }
        //检测放大倍数，不能超过最大倍数
        private float checkMaxScale(float scale, float[] values)
        {
            if(scale*values[Matrix.MSCALE_X]>maxScale)
                scale=maxScale/values[Matrix.MSCALE_X];
            currentMatrix.postScale(scale, scale,getWidth()/2,getHeight()/2);
            return scale;
        }
        //重置放大缩小矩阵
        private void reSetMatrix()
        {
            if(checkRest())
            {
                currentMatrix.set(matrix);
                setImageMatrix(currentMatrix);
            }
        }

        private boolean checkRest()
        {
            float[] values=new float[9];
            getImageMatrix().getValues(values);
            float scale=values[Matrix.MSCALE_X];
            matrix.getValues(values);
            return scale<values[Matrix.MSCALE_X];
        }

        //双击事件，双击时放大两倍
        public void onDoubleClick()
        {
            float scale=isZoomChanged()?1:doubleClickScale;
            currentMatrix.set(matrix);
            currentMatrix.postScale(scale, scale,getWidth()/2,getHeight()/2);
            setImageMatrix(currentMatrix);
        }
        //判断当前图片是否被放大
        private boolean isZoomChanged() {
            float[] values=new float[9];
            getImageMatrix().getValues(values);
            float scale=values[Matrix.MSCALE_X];
            matrix.getValues(values);
            return scale!=values[Matrix.MSCALE_X];
        }


        private float checkDxBound(float[] values,float dx) {
            float width=getWidth();
            if(imgWidth*values[Matrix.MSCALE_X]<width)
                return 0;
            if(values[Matrix.MTRANS_X]+dx>0)
                dx=-values[Matrix.MTRANS_X];
            else if(values[Matrix.MTRANS_X]+dx<-(imgWidth*values[Matrix.MSCALE_X]-width))
                dx=-(imgWidth*values[Matrix.MSCALE_X]-width)-values[Matrix.MTRANS_X];
            return dx;
        }

        private float checkDyBound(float[] values, float dy) {
            float height=getHeight();
            if(imgHeight*values[Matrix.MSCALE_Y]<height)
                return 0;
            if(values[Matrix.MTRANS_Y]+dy>0)
                dy=-values[Matrix.MTRANS_Y];
            else if(values[Matrix.MTRANS_Y]+dy<-(imgHeight*values[Matrix.MSCALE_Y]-height))
                dy=-(imgHeight*values[Matrix.MSCALE_Y]-height)-values[Matrix.MTRANS_Y];
            return dy;
        }

    }
}


