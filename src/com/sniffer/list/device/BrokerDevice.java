/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.device;

import com.sniffer.list.operations.DataManager;
import com.sniffer.list.operations.DeleteThings;
import com.sniffer.list.operations.RegistDevice;
import com.sniffer.list.sniffer.BrokerInternal;
import com.sniffer.mqtt.GenericClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public class BrokerDevice extends GenericClient{
    private final String listPath;
    private final String snifferID;
    private final String deviceID;
    private Boolean firstRegist;
    private static final String TOPIC_REGIST_DEVICE = "/registdevice";
    private static final int LIMIT_MESSAGES = 5;
    private static final String THREAD_KILL = "threadkill";

    private final String[] deviceToken;
    
    private final RegistDevice reg;
    private final DataManager data;
    
    public BrokerDevice(String listPath, String snifferID, String[] deviceToken) throws JSONException {
        this.listPath = listPath;
        this.snifferID = snifferID;
        this.deviceID = deviceToken[1];
        
        this.deviceToken = deviceToken;
        
        reg = new RegistDevice(listPath, snifferID);
        data = new DataManager(listPath, snifferID);
    }
    
    public void setBrokerType(Boolean startedBySniffer) throws JSONException {
        firstRegist = startedBySniffer;      
    }

    @Override
    public void processMessage(String message, String topic) {
        try {
            switch (topic) {
                case TOPIC_REGIST_DEVICE:
                    System.out.println("\nNew device regist operation in the local device broker.");
                    reg.newDevice(message, this, firstRegist, deviceToken);
                    if (!firstRegist) {
                        firstRegist = true;
                    }
                    break;
                default:
                    data.newDataMessageDevice(message, topic, this.getTopicsMap(), LIMIT_MESSAGES);
                    break;
            }
            
        } catch (JSONException ex) {
            Logger.getLogger(BrokerDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void lostConnection() {
        try {
            //If device goes down
            DeleteThings delete = new DeleteThings(listPath, snifferID);
            delete.sendDeleteDevice(snifferID, deviceID);
            delete.deleteDevice(snifferID, deviceID);
            deviceToken[0] = THREAD_KILL;
            synchronized(deviceToken){
                deviceToken.notify();
            }
            
        } catch (JSONException ex) {
            System.err.println("Error deleting device from list.");
            Logger.getLogger(BrokerInternal.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}
