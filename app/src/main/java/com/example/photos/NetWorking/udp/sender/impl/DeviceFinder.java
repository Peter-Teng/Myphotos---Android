package com.example.photos.NetWorking.udp.sender.impl;

import android.os.Looper;

import com.example.photos.Activities.DeviceActivity;
import com.example.photos.NetWorking.udp.sender.DeviceSearcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Ticsmyc
 * @date 2020-06-05 17:22
 */
public class DeviceFinder extends DeviceSearcher {

    private List<DeviceBean> mDeviceList;//搜索到的设备列表
    private DeviceActivity context;//搜索时以及搜索结束后进行UI交互的上下文环境

    public DeviceFinder(int port, DeviceActivity context){
        super(port);
        mDeviceList = new ArrayList<>();
        this.context = context;
    }



    /**
     * 搜索开始时执行
     */
    @Override
    public void onSearchStart() {
        //用于在UI上展示正在搜索
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.startFinding();
            }
        });

    }
    /**
     * 搜索结束后执行
     *
     * @param deviceSet 搜索到的设备集合
     */
    @Override
    public void onSearchFinish(final Set deviceSet) {
        // 结束UI上的正在搜索
        mDeviceList.clear();
        mDeviceList.addAll(deviceSet);
        // 在UI上更新设备列表
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.finishFinding(!deviceSet.isEmpty());
            }
        });

        context.addDeviceAll(mDeviceList);
    }

}
