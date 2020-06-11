package com.example.photos.NetWorking.udp.sender.impl;

import com.example.photos.NetWorking.udp.sender.DeviceSearcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Ticsmyc
 * @date 2020-06-05 17:22
 */
public class DeviceFinder extends DeviceSearcher {

    private List<DeviceBean> mDeviceList;
    public DeviceFinder(int port){
        super(port);
        mDeviceList = new ArrayList<>();
    }



    /**
     * 搜索开始时执行
     */
    @Override
    public void onSearchStart() {
        //用于在UI上展示正在搜索
    }
    /**
     * 搜索结束后执行
     *
     * @param deviceSet 搜索到的设备集合
     */
    @Override
    public void onSearchFinish(Set deviceSet) {
        // 结束UI上的正在搜索
        mDeviceList.clear();
        mDeviceList.addAll(deviceSet);
        // 在UI上更新设备列表
        for(DeviceBean device : mDeviceList ){
            System.out.println(device.toString());
        }
    }

}
