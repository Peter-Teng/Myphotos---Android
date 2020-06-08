package com.example.photos.Activities;


/**
 * @author DHP
 *
 */
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.photos.AdaptersAndView.DoodleView;
import com.example.photos.FileUtils;
import com.example.photos.R;

//涂鸦界面的活动
public class DoodleActivity extends AppCompatActivity {

    private ImageView save;//保存图片
    private DoodleView mainImg;//涂鸦View
    private Bitmap pic;//要显示的图片bitmap

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doodle);
        int index = getIntent().getIntExtra("pos",-1);//从启动DoodleAty的活动中得到对应照片的index
        if(index == -1)
            finish();//假如取得index出错，结束这个活动

        //获取layout中的view
        save = findViewById(R.id.savingDoodle);
        mainImg = findViewById(R.id.Img_Doodle);
        //加载图片
        pic = BitmapFactory.decodeFile(FileUtils.images.get(index).getPath());
        mainImg.setImageBitmap(pic);
        mainImg.init(pic);

        //为save图片“按钮”添加点击事件监听，保存图片
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean res = mainImg.save(DoodleActivity.this);
                //保存成功与否的判断，提示用户保存结果
                if(res)
                    Toast.makeText(DoodleActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(DoodleActivity.this,"保存失败",Toast.LENGTH_SHORT).show();
                //保存之后结束活动
                finish();
            }
        });
    }
}
