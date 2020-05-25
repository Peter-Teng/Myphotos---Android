package com.example.photos.AdaptersAndView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.photos.FileUtils;
import com.example.photos.MyImage;

import java.util.ArrayList;


public class ImageViewPager extends ViewPager implements MyMovingListener
{
    private boolean mChildIsBeingDragged=false;
    private LinearLayout layout;

    public LinearLayout getLayout() {
        return layout;
    }

    public void setLayout(LinearLayout layout) {
        this.layout = layout;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if(mChildIsBeingDragged)
            return false;
        return super.onInterceptTouchEvent(arg0);
    }


    @Override
    public void startDrag() {
        mChildIsBeingDragged=true;
    }


    @Override
    public void stopDrag() {
        mChildIsBeingDragged=false;
    }


    public ImageViewPager(@NonNull Context context)
    {
        super(context);
        this.setAdapter(new ViewPagerAdapter(context, FileUtils.images));
    }

    public ImageViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setAdapter(new ViewPagerAdapter(context,FileUtils.images));
    }

    @Nullable
    @Override
    public ViewPagerAdapter getAdapter()
    {
        return (ViewPagerAdapter) super.getAdapter();
    }

    public void delete(int pos) {this.getAdapter().deleteImg(pos);}

    public class ViewPagerAdapter extends PagerAdapter
    {
        private Context mContext;
        private ArrayList<MyImage> imgs = new ArrayList<>();

        public void deleteImg(int pos)
        {
            imgs.remove(pos);
        }

        public ViewPagerAdapter(Context ctx,ArrayList<MyImage> i)
        {
            this.mContext= ctx;
            this.imgs = i;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgs.get(position).getPath());
            ScaleImageView imageView = new ScaleImageView(mContext,layout);
            imageView.setOnMovingListener(ImageViewPager.this);
            imageView.setImageBitmap(bitmap);
            ((ViewPager)container).addView(imageView,0);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return imgs.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == (View)object;
        }
    }
}
