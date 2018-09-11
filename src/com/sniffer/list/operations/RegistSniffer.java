/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.operations;

import com.sniffer.mqtt.BroadcastSniffers;
import com.sniffer.list.utils.ListReader;
import com.sniffer.list.utils.ListWriter;
import com.sniffer.udp.NetworkOperations;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class RegistSniffer {
    private final String listPath;
    private String snifferID;
    private static final String REGIST_OPERATION = "regist";
    private static final String IP_AUTO_DETECTED = "autodetected";
    private JSONObject snifferJSON = new JSONObject();

    public RegistSniffer(String listPath) {
        this.listPath = listPath;
    }
    
    public void setJSON(JSONObject json){
        this.snifferJSON = json;
    }
    
    public JSONObject getJSON(){
        return snifferJSON;
    }
    
    public void createSnifferRegist(String snifferID, String snifferType, String transport, JSONObject network) throws JSONException{
        System.out.println("\nCreating the JSON file for sniffer registration...");
        this.snifferID = snifferID;
        
        snifferJSON.put("sniffer_id", snifferID);
        snifferJSON.put("sniffer_type", snifferType);
        snifferJSON.put("transport", transport);
        snifferJSON.put("network", network);
        JSONArray devicesArray = new JSONArray();
        snifferJSON.put("devices", devicesArray);
    }
    
    public void setRemoteBroker(JSONObject config) throws JSONException{
        System.out.println("Setting remote broker...");
        JSONObject brokers = config.getJSONObject("remote_brokers");
        JSONObject rBroker = brokers.getJSONObject("sniffer");
        snifferJSON.put("remote_broker", rBroker);
    }
    
    public void setLocalBroker(JSONObject config) throws JSONException{
        System.out.println("Setting local broker...");
        JSONObject configBroker = config.getJSONObject("local_broker");
        String port = configBroker.getString("port");
        String ip = configBroker.getString("ip");
        if (ip.equals(IP_AUTO_DETECTED)) {
            System.out.println("Autodetecting the IP off that machine...");
            NetworkOperations net = new NetworkOperations();
            ip = net.getLocalIP();
        }
        JSONObject localBroker = new JSONObject();
        localBroker.put("ip", ip);
        localBroker.put("port", port);
        snifferJSON.put("local_broker", localBroker);
        System.out.println("Sniffer IP: " + ip + ", Port: " + port);
    }
    
    public void registSniffer() throws JSONException{
        ListReader reader = new ListReader(listPath);
        
        if(reader.findSniffer(snifferID) < 0){
            System.out.println("Service wasn't registered in the list.");
            
            //add sniffet to list
            System.out.println("Adding sniffer to the list...");
            ListWriter writer = new ListWriter(listPath);
            writer.setList(reader.getList());
            writer.addSniffer(snifferJSON);
            writer.saveListFile();
            
            //register on fiware server
            //RegistHTTP fiwareHTTP = new RegistHTTP(listPath);
            //fiwareHTTP.registService(new JSONObject(snifferJSON, JSONObject.getNames(snifferJSON)));
            
        } else {
            System.out.println("Service was registered in the list.");
        }
        
        //register on other sniffers
        System.out.println("Sendding the new sniffer resgist to the network...");
        BroadcastSniffers send = new BroadcastSniffers(listPath, snifferID);
        send.sendObject(REGIST_OPERATION, snifferJSON);       
    }
}
