package com.flashminds.flyingchess.localServer.UDPServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.flashminds.flyingchess.dataPack.DataPack;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by Ryan on 16/5/15.
 *
 * Edited by IACJ on 2018/4/19
 */
public class DataPackUdpSocket {
    protected DatagramSocket socket = null;
    protected Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
    protected byte[] buffer = new byte[1024];

    public DataPackUdpSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * This method sends out the datapack immediately, in the thread
     * which calls the method.
     *
     * @param dataPack The datapack to be sent.
     * @param ip       The ip to which the datapack is sent.
     */
    public void send(DataPack dataPack, InetAddress ip, int port) throws IOException {
        byte[] bytes = gson.toJson(dataPack, DataPack.class).getBytes();
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, ip, port);
        socket.send(packet);
    }

    /**
     * Receive one data pack from the inputstream, which
     * will be blocking until one data pack is successfully read.
     *
     * @return The data pack read.
     */
    public DataPack receive() throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        System.out.println(new String(packet.getData()));
        return gson.fromJson(new String(packet.getData()).trim(), DataPack.class);
    }

    /**
     * Close the socket.
     */
    public void close() throws IOException {
        this.socket.close();
    }

    public InetSocketAddress getInetSocketAddress() {
        InetAddress ip = null;
        ip = this.socket.getInetAddress();
        int port = this.socket.getPort();
        InetSocketAddress address = new InetSocketAddress(ip, port);
        return address;
    }
}
