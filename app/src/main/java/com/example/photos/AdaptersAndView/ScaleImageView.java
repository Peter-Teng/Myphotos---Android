package com.example.photos.AdaptersAndView;

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

public class ScaleImageView extends androidx.appcompat.widget.AppCompatImageView implements MyMovingListener
{
    private GestureDetector gestureDetector;
    private Matrix matrix = new Matrix();
    private float imgWidth;
    private float imgHeight;
    private MyMovingListener moveListener;
    private float scale;
    private LinearLayout tools = null;

    public void setOnMovingListener(MyMovingListener listener)
    {
        moveListener=listener;
    }

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


    private void initData() {
        matrix.set(getImageMatrix());
        float[] values=new float[9];
        matrix.getValues(values);
        imgWidth=getWidth()/values[Matrix.MSCALE_X];
        imgHeight=(getHeight()-values[Matrix.MTRANS_Y]*2)/values[Matrix.MSCALE_Y];
        scale=values[Matrix.MSCALE_X];
    }

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

    public ScaleImageView(Context context,LinearLayout layout)
    {
        super(context);
        this.tools = layout;
        MatrixTouchListener mListener=new MatrixTouchListener();
        setOnTouchListener(mListener);
        gestureDetector = new GestureDetector(getContext(), new GestureListener(mListener));
        if(tools.getVisibility()==VISIBLE)
            setBackgroundColor(Color.WHITE);
        else
            setBackgroundColor(Color.BLACK);
        setScaleType(ScaleType.FIT_CENTER);
    }


    @Override
    public void startDrag() {
        if(moveListener!=null) moveListener.startDrag();
    }

    @Override
    public void stopDrag() {
        if(moveListener!=null) moveListener.stopDrag();
    }

    public class MatrixTouchListener implements OnTouchListener
    {
        private static final int DRAG = 1;
        private static final int ZOOM = 2;
        private static final int UNABLE = 3;
        private float maxScale = 6;
        private float doubleClickScale = 2;
        private int mode = 0;
        private float startDis;
        private Matrix currentMatrix = new Matrix();
        private PointF startPoint = new PointF();
        boolean mLeftDragable;
        boolean mRightDragable;
        boolean mFirstMove=false;

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch (event.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                    mode = DRAG;
                    startPoint.set(event.getX(),event.getY());
                    isMatrixEnable();
                    startDrag();
                    checkDragable();
                    break;
                case MotionEvent.ACTION_UP:
                    exitAty(event);
                    toolsDeal();
                case MotionEvent.ACTION_CANCEL:
                    reSetMatrix();
                    stopDrag();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(mode == ZOOM)
                    {
                        setZoomMatrix(event);
                    }
                    else if(mode == DRAG)
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

        private void exitAty(MotionEvent event)
        {
            if(!isZoomChanged())
            {
                float dy = Math.abs(event.getY() - startPoint.y);
                if(dy > 450f)
                {
                    ((ImgActivity)getContext()).setResult(0);
                    ((ImgActivity)getContext()).finish();
                }
            }
        }

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

        private void isMatrixEnable() {
            //当加载出错时，不可缩放
            if(getScaleType()!=ScaleType.CENTER){
                setScaleType(ScaleType.MATRIX);
            }else {
                mode=UNABLE;//设置为不支持手势
            }
        }

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


        private float distance(MotionEvent event)
        {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

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

        private float checkMaxScale(float scale, float[] values)
        {
            if(scale*values[Matrix.MSCALE_X]>maxScale)
                scale=maxScale/values[Matrix.MSCALE_X];
            currentMatrix.postScale(scale, scale,getWidth()/2,getHeight()/2);
            return scale;
        }

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
            // TODO Auto-generated method stub
            float[] values=new float[9];
            getImageMatrix().getValues(values);
            float scale=values[Matrix.MSCALE_X];
            matrix.getValues(values);
            return scale<values[Matrix.MSCALE_X];
        }


        public void onDoubleClick()
        {
            float scale=isZoomChanged()?1:doubleClickScale;
            currentMatrix.set(matrix);
            currentMatrix.postScale(scale, scale,getWidth()/2,getHeight()/2);
            setImageMatrix(currentMatrix);
        }
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


