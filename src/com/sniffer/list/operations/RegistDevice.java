/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.operations;

import com.sniffer.mqtt.BroadcastSniffers;
import com.sniffer.list.utils.ListReader;
import com.sniffer.mqtt.GenericClient;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class RegistDevice {
    private final String listPath;
    private final String snifferID;
    private static final String REGIST_OPERATION = "regist";
    private static final String DEVICE_BROKER = "broker";
    private static final String DEVICE_NO_BROKER = "nobroker";
    private static final String TYPE_INTERNET = "internet";
    private static final String THREAD_NEW_BROKER = "newbroker";
    
    private Map<String,GenericClient> dataBrokers;    
    
    public RegistDevice(String listPath, String snifferID) {
        this.listPath = listPath;
        this.snifferID = snifferID;
    }    

    public void setSubscritionsActive(Map<String,GenericClient> dataBrokers) {
        this.dataBrokers = dataBrokers;        
    }
    
    public void newDevice(String message, GenericClient mqtt, Boolean firstRegist, String[] deviceToken) throws JSONException {
        JSONObject newDeviceJSON = new JSONObject(message);
        ListReader reader = new ListReader(listPath);
        String newDeviceID = newDeviceJSON.getString("device_id");
        String newDeviceType = newDeviceJSON.getString("device_type");
        System.out.println("Registering the device: " + newDeviceID);
        
        switch (newDeviceType) {
            case DEVICE_BROKER:
                try {
                    System.out.println("Registration of a device wiht broker.");
                    
                    // if is a device broker registered by this sniffer
                    deviceToken[0] = THREAD_NEW_BROKER;
                    deviceToken[1] = newDeviceID;
                    JSONObject newBroker = newDeviceJSON.getJSONObject("local_broker");
                    deviceToken[2] = newBroker.getString("ip");
                    deviceToken[3] = newBroker.getString("port");
                    registDevice(newDeviceJSON);
                    System.out.println("Registration request using the sniffer broker.");
                    
                    synchronized (deviceToken) {
                        deviceToken.notify();
                    }
                    
                } catch (JSONException ex) {
                    
                    // if is a device broker discovered by the network checker
                    System.out.println("The JSON doesn't have specify the broker IP.");
                    if (!firstRegist) {
                        System.out.println("Registration request sended by UDP.");
                        deviceToken[1] = newDeviceID;
                        registDeviceWithIP(newDeviceJSON, deviceToken[2], deviceToken[3]);
                        DataManager data = new DataManager(listPath, snifferID);
                        data.subscribeDeviceTopics(snifferID, deviceToken[1], mqtt);
                
                    } else {
                        System.err.println("Error you must specify the broker parametres.");
                    }
                }
                break;
                
            case DEVICE_NO_BROKER:
                // set the ip off the sniffer
                System.out.println("Registration of a device without broker.");
                String[] ipAndPort = reader.getSniffeLocalBroker(snifferID);
                registDeviceWithIP(newDeviceJSON, ipAndPort[0], ipAndPort[1]);
                break;
                
            default:
                break;
        }
        
        String snifferType = reader.getSnifferType(snifferID);
        if (snifferType.equals(TYPE_INTERNET)) {
            DataManager data = new DataManager(listPath, snifferID);
            data.addDataDevice(newDeviceID, snifferID, dataBrokers);
        }
    }
    
    private void registDeviceWithIP(JSONObject deviceJSON, String deviceIP, String port) throws JSONException{
        JSONObject lBroker = new JSONObject();
        lBroker.put("ip", deviceIP);
        lBroker.put("port", port);
        deviceJSON.put("local_broker", lBroker); 
        registDevice(deviceJSON);
    }
    
    private void registDevice(JSONObject newDeviceJSON) throws JSONException {
        ListReader reader = new ListReader(listPath);
        
        int positionSniffer = reader.findSniffer(snifferID);
        String newDeviceID = newDeviceJSON.getString("device_id");
        int positionDevice = reader.findDevice(positionSniffer, newDeviceID);
        if (positionDevice < 0) {
            //regist the device in fiware
            //RegistHTTP http = new RegistHTTP(listPath);
            //http.registDeviceWithIP(new JSONObject(deviceJSON, JSONObject.getNames(deviceJSON)), snifferID);
            
            //add device to the list
            AddThings add = new AddThings(listPath, snifferID);
            add.addDevice(snifferID, newDeviceJSON);

            // get the sniffer JSON updated (with the new device)
            reader.readFile();
            JSONArray sniffersArray = reader.getList().getJSONArray("sniffers");
            JSONObject snifferJSON = (JSONObject) sniffersArray.get(positionSniffer);
            
            //regist the sniffer (with the new device) in the others sniffers
            System.out.println("Sending the new device to the other sniffers.");
            BroadcastSniffers broad = new BroadcastSniffers(listPath, snifferID);
            broad.sendObject(REGIST_OPERATION, snifferJSON);
        }
    }
}
