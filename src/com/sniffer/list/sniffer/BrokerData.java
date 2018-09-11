/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.sniffer;

import com.sniffer.list.operations.DataManager;
import com.sniffer.mqtt.GenericClient;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public class BrokerData extends GenericClient{
    private final GenericClient internetBroker;
    
    private final DataManager manager;
    
    public BrokerData(String listPath, String subSnifferID, GenericClient internetBroker) throws JSONException {
        this.internetBroker = internetBroker;
        
        manager = new DataManager(listPath, subSnifferID);
    }
    
    @Override
    public void processMessage(String message, String topic) {
        manager.newDataMessageSniffer(message, topic, internetBroker, this.getTopicsMap(), 3);
    }

    @Override
    public void lostConnection() {
        System.err.println("Error at data broker sniffer subscriber.");
    }
    
}
