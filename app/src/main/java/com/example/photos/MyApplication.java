package com.example.photos;

import android.app.Application;
import android.content.Context;

//参考了《第一行代码》利用这种方法全局地获取Context
public class MyApplication extends Application
{
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext()
    {
        return context;
    }
}
