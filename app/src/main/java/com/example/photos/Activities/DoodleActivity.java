package com.example.photos.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.photos.AdaptersAndView.DoodleView;
import com.example.photos.FileUtils;
import com.example.photos.R;

public class DoodleActivity extends AppCompatActivity {

    private ImageView save;
    private DoodleView mainImg;
    private Bitmap pic;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doodle);
        int index = getIntent().getIntExtra("pos",-1);
        if(index == -1)
            finish();
        save = findViewById(R.id.savingDoodle);
        mainImg = findViewById(R.id.Img_Doodle);
        pic = BitmapFactory.decodeFile(FileUtils.images.get(index).getPath());
        mainImg.setImageBitmap(pic);
        mainImg.init(pic);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean res = mainImg.save(DoodleActivity.this);
                if(res)
                    Toast.makeText(DoodleActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(DoodleActivity.this,"保存失败",Toast.LENGTH_SHORT).show();
                finish();
                finish();
            }
        });
    }
}
