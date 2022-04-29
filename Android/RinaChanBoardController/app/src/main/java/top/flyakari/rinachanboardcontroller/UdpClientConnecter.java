package top.flyakari.rinachanboardcontroller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClientConnecter {
    private static UdpClientConnecter mUdpClientConnecter;
    private ConnectListener mListener;
    private Thread mSendThread;

    private byte receiveData[] = new byte[1024];
    private String mSendHexString;

    private boolean isSend = false;

    public interface ConnectListener {
        void onReceiveData(String data);
    }

    public void setOnConnectListener(ConnectListener listener) {
        this.mListener = listener;
    }

    public static UdpClientConnecter getInstance() {
        if (mUdpClientConnecter == null) {
            mUdpClientConnecter = new UdpClientConnecter();
        }
        return mUdpClientConnecter;
    }

//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case 1000:
//                    if (mListener != null) {
//                        mListener.onReceiveData(msg.getData().getString("data"));
//                    }
//                    break;
//            }
//        }
//    };

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 1000) {
                if (mListener != null) {
                    mListener.onReceiveData(msg.getData().getString("data"));
                }
            }
            return false;
        }
    });

    /**
     * 创建udp发送连接（服务端ip地址、端口号、超时时间）
     *
     * @param ip
     * @param port
     * @param timeOut
     */
    public void createConnector(final String ip, final int port, final int timeOut) {
        if (mSendThread == null) {
            mSendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (!isSend)
                            continue;
                        DatagramSocket socket = null;
                        try {
                            socket = new DatagramSocket();
                            socket.setSoTimeout(timeOut);
                            InetAddress serverAddress = InetAddress.getByName(ip);
                            byte data[] = mSendHexString.getBytes("utf-8");
                            DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddress, port);
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            socket.send(sendPacket);
                            socket.receive(receivePacket);
                            Message msg = new Message();
                            msg.what = 1000;
                            Bundle bundle = new Bundle();
                            bundle.putString("data", new String(receivePacket.getData()));
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                            socket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        isSend = false;
                    }
                }
            });
            mSendThread.start();
        }
    }

    /**
     * 发送数据
     *
     * @param str
     */
    public void sendStr(final String str) {
        mSendHexString = str;
        isSend = true;
    }
}
