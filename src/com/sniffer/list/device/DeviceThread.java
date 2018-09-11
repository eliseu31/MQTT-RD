/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.device;

import com.sniffer.list.operations.DataManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public class DeviceThread implements Runnable{
    private final String snifferID;
    private final String listPath;
    private final Boolean startedBySniffer;
    private final String[] deviceToken = new String[4];
    private static final String TOPIC_REGIST_DEVICE = "/registdevice";
    private static final String THREAD_NEW_BROKER = "newbroker";
    private static final String THREAD_KILL = "threadkill";
    
    public DeviceThread(String listPath, String snifferID, String deviceID, String deviceIP, String devicePort, Boolean startedBySniffer) {
        this.listPath = listPath;
        this.snifferID = snifferID;
        
        this.deviceToken[1] = deviceID;
        this.deviceToken[2] = deviceIP;
        this.deviceToken[3] = devicePort;
        
        this.startedBySniffer = startedBySniffer;
    }    
    
    @Override
    public void run() {
        try {
            System.out.println("\nStarting the device local broker...");
            BrokerDevice brokerDevice = new BrokerDevice(listPath, snifferID, deviceToken);
            brokerDevice.setBrokerType(startedBySniffer);
            brokerDevice.connectInternalBroker(deviceToken[1] + "DeviceBroker", deviceToken[2], deviceToken[3]);
            brokerDevice.subscribe(TOPIC_REGIST_DEVICE);
            if (startedBySniffer) {
                DataManager manager = new DataManager(listPath, snifferID);
                manager.subscribeDeviceTopics(snifferID, deviceToken[1], brokerDevice);                
            }
            
            while (true) {            
                synchronized(deviceToken){
                    deviceToken.wait();
                }
                
                switch (deviceToken[0]) {
                    case THREAD_NEW_BROKER:
                        (new Thread(new DeviceThread(listPath, snifferID, deviceToken[1], deviceToken[2], deviceToken[3], true), "DeviceThread")).start();
                        break;
                    case THREAD_KILL:
                        brokerDevice.getClient().close();
                        Thread.currentThread().interrupt();
                        break;
                    default:
                        break;
                }
            }         
            
        } catch (JSONException | InterruptedException | MqttException ex) {
            Logger.getLogger(DeviceThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
