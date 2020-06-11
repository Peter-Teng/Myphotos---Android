package com.example.photos.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
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

public class ImgActivity extends AppCompatActivity {

    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img);
        int pos = getIntent().getIntExtra("pos",0);
        LinearLayout tools = findViewById(R.id.tools);
        final ImageViewPager viewPager = findViewById(R.id.pager);
        viewPager.setLayout(tools);
        viewPager.setCurrentItem(pos);
        viewPager.setOffscreenPageLimit(1);
        ImageView turning = findViewById(R.id.turnImg);
        turning.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ImgActivity.this,TurningActivity.class);
                intent.putExtra("pos",viewPager.getCurrentItem());
                startActivity(intent);
            }
        });
        ImageView painting = findViewById(R.id.paintImg);
        painting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImgActivity.this,DoodleActivity.class);
                intent.putExtra("pos",viewPager.getCurrentItem());
                startActivity(intent);
            }
        });
        ImageView share = findViewById(R.id.shareImg);
        share.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Toast.makeText(ImgActivity.this,"这是图像分享功能",Toast.LENGTH_SHORT).show();
            }
        });
        ImageView delete = findViewById(R.id.deleteImg);
        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(ImgActivity.this);
                alert.setTitle("删除照片？");
                alert.setPositiveButton("确定删除!", new DialogInterface.OnClickListener()
                {
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
        ImageView info = findViewById(R.id.infomation);
        info.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(ImgActivity.this);
                MyImage image = FileUtils.images.get(viewPager.getCurrentItem());
                StringBuilder sb = new StringBuilder();
                String name = image.getDisplayName();
                String path = image.getPath();
                Date date = image.getDateAdded();
                sb.append("照片名字：  " + name + "\n\n照片路径：  " + path + "\n\n照片日期：  " + date.toString());
                alert.setMessage(sb.toString());
                alert.setTitle("照片信息：");
                alert.setPositiveButton("确定",null);
                final AlertDialog dialog = alert.create();
                dialog.show();
            }
        });
    }
}
