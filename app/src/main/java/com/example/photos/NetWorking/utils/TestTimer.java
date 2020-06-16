package com.example.photos.NetWorking.utils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.example.photos.Activities.MainActivity;

import java.util.TimerTask;

public class TestTimer extends TimerTask {

    MainActivity context;

    public TestTimer(MainActivity context){
        this.context = context;
    }

    @Override
    public void run() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Timer is running", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
