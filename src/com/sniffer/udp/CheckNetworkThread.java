/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.udp;

import com.sniffer.list.device.DeviceThread;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public class CheckNetworkThread implements Runnable {

    private final String listPath;
    private final String snifferID;
    private static final String REQUEST_SNIFFER = "anyonesniffer?";
    private static final String RESPONSE_SNIFFER = "yes";
    private static final String REQUEST_DEVICE = "newdevice";
    private static final String RESPONSE_DEVICE = "startregist";
    private static final int PACKET_SIZE = 300;
    private final String mAddress;
    private final int port;

    public CheckNetworkThread(String mAddress, int port, String listPath, String snifferID) throws JSONException {
        this.mAddress = mAddress;
        this.port = port;
        this.listPath = listPath;
        this.snifferID = snifferID;
    }

    @Override
    public void run() {
        try {
            System.out.println("\nStart checking new devices and sniffers in the network...");

            InetAddress addr = InetAddress.getByName(mAddress);
            MulticastSocket mcSock = new MulticastSocket(port);
            mcSock.joinGroup(addr);

            DatagramPacket rcPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

            while (true) {
                mcSock.receive(rcPacket);
                System.out.println("\nNew message.");
                String request = new String(rcPacket.getData()).trim();

                String deviceIP = rcPacket.getAddress().getHostAddress();

                String[] splitRequest = request.split(":");

                switch (splitRequest[0]) {
                    case REQUEST_SNIFFER:
                        System.out.println("New sniffer in the network.\nSniffer ip: " + deviceIP);
                        sendResponse(deviceIP, RESPONSE_SNIFFER);
                        break;
                    case REQUEST_DEVICE:
                        System.out.println("New device in the network.\nDevice ID: " + splitRequest[1]);
                        System.out.println("Device IP: " + deviceIP + ", Port: 1883");
                        sendResponse(deviceIP, RESPONSE_DEVICE);
                        (new Thread(new DeviceThread(listPath, snifferID, splitRequest[1], deviceIP, "1883", false), "DeviceThread")).start();
                        break;
                    default:
                        System.out.println("Message doesn't corresponds to the expected.");
                        break;
                }
            }

        } catch (IOException ex) {
            System.err.println("Error checking new network sniffers and devices.");
        }
    }

    private void sendResponse(String host, String responseString) {
        try {
            InetAddress addr = InetAddress.getByName(host);
            byte[] response = responseString.getBytes();

            DatagramSocket sendSOCK = new DatagramSocket();
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);
            responsePacket.setAddress(addr);
            responsePacket.setPort(port + 1);
            sendSOCK.send(responsePacket);

            System.out.println("Sended packet to address: " + responsePacket.getAddress().getHostAddress());

        } catch (SocketException ex) {
            Logger.getLogger(CheckNetworkThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CheckNetworkThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
