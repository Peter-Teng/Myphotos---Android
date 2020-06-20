package com.example.photos.AdaptersAndView;

/**
 * @author DHP
 *
 */
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

import com.example.photos.Activities.ImgActivity;
import com.example.photos.FileUtils;
import com.example.photos.MyImage;

import java.util.ArrayList;

//自定义的ViewPager类，继承ViewPager，并且实现了MyMovingListener
public class ImageViewPager extends ViewPager implements MyMovingListener
{
    //图片是否在被dragged（即放大拖动）
    private boolean mChildIsBeingDragged=false;
    private LinearLayout layout;

    //构造函数，一并设置了对应的Adapter
    public ImageViewPager(@NonNull Context context)
    {
        super(context);
        this.setAdapter(new ViewPagerAdapter(context, FileUtils.images));
    }

    public ImageViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setAdapter(new ViewPagerAdapter(context,FileUtils.images));
    }

    public LinearLayout getLayout() {
        return layout;
    }

    public void setLayout(LinearLayout layout) {
        this.layout = layout;
    }

    //假如图片是在被放大的情况下左右上下拖动，那么就截取监听事件，以防止左右翻图像，上下滑动退出大图详情
    //的事件被触发
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if(mChildIsBeingDragged)
            return false;
        return super.onInterceptTouchEvent(arg0);
    }


    //开始拖动，设置Dragged变量为true
    @Override
    public void startDrag() {
        mChildIsBeingDragged=true;
    }

    //图片恢复原样，结束拖动
    @Override
    public void stopDrag() {
        mChildIsBeingDragged=false;
    }

    //获取adapter
    @Nullable
    @Override
    public ViewPagerAdapter getAdapter()
    {
        return (ViewPagerAdapter) super.getAdapter();
    }

    //从adapeter中删除图像
    public void delete(int pos) {this.getAdapter().deleteImg(pos);}

    //ImageViewPager对应的adapter
    public class ViewPagerAdapter extends PagerAdapter
    {
        private Context mContext;
        //图像对象列表
        private ArrayList<MyImage> imgs = new ArrayList<>();

        //构造函数
        public ViewPagerAdapter(Context ctx,ArrayList<MyImage> i)
        {
            this.mContext= ctx;
            this.imgs = i;
        }


        //删除图像
        public void deleteImg(int pos)
        {
            imgs.remove(pos);
        }

        //重写instatiateItem方法，显示相关图片
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgs.get(position).getPath());
            ScaleImageView imageView = new ScaleImageView(mContext,layout);
            imageView.setOnMovingListener(ImageViewPager.this);
            imageView.setImageBitmap(bitmap);
            ((ViewPager)container).addView(imageView,0);
            ImgActivity.matrix = null;
            return imageView;
        }

        //从viewGroup中删除这个图片
        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
        //获取显示的图片的总数
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
