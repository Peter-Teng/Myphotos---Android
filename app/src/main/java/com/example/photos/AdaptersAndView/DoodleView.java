package com.example.photos.AdaptersAndView;

/**
 * @author DHP
 *
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.photos.FileUtils;

//自定义的View，用于给图片涂鸦，继承自ImageView
public class DoodleView extends androidx.appcompat.widget.AppCompatImageView
{
    //相关参数以及canvas，paint
    private int view_width = 0;
    private int view_height = 0;
    private float downX;
    private float downY;
    public Paint paint = null;
    Bitmap cacheBitmap = null;
    Canvas canvas = null;

    //构造函数
    public DoodleView(Context context, AttributeSet set)
    {
        super(context, set);
        //获取view相关的宽度和高度
        view_width = context.getResources().getDisplayMetrics().widthPixels;
        view_height = context.getResources().getDisplayMetrics().heightPixels;
    }

    //初始化bitmap，画布canvas以及涂鸦笔paint
    public void init(Bitmap bitmap)
    {
        cacheBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        canvas = new Canvas();
        canvas.setBitmap(cacheBitmap);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
        paint.setDither(true);
    }


    //重写点击事件
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            //假如被点击
            case MotionEvent.ACTION_DOWN:
                //记录相关x，y坐标
                downX = event.getX();
                downY = event.getY();
                break;
            //手指在屏幕中移动
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                //涂鸦
                canvas.drawLine(downX,downY,moveX,moveY,paint);
                //更新老的x，y坐标
                downX = moveX;
                downY = moveY;
                this.setImageBitmap(cacheBitmap);
                break;
            default:
                break;
        }
        return true;
    }

    //保存图片
    public boolean save(Context ctx)
    {
        return FileUtils.saveBitmap(ctx,cacheBitmap);
    }
}