package com.flashminds.flyingchess.localServer.UDPServer;


import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.flashminds.flyingchess.Global;
import com.flashminds.flyingchess.dataPack.DataPack;
import com.flashminds.flyingchess.localServer.LocalServer;
import com.flashminds.flyingchess.localServer.TCPServer.GameObjects.Room;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.support.v7.widget.StaggeredGridLayoutManager.TAG;

/**
 * Created by BingF on 2016/5/15.
 *
 * Edited by IACJ on 2018/4/22
 */
public class UDPServer {
    private BroadcastSender sender = null;
    private BroadcastReceiver receiver = null;
    private LocalServer parent = null;
    private ConcurrentHashMap<UUID, DataPack> roomMap = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private AppCompatActivity activity = null;

    private static final String TAG = "UDPServer";

    public UDPServer(LocalServer parent, AppCompatActivity activity) {
        this.parent = parent;
        this.activity = activity;
    }

    public DataPack createRoomInfoListDataPack() {
        List<String> msgList = new LinkedList<>();
        for (DataPack dataPack : roomMap.values()) {
            msgList.addAll(dataPack.getMessageList().subList(0, 4));
        }

        return new DataPack(DataPack.A_ROOM_LOOKUP, msgList);
    }

    public void onRoomChanged(Room room) {
        this.sender.onRoomChanged(room);
    }

    void dataPackReceived(DataPack dataPack) {
        UUID id = UUID.fromString(dataPack.getMessage(0));
        switch (dataPack.getCommand()) {
            case DataPack.E_ROOM_REMOVE_BROADCAST: {
                roomMap.remove(id);
                parent.onDataPackReceived(createRoomInfoListDataPack());
                break;
            }
            case DataPack.E_ROOM_CREATE_BROADCAST: {
                roomMap.put(id, dataPack);
                parent.onDataPackReceived(createRoomInfoListDataPack());
                break;
            }
            default: {
                Log.e(TAG, "dataPackReceived: 未知数据包错误");
            }
                
        }
    }

    public Map<UUID, DataPack> getRoomMap() {
        return roomMap;
    }

    public void startBroadcast() {
        if (this.sender == null) {
            Log.d(TAG, "startBroadcast: 开启广播发送");
            this.sender = new BroadcastSender(this, activity);
            this.executor.submit(this.sender);
        }else {
            Log.e(TAG, "startBroadcast: 重复开启广播发送");
        }
    }
    public void stopBroadcast(@NotNull Room room) {
        if (this.sender != null){
            Log.d(TAG, "startBroadcast: 关闭广播发送");
            this.sender.stop(room);
            this.sender = null;
        }else{
            Log.e(TAG, "startBroadcast: 重复关闭广播发送");
        }
    }

    public void startListen() {
        if (this.receiver == null){
            Log.d(TAG, "startListen: 开启广播接收");
            this.receiver = new BroadcastReceiver(this);
            this.executor.submit(this.receiver);
        }else{
            Log.e(TAG, "startBroadcast: 重复开启广播接收");
        }
    }

    public void stopListen() {
        if (this.receiver != null){
            Log.d(TAG, "startBroadcast: 关闭广播接收");
            this.roomMap.clear();
            this.receiver.stop();
            receiver=null;
        }else{
            Log.e(TAG, "startBroadcast: 重复关闭广播接收");
        }

    }

    /**
     * 内部类： 房间信息广播发送者
     *
     * 每个房间对应一个发送者
     */
    public class BroadcastSender implements Runnable {
        private MyUdpSocket sendSocket;
        private boolean isRunning = true;
        private String localIp = null;
        private int port = 6667;
        private UDPServer parent = null;
        private DataPack dataPack = null;
        private String ipBroadcast;
        private List<String> ipSection = null;

        private static final String TAG = "BroadcastSender";

        public BroadcastSender(UDPServer parent, final AppCompatActivity activity) {
            this.parent = parent;
            try {
                WifiManager wm = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);

                localIp = getLocalHostIp();
                ipSection = getIpSection(localIp, wm.getDhcpInfo().netmask);
                ipBroadcast = getBroadcast();
                sendSocket = new MyUdpSocket();
                Log.v(TAG, "BroadcastSender: localIP为 "+localIp+",ipBroadcast为"+ ipBroadcast +"ipSection为 "+ ipSection);
                Log.v(TAG, "BroadcastSender: Dhcp信息："+wm.getDhcpInfo());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                while (isRunning) {
                    if (this.dataPack != null) {
                        Log.v(TAG, "run: 向"+ipBroadcast+"发送广播"+dataPack);
                        this.sendSocket.send(this.dataPack, InetAddress.getByName(ipBroadcast), port);
                    }
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void stop(Room room) {
            this.isRunning = false;
            try {
                if (room != null) {
                    List<String> msgList = new ArrayList<>();
                    msgList.add(room.getId().toString());
                    this.sendSocket.send(new DataPack(DataPack.E_ROOM_REMOVE_BROADCAST, msgList), InetAddress.getByName(ipBroadcast), port);
                }
                sendSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private void onRoomChanged(Room room) {
            List<String> msgList = new ArrayList<>();
            msgList.add(room.getId().toString());
            msgList.add(room.getName());
            msgList.add(String.valueOf(room.getAllPlayers().size()));
            msgList.add(room.isPlaying() == true ? "1" : "0");
            msgList.add(this.localIp);
            msgList.add(String.valueOf(port));
            this.dataPack = new DataPack(DataPack.E_ROOM_CREATE_BROADCAST, msgList);
        }


        /**
         * 获取本地IP
         *
         * @return 本地IP
         */
        private String getLocalHostIp() {
            String ipaddress = "";
            try {
                Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                while (en.hasMoreElements()) {
                    NetworkInterface nif = en.nextElement();
                    Enumeration<InetAddress> inet = nif.getInetAddresses();
                    while (inet.hasMoreElements()) {
                        InetAddress ip = inet.nextElement();
                        if (!ip.isLoopbackAddress() && ip.getHostAddress().length() <= 15) {
                            return ip.getHostAddress();
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return ipaddress;
        }

        /**
         * 转换 ip 的类型： String -> int
         *
         * @param ip String 类型的 ip.
         * @return int 类型 ip.
         */
        private int stringToInt(String ip) {
            ip = ip.trim();

            String[] dots = ip.split("\\.");
            if (dots.length < 4) {
                throw new IllegalArgumentException();
            }

            return (Integer.valueOf(dots[0]) << 24) + (Integer.valueOf(dots[1]) << 16) + (Integer.valueOf(dots[2]) << 8) + Integer.valueOf(dots[3]);
        }

        /**
         * 转换 ip 的类型： int -> String
         *
         * @param ip int 类型的 ip.
         * @return String 类型的 ip.
         */
        private String intToString(int ip) {
            StringBuilder sb = new StringBuilder();

            sb.append(String.valueOf((ip >>> 24)));
            sb.append(".");

            sb.append(String.valueOf((ip & 0x00FFFFFF) >>> 16));
            sb.append(".");

            sb.append(String.valueOf((ip & 0x0000FFFF) >>> 8));
            sb.append(".");

            sb.append(String.valueOf((ip & 0x000000FF)));
            return sb.toString();
        }

        /**
         * 得到 ip 网段
         *
         * @param ip   网段内的任意 ip
         * @param mask 子网掩码
         * @return ip 列表
         */
        public List<String> getIpSection(String ip, Integer mask) {
            List<String> ipSection = new LinkedList<>();

            int orderedMask = ((mask & 0xFF000000) >>> 24) | ((mask & 0x00FF0000) >>> 8) | ((mask & 0x0000FF00) << 8) | ((mask & 0x000000FF) << 24);


            int startIp = stringToInt(ip) & orderedMask;
            for (int i = startIp; i < ((startIp) | (~orderedMask)); i++) {

                String ipStr = intToString(i);
                if (ipStr.equals(ip) || ipStr.contains("255"))
                    continue;

                ipSection.add(ipStr);
            }
            return ipSection;
        }


        public  String getBroadcast() throws SocketException {
            System.setProperty("java.net.preferIPv4Stack", "true");
            for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();) {
                NetworkInterface ni = niEnum.nextElement();
                if (!ni.isLoopback()) {
                    for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                        if (interfaceAddress.getBroadcast() != null) {
                            return interfaceAddress.getBroadcast().toString().substring(1);
                        }
                    }
                }
            }
            return null;
        }

    }

    /**
     * 内部类： 房间信息广播接收者
     *
     * 每个用户在`大厅活动`中获得一个广播接收者
     */

    public class BroadcastReceiver implements Runnable {
        private MyUdpSocket receiveSocket = null;
        private UDPServer parent = null;

        private boolean isRunning = false;
        private final static int port = 6667;

        private static final String TAG = "BroadcastReceiver";

        public BroadcastReceiver(UDPServer parent) {
            try {
                this.parent = parent;
                this.receiveSocket = new MyUdpSocket(port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            isRunning = true;
            while (isRunning) {
                try {
                    DataPack dataPack = this.receiveSocket.receive();
                    parent.dataPackReceived(dataPack);
                } catch (Exception e) {
                    if (e.getMessage().equals("Socket closed") ){
                        Log.d(TAG, "run: 广播接收器Socket被强行关闭");
                    }
                }
            }
        }

        public void stop() {
            isRunning = false;
            try {
                receiveSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
