/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.sniffer;

import com.sniffer.mqtt.GenericClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public class BrokerExternal extends GenericClient{
    private static final String TOPIC_SNIFFER = "/sniffercommunication";
    
    private final SnifferOperations ope;

    public BrokerExternal(String listPath, String snifferID, String confBroker) throws JSONException {
        ope = new SnifferOperations(listPath, snifferID, confBroker);
    }
    
    @Override
    public void processMessage(String message, String topic) {
        try {
            switch (topic) {
                case TOPIC_SNIFFER:
                    ope.snifferProtocol(message);
                    break;
                default:
                    break;
            }
            
        } catch (JSONException ex) {
            Logger.getLogger(BrokerExternal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void lostConnection() {
        System.err.println("Error at external broker connection.");
    }
    
}
