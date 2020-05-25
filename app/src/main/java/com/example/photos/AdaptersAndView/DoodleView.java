package com.example.photos.AdaptersAndView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.photos.FileUtils;

public class DoodleView extends androidx.appcompat.widget.AppCompatImageView
{
    private int view_width = 0;
    private int view_height = 0;
    private float downX;
    private float downY;
    public Paint paint = null;
    Bitmap cacheBitmap = null;
    Canvas canvas = null;

    public DoodleView(Context context, AttributeSet set) {
        super(context, set);
        view_width = context.getResources().getDisplayMetrics().widthPixels;
        view_height = context.getResources().getDisplayMetrics().heightPixels;
    }

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


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                canvas.drawLine(downX,downY,moveX,moveY,paint);
                downX = moveX;
                downY = moveY;
                this.setImageBitmap(cacheBitmap);
                break;
            default:
                break;
        }
        return true;
    }

    public boolean save(Context ctx)
    {
        return FileUtils.saveBitmap(ctx,cacheBitmap);
    }
}