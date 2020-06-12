package com.example.photos.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photos.FileUtils;
import com.example.photos.NetWorking.tcp.sender.TcpSender;
import com.example.photos.NetWorking.udp.sender.DeviceSearcher;
import com.example.photos.NetWorking.udp.sender.impl.DeviceFinder;
import com.example.photos.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/*
* @author:sujiaxin
* */
public class DeviceActivity extends AppCompatActivity {

    private List<DeviceSearcher.DeviceBean> deviceBeanList = new ArrayList<>();//设备列表

    private LinearLayout progress_symbol;

    private ListView device_list;

    private int imagePos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

//        获取图片位置
        imagePos = getIntent().getIntExtra("pos",-1);
        if(imagePos == -1)
            finish();//假如取得index出错，结束这个活动

        //设备列表
        device_list = findViewById(R.id.device_list);
        device_list.setAdapter(new DeviceAdapter());

//        为item项添加点击方法，即传输文件方法
        device_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog(position);
            }
        });

        initBeanList();
        DeviceFinder deviceFinder = new DeviceFinder(MainActivity.DEVICE_RESP_PORT, DeviceActivity.this);
//        deviceFinder.start();
//        进度显示
        progress_symbol = findViewById(R.id.finding_symbol);
    }

    /*
    * 传输图片对话框
    * 确定传输则开启线程传输图片
    * */
    public void dialog(final int pos){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                上传文件
                final String path = FileUtils.images.get(imagePos).getPath();
//                活动进入传输进度画面
                transferringFile();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
//                            开启传输
                            TcpSender sender = new TcpSender();
                            sender.sendFile(path, deviceBeanList.get(pos).getIp(), MainActivity.RECV_PORT);
//                            显示结果
                            Looper.prepare();
                            Toast.makeText(DeviceActivity.this, "传输成功", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }catch (Exception e){
                            e.printStackTrace();
                            Looper.prepare();
                            Toast.makeText(DeviceActivity.this, "传输失败", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }finally {
                            finishFinding(true);
                        }
                    }
                }).start();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setMessage("确定传输图片吗？");
        builder.setTitle("提示");
        builder.show();

    }

    public void transferringFile(){
        progress_symbol.setVisibility(View.VISIBLE);//显示进度条
        TextView textView = findViewById(R.id.progress_text);
        textView.setText("传输图片中。。。");
        device_list.setVisibility(View.GONE);//列表不可见
    }

    /*
    * 搜索设备时UI显示
    * */
    public void startFinding(){
        progress_symbol.setVisibility(View.VISIBLE);//显示进度条
        TextView textView = findViewById(R.id.progress_text);
        textView.setText("搜索设备中。。。");
        device_list.setVisibility(View.GONE);//列表不可见
    }

    /*
    * 搜索设备结束后UI显示
    * */
    public void finishFinding(boolean finded){
        progress_symbol.setVisibility(View.GONE);//进度条不可见
        if (finded) {
            device_list.setVisibility(View.VISIBLE);//显示列表
        }else{
            //查无设备，则显示无
            TextView textView = findViewById(R.id.find_none_text);
            textView.setVisibility(View.VISIBLE);
        }
    }

    //增加设备，用于UI测试
    private void initBeanList(){
        for(int i=0;i<5;i++) {
            DeviceSearcher.DeviceBean bean = new DeviceSearcher.DeviceBean();
            bean.setName("Honor");
            deviceBeanList.add(bean);
        }

    }

    //增加设备集
    public void addDeviceAll(Collection<DeviceSearcher.DeviceBean> deviceBeans){
        this.deviceBeanList.addAll(deviceBeans);
    }

    //增加设备
    public void addDevice(DeviceSearcher.DeviceBean deviceBean){
        this.deviceBeanList.add(deviceBean);
    }



    class DeviceAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return deviceBeanList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(DeviceActivity.this);
            textView.setText(deviceBeanList.get(position).getName());
            textView.setTextSize(25);
            textView.setPadding(10,10,10,0);
            return textView;
        }
    }
}
