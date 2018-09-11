/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.sniffer.publish;

import com.sniffer.mqtt.BroadcastSniffers;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class PingThread implements Runnable{
    private final String listPath;
    private final String snifferID;  
    private static final int PUBLISH_INTERVAL = 1000;
    private static final String PING_ALIVE = "pingalive";
    
    public PingThread(String listPath, String snifferID) throws JSONException {
        this.listPath = listPath;
        this.snifferID = snifferID;
    }
    
    @Override
    public void run() {
        try {
            System.out.println("Starting the thread responsible to ping the others sniffers.");
            JSONObject objectJSON = new JSONObject();
            objectJSON.put("sniffer_id", snifferID);
            
            BroadcastSniffers send = new BroadcastSniffers(listPath, snifferID);
            
            while (true) {
                send.sendObject(PING_ALIVE, objectJSON);
                
                //sleep between publications
                Thread.sleep(PUBLISH_INTERVAL);
            }
        } catch (JSONException | InterruptedException ex) {
            System.err.println("Stop pinging the other sniffers.");
        }
    }
    
}
