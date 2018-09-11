/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.sniffer;

import com.sniffer.list.device.DeviceThread;
import com.sniffer.list.sniffer.publish.PingThread;
import com.sniffer.list.sniffer.publish.PublishListThread;
import com.sniffer.list.utils.ListReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public class SnifferThread implements Runnable{
    private final String snifferID;
    private final String listPath;
    private static final String TOPIC_SNIFFER = "/sniffercommunication";
    private static final String TOPIC_REGIST_DEVICE = "/registdevice";
    
    private static final String TYPE_INTERNET = "internet";
    private static final String CONF_BROKER_EXTERNAL = "internet";
    private static final String CONF_BROKER_LOCAL = "local";
    private static final String THREAD_NEW_BROKER = "newbroker";
    
    public SnifferThread(String listPath, String snifferID) {
        this.listPath = listPath;
        this.snifferID = snifferID;
    }
    
    @Override
    public void run() {
        try {
            System.out.println("\nStarting the local broker.");
            String[] deviceToken = new String[4];
            
            ListReader reader = new ListReader(listPath);
            String[] ipAndPort = reader.getSniffeLocalBroker(snifferID);
            
            BrokerInternal brokerInt = new BrokerInternal(listPath, snifferID, CONF_BROKER_LOCAL, deviceToken);
            brokerInt.connectInternalBroker(snifferID + "LocalBroker", ipAndPort[0], ipAndPort[1]);
            brokerInt.subscribe(TOPIC_REGIST_DEVICE);
            brokerInt.subscribe(TOPIC_SNIFFER);
            
            (new Thread(new PublishListThread(listPath, brokerInt), "LocalPublishList")).start();
            (new Thread(new PingThread(listPath, snifferID), "LocalPingDevices")).start();
            
            String snifferType = reader.getSnifferType(snifferID);
            if (snifferType.equals(TYPE_INTERNET)) {
                System.out.println("\nStarting the thread for the Internet Broker...");
                String[] brokerPar = reader.getSnifferRemoteBroker(snifferID);
                BrokerExternal brokerExt = new BrokerExternal(listPath, snifferID, CONF_BROKER_EXTERNAL);
                brokerExt.connectExternalBroker(snifferID + "ExternalBroker", brokerPar[0], brokerPar[1], brokerPar[2], brokerPar[3]);
                brokerExt.subscribe(TOPIC_SNIFFER);

                (new Thread(new PublishListThread(listPath, brokerExt), "ExternalPublishList")).start();
                
                //subscribing the data from others sniffers
                System.out.println("Starting subscribe data from local devices and sniffers...");
                brokerInt.setSubscritionActive(brokerExt);
            }
            
            while (true) {
                synchronized(deviceToken){
                    deviceToken.wait();
                }
                
                switch (deviceToken[0]) {
                    case THREAD_NEW_BROKER:
                        (new Thread(new DeviceThread(listPath, snifferID, deviceToken[1], deviceToken[2], deviceToken[3], true), "DeviceThread")).start();
                        break;
                    default:
                        break;
                }
                
            }
            
        } catch (JSONException | InterruptedException ex) {
            Logger.getLogger(SnifferThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
