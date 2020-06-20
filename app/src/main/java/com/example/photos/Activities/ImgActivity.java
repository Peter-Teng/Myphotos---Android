package com.example.photos.Activities;


/**
 * @author DHP
 *
 */
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.photos.AdaptersAndView.ImageViewPager;
import com.example.photos.FileUtils;
import com.example.photos.MyImage;
import com.example.photos.R;

import java.util.Date;


//查看大图页面的Activity
public class ImgActivity extends AppCompatActivity {

    public static Matrix matrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img);

        //取得再上一个活动中点击的缩略图对应的index
        int pos = getIntent().getIntExtra("pos",0);

        //初始化ImageiewPager的界面
        LinearLayout tools = findViewById(R.id.tools);
        final ImageViewPager viewPager = findViewById(R.id.pager);
        viewPager.setLayout(tools);
        viewPager.setCurrentItem(pos);
        viewPager.setOffscreenPageLimit(1);

        //为下方工具栏的各个功能设置点击事件监听
        //旋转图像功能按钮的点击事件
        ImageView turning = findViewById(R.id.turnImg);
        turning.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ImgActivity.this,TurningActivity.class);//启动旋转图像对应的Aty
                intent.putExtra("pos",viewPager.getCurrentItem());//设置intent，告知该ATY应该获取哪张图片
                startActivity(intent);
            }
        });

        //涂鸦功能按钮的点击事件
        ImageView painting = findViewById(R.id.paintImg);
        painting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImgActivity.this,DoodleActivity.class);//启动旋转涂鸦对应的Aty
                intent.putExtra("pos",viewPager.getCurrentItem());//设置intent，告知该ATY应该获取哪张图片
                startActivity(intent);
            }
        });

        //分享（局域网传图）相关功能按钮的点击事件
        ImageView share = findViewById(R.id.shareImg);
        share.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ImgActivity.this, DeviceActivity.class);//启动设备列表活动
                intent.putExtra("pos",viewPager.getCurrentItem());//设置intent，告知该ATY应该获取哪张图片
                startActivity(intent);
            }
        });

        //删除图像按钮的点击事件
        ImageView delete = findViewById(R.id.deleteImg);
        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //创建一个AlertDialog来二次确认用户是否真的要删除图片
                AlertDialog.Builder alert = new AlertDialog.Builder(ImgActivity.this);
                alert.setTitle("删除照片？");
                //为dialog设置按钮点击事件
                alert.setPositiveButton("确定删除!", new DialogInterface.OnClickListener()
                {
                    //设置结束该Aty返回的参数，真正删除图片的代码不在这个活动中，在MainActivity中
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent intent = new Intent();
                        intent.putExtra("pos",viewPager.getCurrentItem());
                        setResult(1,intent);
                        finish();
                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();
            }
        });

        //显示图片详情信息的按钮的点击事件
        ImageView info = findViewById(R.id.infomation);
        info.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //创建一个AlertDialog显示图像相关的信息
                AlertDialog.Builder alert = new AlertDialog.Builder(ImgActivity.this);
                //获取图像对应的MyImage对象
                MyImage image = FileUtils.images.get(viewPager.getCurrentItem());
                StringBuilder sb = new StringBuilder();
                //获取图像相关信息
                String name = image.getDisplayName();
                String path = image.getPath();
                Date date = image.getDateAdded();
                sb.append("照片名字：  " + name + "\n\n照片路径：  " + path + "\n\n照片日期：  " + date.toString());
                alert.setMessage(sb.toString());
                alert.setTitle("照片信息：");
                alert.setPositiveButton("确定",null);
                final AlertDialog dialog = alert.create();
                dialog.show();//展示dialog
            }
        });
    }
}
