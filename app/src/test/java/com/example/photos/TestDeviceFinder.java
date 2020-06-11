package com.example.photos;


import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.example.photos.Activities.MainActivity;
import com.example.photos.NetWorking.udp.recevier.impl.DeviceWaitingFinder;
import com.example.photos.NetWorking.udp.sender.impl.DeviceFinder;
import com.example.photos.NetWorking.utils.NetworkUtils;


import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;


/**
 * @author Ticsmyc
 * @date 2020-05-31 21:58
 */
public class TestDeviceFinder {
    Scanner sc  = new Scanner(System.in);
    MainActivity main = new MainActivity();

    /**
     * 启动UDP接收方， 先运行这个，再运行下面那个
     */
    @Test
    public void startReceviceFinder(){
        DeviceWaitingFinder deviceWaitingFinder = new DeviceWaitingFinder("小米手机",9999, null);
        deviceWaitingFinder.start();

        //用于在测试代码中阻塞线程
        sc.nextInt();
    }

    /**
     * 通过UDP通信,获取当前局域网下可传输文件的设备IP地址
     * 这个方法用于启动UDP发送方
     */
    @Test
    public void startFinder(){
        DeviceFinder deviceFinder = new DeviceFinder(9999);
        deviceFinder.start();
        //用于在测试代码中阻塞线程
        sc.nextInt();
    }

    @Test
    public void getHostIPTest(){
//        String curDeviceIp = NetworkUtils.getInet4Address().getHostAddress();
//        String broadcastIp = curDeviceIp.substring(0,curDeviceIp.lastIndexOf(".")+1)+"255";
//        System.out.println(broadcastIp);
        System.out.println(getIPs());
    }

    public List<String> getIPs()
    {
        List<String> list = new ArrayList<String>();
        boolean flag = false;
        int count=0;
        Runtime r = Runtime.getRuntime();
        Process p;
        try {
            p = r.exec("arp -a");
            BufferedReader br = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            String inline;
            while ((inline = br.readLine()) != null) {
                if(inline.indexOf("接口") > -1){
                    flag = !flag;
                    if(!flag){
                        //碰到下一个"接口"退出循环
                        break;
                    }
                }
                if(flag){
                    count++;
                    if(count > 2){
                        //有效IP
                        String[] str=inline.split(" {4}");
                        list.add(str[0]);
                    }
                }
                System.out.println(inline);
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(list);
        return list;
    }


}
