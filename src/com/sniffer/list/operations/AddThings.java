/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.operations;

import com.sniffer.list.utils.ListReader;
import com.sniffer.list.utils.ListWriter;
import com.sniffer.mqtt.GenericClient;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class AddThings {
    private final String listPath;
    private final String snifferID;
    private final ListReader reader;
    
    private GenericClient internetBroker;
    private Map<String,GenericClient> dataBrokers;

    public AddThings(String listPath, String snifferID) {
        this.listPath = listPath;
        this.snifferID = snifferID;
        reader = new ListReader(listPath);
    }
    
    public void setSubscritionActive(GenericClient internetBroker, Map<String,GenericClient> dataBrokers){
        this.internetBroker = internetBroker;
        this.dataBrokers = dataBrokers;
    }
    
    public void addThing(JSONObject newSnifferJSON) throws JSONException {
        String newSnifferID = newSnifferJSON.getString("sniffer_id");
        int positionSniffer = reader.findSniffer(newSnifferID);
        if (positionSniffer < 0) {
            System.out.println("Sniffer wasn't registered in the list.");
            
            addSniffer(newSnifferJSON);
            
            DataManager manager = new DataManager(listPath, snifferID);
            manager.addDataSniffer(newSnifferID, internetBroker, dataBrokers);

        } else {
            System.out.println("Sniffer was registered in the list.");

            //check new devices in that sniffer
            JSONArray newDevices = newSnifferJSON.getJSONArray("devices");

            JSONObject device;
            for (int i = 0; i < newDevices.length(); i++) {
                device = (JSONObject) newDevices.get(i);

                //check if new device are registered on list
                String newDeviceID = device.getString("device_id");
                int positionDevice = reader.findDevice(positionSniffer, newDeviceID);
                if (positionDevice < 0) {
                    addDevice(newSnifferID, device);
                    
                    DataManager manager = new DataManager(listPath, snifferID);
                    manager.addDataDevice(newDeviceID, newSnifferID, dataBrokers);
                }
            }
        }
    }
    
    public void addSniffer(JSONObject snifferJSON) throws JSONException {
        //add sniffer to list
        System.out.println("Adding the new sniffer to the list.");
        ListWriter writer = new ListWriter(listPath);
        writer.setList(reader.getList());
        writer.addSniffer(snifferJSON);
        writer.saveListFile();       
    }
    
    public void addDevice(String snifferID, JSONObject device) throws JSONException{
        System.out.println("Adding the new device to the list.");
        ListWriter writer = new ListWriter(listPath);
        writer.setList(reader.getList());
        writer.addDevice(snifferID, device);
        writer.saveListFile();    
    }
}
