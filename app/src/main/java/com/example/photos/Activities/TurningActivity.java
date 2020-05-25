package com.example.photos.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.photos.FileUtils;
import com.example.photos.R;

public class TurningActivity extends AppCompatActivity
{
    private ImageView picView;
    private ImageView left;
    private ImageView right;
    private ImageView save;
    private Bitmap pic;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turning);
        int index = getIntent().getIntExtra("pos",-1);
        if(index == -1)
            finish();
        picView = findViewById(R.id.Img_turn);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        save = findViewById(R.id.saving);
        pic = BitmapFactory.decodeFile(FileUtils.images.get(index).getPath());
        picView.setImageBitmap(pic);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pic = rotateImg(pic,90);
                picView.setImageBitmap(pic);
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pic = rotateImg(pic,-90);
                picView.setImageBitmap(pic);
            }
        });

        save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                boolean res = FileUtils.saveBitmap(TurningActivity.this,pic);
                if(res)
                    Toast.makeText(TurningActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(TurningActivity.this,"保存失败",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    private Bitmap rotateImg(final Bitmap bitmap,final int degree)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

}
