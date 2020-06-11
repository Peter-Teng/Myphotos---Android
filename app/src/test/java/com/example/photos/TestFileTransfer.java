package com.example.photos;

import com.example.photos.NetWorking.tcp.sender.TcpSender;
import com.example.photos.NetWorking.tcp.receiver.TcpReciver;

import org.junit.Test;

import java.io.IOException;

/**
 * @author Ticsmyc
 * @date 2020-06-05 17:29
 */
public class TestFileTransfer {

    /**
     * 通过TCP通信发送文件  ,参数为文件的本地路径 + 对方IP地址（通过FindDevice获得）+端口号（提前设定好）
     */
    @Test
    public void startTcpSender(){
        TcpSender tcpSender = new TcpSender();
        tcpSender.sendFile("C:\\Users\\user\\Documents\\Tencent Files\\731744768\\FileRecv\\Textbook.txt","localhost",8888);
    }

    /**
     * TCP接收， 参数为 提前商量好的端口号+文件存储路径+要展示上传结果的活动
     */
    @Test
    public void startTcpReceiver() throws IOException {
        TcpReciver tcpReciver = new TcpReciver(8888,"D:\\",null);
        new Thread(tcpReciver).start();
        while(true);
    }

}
