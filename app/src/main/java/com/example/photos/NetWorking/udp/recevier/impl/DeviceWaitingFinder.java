package com.example.photos.NetWorking.udp.recevier.impl;

import android.content.Context;

import com.example.photos.NetWorking.udp.recevier.DeviceWaitingSearch;

import java.net.InetSocketAddress;

/**
 * @author Ticsmyc
 * @date 2020-06-05 17:22
 */
public class DeviceWaitingFinder extends DeviceWaitingSearch {


    public DeviceWaitingFinder(String name, int reciverPort, Context context){
        super(name,reciverPort, context);
    }

    /**
     * 当设备被发现时执行
     *
     * @param socketAddr
     */
    @Override
    public void onDeviceSearched(InetSocketAddress socketAddr) {
        System.out.println("已上线，搜索主机：" + socketAddr.getAddress().getHostAddress() + ":" + socketAddr.getPort());
    }

}
