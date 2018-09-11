/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.mqtt;

import com.sniffer.list.utils.ListReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class BroadcastSniffers {
    private final String listPath;
    private final String snifferID;
    private final String snifferType;
    private static final String TYPE_INTERNET = "internet";
    private static final String TYPE_LOCAL = "local";
    private final ListReader reader;
    
    public BroadcastSniffers(String listPath, String snifferID) throws JSONException {
        this.listPath = listPath;
        this.snifferID = snifferID;
        reader = new ListReader(listPath);
        this.snifferType = reader.getSnifferType(snifferID);
    }
    
    public void sendObject(String operation, JSONObject objectJSON) throws JSONException {
        switch (snifferType) {
            case TYPE_INTERNET:
                sendLocal(operation, objectJSON);
                sendInternet(operation, objectJSON);
                break;
            case TYPE_LOCAL:
                sendLocal(operation, objectJSON);
                break;
            default:
                break;
        }

    }
    
    public synchronized void sendLocal(String operation, JSONObject objectJSON) throws JSONException{
        JSONObject message = new JSONObject();
        message.put("operation", operation);
        message.put("object", objectJSON);
        
        reader.readFile();
        List<String[]> sniffersList = reader.getSniffers(snifferID, TYPE_LOCAL);
        for (String[] iterator : sniffersList) {
            if (!iterator[0].equals(snifferID)) {
                
                SendThread send = new SendThread(snifferID,listPath, iterator[0], iterator[1], iterator[2]);
                send.setMessage(message.toString());
                Thread t = new Thread(send);
                t.start();
                
                synchronized (t) {
                    try {
                        t.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BroadcastSniffers.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }        
    }
    
    public synchronized void sendInternet(String operation, JSONObject objectJSON) throws JSONException{
        JSONObject message = new JSONObject();
        message.put("operation", operation);
        message.put("object", objectJSON);    
        
        reader.readFile();
        List<String[]> sniffersList = reader.getSniffers(snifferID, TYPE_INTERNET);
        for (String[] iterator : sniffersList) {
            if (!iterator[0].equals(snifferID)) {
               
                SendThread send = new SendThread(snifferID,listPath, iterator[0], iterator[1], iterator[2]);
                String[] auth = reader.getSnifferRemoteBroker(iterator[0]);
                send.setLoginAndPasswd(auth[2], auth[3]);
                send.setMessage(message.toString());
                Thread t = new Thread(send);
                t.start();
                
                synchronized (t) {
                    try {
                        t.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BroadcastSniffers.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }         
                
            }
        }
    }
}
