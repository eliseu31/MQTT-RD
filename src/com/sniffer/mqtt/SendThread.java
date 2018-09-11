/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.mqtt;

import com.sniffer.list.operations.DeleteThings;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public class SendThread implements Runnable{
    private final String snifferID;
    private final String url;
    private static final String TOPIC_SNIFFER = "/sniffercommunication";
    private final BasicClientMQTT mqtt;
    private String message;
    private final String listPath;
    private final String otherSniffer;

    public SendThread(String snifferID, String listPath, String otherSniffer, String ip, String port) {
        this.snifferID = snifferID;
        this.url = "tcp://" + ip + ":" + port;
        this.mqtt = new BasicClientMQTT();
        this.otherSniffer = otherSniffer;
        this.listPath = listPath;
        
    }
    
    public void setLoginAndPasswd(String login, String passwd){
        mqtt.setUserPass(login, passwd);
    }
    
    public void setMessage(String message){
        this.message = message;
    }
    
    @Override
    public void run() {
        int sucess = mqtt.connect(url, snifferID + "BroadcastPublisher" + UUID.randomUUID());
        if (sucess < 0) {                        
            try {
                DeleteThings delete = new DeleteThings(listPath, snifferID);
                delete.deleteSniffer(otherSniffer);
                delete.sendDeleteSniffer(otherSniffer);
            } catch (JSONException ex) {
                Logger.getLogger(SendThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            mqtt.publish(message, TOPIC_SNIFFER);
            mqtt.disconnect();
        }   
        
        synchronized (this) {
            notify();
        }
    }
    
}
