/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.sniffer;

import com.sniffer.list.operations.RegistDevice;
import com.sniffer.mqtt.GenericClient;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public class BrokerInternal extends GenericClient{
    private static final String TOPIC_SNIFFER = "/sniffercommunication";
    private static final String TOPIC_REGIST_DEVICE = "/registdevice";
    
    private final String[] deviceToken;
    
    private final SnifferOperations ope;
    private final RegistDevice dev;
    
    public BrokerInternal(String listPath, String snifferID, String confBroker, String[] deviceToken) throws JSONException {
        this.deviceToken = deviceToken;
        
        ope = new SnifferOperations(listPath, snifferID, confBroker);
        dev = new RegistDevice(listPath, snifferID);
    }
    
    protected void setSubscritionActive(GenericClient internetBroker) throws JSONException{
        Map<String,GenericClient> dataBrokers = new HashMap<>();
        ope.setSubscritionsActive(internetBroker, dataBrokers);
        dev.setSubscritionsActive(dataBrokers);
    }
    
    @Override
    public void processMessage(String message, String topic) {
        try {
            switch (topic) {
                case TOPIC_SNIFFER:
                    ope.snifferProtocol(message);
                    break;
                case TOPIC_REGIST_DEVICE:
                    System.out.println("\nNew device regist operation in the local broker.");
                    dev.newDevice(message, this, Boolean.TRUE, deviceToken);
                    break;
                default:
                    break;
            }
            
        } catch (JSONException ex) {
            Logger.getLogger(BrokerInternal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void lostConnection() {
        System.err.println("Error at internal broker connection.");
    }
}
