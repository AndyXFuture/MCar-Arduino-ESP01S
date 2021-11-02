package com.example.mcar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.example.mcar.R;
import com.example.mcar.CustomView.JoystickView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private JoystickView Joystick_A;
    private JoystickView Joystick_B;
    private TextView tv_XY1;
    private TextView tv_XY2;
    private int x1,x2,y1,y2;
    //UDP相关--------------------
    private static String IP;
    private static int BROADCAST_PORT = 9999;
    private static String BROADCAST_IP = "255.255.255.255";
    private InetAddress inetAddress = null;
    private BroadcastThread broadcastThread;
    private TelecontrolThread TelecontrolThread;
    private DatagramSocket sendSocket = null;
    private DatagramSocket receiveSocket = null;
    private volatile boolean isRuning = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initIp();
        initThread();
        while(broadcastThread.mhandler==null){}

        try {
            inetAddress = InetAddress.getByName(BROADCAST_IP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //绑定
        Joystick_A=(JoystickView)findViewById(R.id.Joystick_A);
        Joystick_B=(JoystickView)findViewById(R.id.Joystick_B);
        tv_XY1 = findViewById(R.id.tv_XY1);
        tv_XY2 = findViewById(R.id.tv_XY2);

        Joystick_A.setJoystickListener(new JoystickView.JoyStickListener() {
            @Override
            public void onSteeringWheelChanged(int x, int y) {
                x1 = x;
                y1 = y;
                Log.i("test", "x:" + (x) + " " + "y:" + (y));
                String temp = ("x:" + (x) + " " + "y:" + (y));
                tv_XY1.setText(temp);
            }
        });

        Joystick_B.setJoystickListener(new JoystickView.JoyStickListener() {
            @Override
            public void onSteeringWheelChanged(int x, int y) {
                x2 = x;
                y2 = y;
                Log.i("test", "x:" + (x) + " " + "y:" + (y));
                String temp = ("x:" + (x) + " " + "y:" + (y));
                tv_XY2.setText(temp);
            }
        });
    }

    public int margeData(){
        return ((x1+16)*1000000+(y1+16)*10000+(x2+16)*100+(y2+16));
    }

    public String margeHexData(){
        char[] chars = "0123456789ABCDEF".toCharArray();
        int[] ints = {x1,y1,x2,y2};
        StringBuilder str = new StringBuilder("");
        for(int i = 0; i<4;i++){
            str.append(chars[(ints[i]+127)/16]);
            str.append(chars[(ints[i]+128)%16]);
        }

        return str.toString().trim();
    }


    //不断发送数据
    private class TelecontrolThread extends Thread {
        @Override
        public void run() {
            int i = 0;
            while (true) {
                try {
                    if(margeData() != 16161616){
                        i = 0;
                        sendMessageToThread(broadcastThread.mhandler, String.valueOf(margeData()));
                        Log.i("test", String.valueOf(margeData()));
                    }else if(i<10){
                        i++;
                        sendMessageToThread(broadcastThread.mhandler, String.valueOf(margeData()));
                        Log.i("test", String.valueOf(margeData()));
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //UDP相关函数=======================================================================
    private void initThread() {
        broadcastThread = new BroadcastThread();
        broadcastThread.start();
        TelecontrolThread = new TelecontrolThread();
        TelecontrolThread.start();
    }

    private void initIp() {
        //Wifi状态判断
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            IP = getIpString(wifiInfo.getIpAddress());
            //ipInfo.append(IP);
            System.out.println("IP IP:" + IP);
        }
    }

    private boolean isExistIp(String revIp) {
        if (ipList != null && ipList.size() > 0) {
            for (String ip : ipList) {
                if (ip != revIp) {
                    return false;
                }
            }
        }
        return false;
    }

    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1: {
                    if (!msg.obj.equals(IP)) {
                        if (!isExistIp(msg.obj.toString())) {
                            ipList.add(msg.obj.toString());
                        }
                        //tv_receive.append(msg.obj.toString() + " 接收到信息 " + "\n");
                    }
                }
                break;
                default:
                    break;
            }
        }

    };

    //发送线程
    public class BroadcastThread extends Thread {
        private Handler mhandler = null;
        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            Looper.prepare();
            mhandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    String message = (String) msg.obj;
                    byte[] data = message.getBytes();
                    DatagramPacket dpSend = null;
                    dpSend = new DatagramPacket(data, data.length, inetAddress, BROADCAST_PORT);
                    try {
                        double start = System.currentTimeMillis();
                        //for (int i = 0 ; i < 15; i ++) {
                            sendSocket = new DatagramSocket();
                            sendSocket.send(dpSend);
                            sendSocket.close();
                            //Thread.sleep(100);
                            //Log.i(TAG, "sendMessage: data " + new String(data));
                        //}
                        double end = System.currentTimeMillis();
                        double times = end - start;
                        //Log.i(TAG, "receive: executed time is : "+ times +"ms");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();
        }
    }

    //接收线程
    private List<String> ipList = new ArrayList<>();
    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (isRuning) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket dpReceive = null;
                    ipList.clear();
                    dpReceive = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        receiveSocket.receive(dpReceive);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String recIp = dpReceive.getAddress().toString().substring(1);
                    if (dpReceive != null) {
                        Message revMessage = Message.obtain();
                        revMessage.what = 1;
                        revMessage.obj = recIp;
                        //Log.i(TAG, "handleMessage: receive ip" + recIp);
                        myHandler.sendMessage(revMessage);
                    }
                }
            }
        }
    }





    /**
     * 将获取到的int型ip转成string类型
     */
    private String getIpString(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    //发送信息到线程
    private void sendMessageToThread(Handler mhandler,String sendContent) {
        Message msg = Message.obtain();
        msg.obj = sendContent;
        msg.what = 1;
        mhandler.sendMessage(msg);
    }



}
