package com.example.photos.Activities;


/**
 * @author DHP
 *
 */
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

//旋转图片相关的Aty
public class TurningActivity extends AppCompatActivity
{
    //相关UI
    private ImageView picView;
    private ImageView left;
    private ImageView right;
    private ImageView save;
    //显示的图片bitmap
    private Bitmap pic;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turning);
        //获取是需要被旋转图片的index
        int index = getIntent().getIntExtra("pos",-1);
        if(index == -1)
            finish();

        //初始化UI
        picView = findViewById(R.id.Img_turn);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        save = findViewById(R.id.saving);

        //获取图片
        pic = BitmapFactory.decodeFile(FileUtils.images.get(index).getPath());
        picView.setImageBitmap(pic);

        //图片向左顺时针旋转90度
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pic = rotateImg(pic,90);
                picView.setImageBitmap(pic);
            }
        });

        //图片逆时针旋转90度
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pic = rotateImg(pic,-90);
                picView.setImageBitmap(pic);
            }
        });

        //保存图片
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

    //旋转图片，使用matrix
    private Bitmap rotateImg(final Bitmap bitmap,final int degree)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

}
