/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.operations;

import com.sniffer.list.utils.ListWriter;
import com.sniffer.mqtt.GenericClient;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class ListSubscriber extends GenericClient{
    private final String listPath;
    private static final String TOPIC_LIST = "/getlist";
    private static final int SERVER_TIMEOUT = 10000;    

    public ListSubscriber(String listPath) {
        this.listPath = listPath;
    }

    public void getLocalList(String snifferID, String ip, String port) {
        System.out.println("\nGetting the list from a local sniffer...");
        connectInternalBroker(snifferID, ip, port);
        subscribe(TOPIC_LIST);
        try {
            Thread.sleep(SERVER_TIMEOUT);
        } catch (InterruptedException ex) {
            System.err.println("Cann't get the list from the sniffer at that IP: " + ip);
        }
        unsubscribe(TOPIC_LIST);
        disconnect();
    }
    
    public void getRemoteList(String snifferID, JSONObject config) throws JSONException, InterruptedException{
        System.out.println("\nGetting the list from a remote sniffer...");
        
        JSONObject brokers = config.getJSONObject("remote_brokers");

        //Get the list from the server
        JSONObject serverBroker = brokers.getJSONObject("server");
        String serverIP = serverBroker.getString("ip");
        String serverPort = serverBroker.getString("port");
        String serverUser = serverBroker.getString("user");
        String serverPass = serverBroker.getString("pass");
        
        //Get the list from the server
        ListSubscriber subList = new ListSubscriber(listPath);
        subList.connectExternalBroker(snifferID, serverIP, serverPort, serverUser, serverPass);
        subList.subscribe(TOPIC_LIST);
        Thread.sleep(SERVER_TIMEOUT);
        subList.disconnect();        
    }
    
    @Override
    public void processMessage(String message, String topic) {
        try {
            System.out.println("Sucess getting the list.");
            JSONObject listJSON = new JSONObject(message);
            ListWriter writer = new ListWriter(listPath);
            writer.setList(listJSON);
            writer.saveListFile();
            System.out.println("List saved at file: " + listPath);
        } catch (JSONException ex) {
            System.err.println("Error getting the list.\nCheck the received JSON file.");
        }     
    }
    
    @Override
    public void lostConnection() {
        System.err.println("Error subscribing the list.");
    }    
}
