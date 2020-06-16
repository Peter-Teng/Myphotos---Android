package com.example.photos.NetWorking.udp.recevier;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.photos.Activities.MainActivity;
import com.example.photos.NetWorking.utils.NetworkUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

/**
 * @author Ticsmyc
 * @date 2020-05-31 22:03
 */
public abstract class DeviceWaitingSearch implements Runnable {
    private final String TAG = DeviceWaitingSearch.class.getSimpleName();

    private static final int DEVICE_FIND_PORT = 9000;
    private static final int RECEIVE_TIME_OUT = 1500; // 接收超时时间，应小于等于主机的超时时间1500
    private static final int RESPONSE_DEVICE_MAX = 200; // 响应设备的最大个数，防止UDP广播攻击

    private static final byte PACKET_TYPE_FIND_DEVICE_REQ_10 = 0x10; // 搜索请求
    private static final byte PACKET_TYPE_FIND_DEVICE_RSP_11 = 0x11; // 搜索响应
    private static final byte PACKET_TYPE_FIND_DEVICE_CHK_12 = 0x12; // 搜索确认

    private static final byte PACKET_DATA_TYPE_DEVICE_NAME_20 = 0x20;
    private static final byte PACKET_DATA_TYPE_DEVICE_ROOM_21 = 0x21;

    private String deviceName;
    private int port;
    private MainActivity context; //保存可能需要进行UI交互的上下文环境
    private boolean ifInform;//开启服务时是否通过Toast通知用户,测试环境时尽量开启

    public DeviceWaitingSearch(String name, int receivePort, MainActivity context, boolean ifInform) {
        this.deviceName = name;
        this.port = receivePort;
        this.context = context;
        this.ifInform = ifInform;
    }

    public DeviceWaitingSearch(String name, int receivePort, MainActivity context){
        this(name, receivePort, context, true);
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
//        MulticastSocket socket = null;
        try {
//            socket = new MulticastSocket(port);
//            InetAddress groupAddress = InetAddress.getByName("255.255.255.255");
//            socket.joinGroup(groupAddress);

            socket = new DatagramSocket(port);

            byte[] data = new byte[1024];
            DatagramPacket pack = new DatagramPacket(data, data.length);
            while (true) {

                // 等待主机的搜索
                socket.receive(pack);
                //判断是否是搜索请求
                if (verifySearchData(pack)&&!getOwnWifiIP().equals(pack.getAddress().getHostAddress())) {
                    byte[] sendData = packData();
                    DatagramPacket sendPack = new DatagramPacket(sendData, sendData.length, pack.getAddress(), pack.getPort());
                    System.out.println(TAG+ ": 给主机回复信息");
                    socket.send(sendPack);
                    socket.setSoTimeout(RECEIVE_TIME_OUT);
                    try {
                        System.out.println(TAG+": 等待主机接收确认");
                        socket.receive(pack);
                        if (verifyCheckData(pack)) {
                            System.out.println(TAG+ ": 确认成功");
                            onDeviceSearched((InetSocketAddress) pack.getSocketAddress());
//                            break;
                        }
                    } catch (SocketTimeoutException e) {
                    }
                    // 连接超时还原成无穷大，阻塞式接收
                    socket.setSoTimeout(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    /**
     * 当设备被发现时执行
     */
    public abstract void onDeviceSearched(InetSocketAddress socketAddr);

    /**
     * 打包响应报文
     * 协议：$ + packType(1) + data(n)
     *  data: 由n组数据，每组的组成结构type(1) + length(4) + data(length)
     *  type类型中包含name类型，但name必须在最前面
     */
    private byte[] packData() {
        byte[] data = new byte[1024];
        int offset = 0;
        data[offset++] = '$';
        data[offset++] = PACKET_TYPE_FIND_DEVICE_RSP_11;

        byte[] temp = getBytesFromType(PACKET_DATA_TYPE_DEVICE_NAME_20, deviceName);
        System.arraycopy(temp, 0, data, offset, temp.length);
        offset += temp.length;

//        temp = getBytesFromType(PACKET_DATA_TYPE_DEVICE_ROOM_21, deviceRoom);
//        System.arraycopy(temp, 0, data, offset, temp.length);
//        offset += temp.length;

        byte[] retVal = new byte[offset];
        System.arraycopy(data, 0, retVal, 0, offset);

        return retVal;
    }

    private byte[] getBytesFromType(byte type, String val) {
        byte[] retVal = new byte[0];
        if (val != null) {
            byte[] valBytes = val.getBytes(Charset.forName("UTF-8"));
            retVal = new byte[5 + valBytes.length];
            retVal[0] = type;
            retVal[1] = (byte) valBytes.length;
            retVal[2] = (byte) (valBytes.length >> 8 );
            retVal[3] = (byte) (valBytes.length >> 16);
            retVal[4] = (byte) (valBytes.length >> 24);
            System.arraycopy(valBytes, 0, retVal, 5, valBytes.length);
        }
        return retVal;
    }

    /**
     * 校验搜索数据
     * 协议：$ + packType(1) + sendSeq(4)
     *  packType - 报文类型
     *  sendSeq - 发送序列
     */
    private boolean verifySearchData(DatagramPacket pack) {
        if (pack.getLength() != 6) {
            return false;
        }

        byte[] data = pack.getData();
        int offset = pack.getOffset();
        int sendSeq;
        if (data[offset++] != '$' || data[offset++] != PACKET_TYPE_FIND_DEVICE_REQ_10) {
            return false;
        }
        sendSeq = data[offset++] & 0xFF;
        sendSeq |= (data[offset++] << 8 );
        sendSeq |= (data[offset++] << 16);
        sendSeq |= (data[offset++] << 24);
        return sendSeq >= 1 && sendSeq <= 3;
    }

    /**
     * 校验确认数据
     * 协议：$ + packType(1) + sendSeq(4) + deviceIP(n<=15)
     *  packType - 报文类型
     *  sendSeq - 发送序列
     *  deviceIP - 设备IP，仅确认时携带
     */
    private boolean verifyCheckData(DatagramPacket pack) {
        if (pack.getLength() < 6) {
            return false;
        }

        byte[] data = pack.getData();
        int offset = pack.getOffset();
        int sendSeq;
        if (data[offset++] != '$' || data[offset++] != PACKET_TYPE_FIND_DEVICE_CHK_12) {
            return false;
        }
        sendSeq = data[offset++] & 0xFF;
        sendSeq |= (data[offset++] << 8 );
        sendSeq |= (data[offset++] << 16);
        sendSeq |= (data[offset++] << 24);
        if (sendSeq < 1 || sendSeq > RESPONSE_DEVICE_MAX) {
            return false;
        }

        String ip = new String(data, offset, pack.getLength() - offset, Charset.forName("UTF-8"));
        System.out.println(TAG+ "：主机IP " + ip);
        return ip.equals(getOwnWifiIP());
    }

    /**
     * 获取本机在Wifi中的IP
     */
    private String getOwnWifiIP() {
//        WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//        if (!wm.isWifiEnabled()) {
//            return "";
//        }
//
//        // 需加权限：android.permission.ACCESS_WIFI_STATE
//        WifiInfo wifiInfo = wm.getConnectionInfo();
//        int ipInt = wifiInfo.getIpAddress();
//        String ipAddr = int2Ip(ipInt);
//        Log.i(TAG, "@@@zjun: 本机IP=" + ipAddr);
//        return int2Ip(ipInt);
        return NetworkUtils.getInet4Address().getHostAddress();
    }

    /**
     * 把int表示的ip转换成字符串ip
     */
    private String int2Ip(int i) {
        return String.format("%d.%d.%d.%d", i & 0xFF, (i >> 8) & 0xFF, (i >> 16) & 0xFF, (i >> 24) & 0xFF);
    }
}

