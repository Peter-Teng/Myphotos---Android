package com.example.photos.NetWorking.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author Ticsmyc
 * @date 2020-05-31 21:27
 */
public class NetworkUtils {

    /**
     * 获取本机的真实IPv4地址，过滤虚拟机、蓝牙等地址
     * @return
     */
    public static InetAddress getInet4Address(){
        try {
            //获取所有网络接口
            Enumeration<NetworkInterface> allNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            //遍历所有网络接口
            for(;allNetworkInterfaces.hasMoreElements();){
                NetworkInterface networkInterface=allNetworkInterfaces.nextElement();
                //如果此网络接口为 回环接口 或者 虚拟接口(子接口) 或者 未启用 或者 描述中包含VM
                if(networkInterface.isLoopback()||networkInterface.isVirtual()||!networkInterface.isUp()||networkInterface.getDisplayName().contains("VM")){
                    //继续下次循环
                    continue;
                }
                //如果不是Intel与Realtek的网卡
//                if(!(networkInterface.getDisplayName().contains("Intel"))&&!(networkInterface.getDisplayName().contains("Realtek"))){
//                         //继续下次循环
//                            continue;
//                }
                //遍历此接口下的所有IP（因为包括子网掩码各种信息）
                for(Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses(); inetAddressEnumeration.hasMoreElements();){
                    InetAddress inetAddress=inetAddressEnumeration.nextElement();
                    //如果此IP不为空
                    if(inetAddress!=null){
                        //如果此IP为IPV4 则返回
                        if(inetAddress.getAddress().length==4){
                            return inetAddress;
                        }
                    }
                }
            }
            return null;
        }catch(SocketException e){
            e.printStackTrace();
            return null;
        }
    }
}
