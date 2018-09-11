/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.operations;

import com.sniffer.mqtt.BroadcastSniffers;
import com.sniffer.list.utils.ListReader;
import com.sniffer.list.utils.ListWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class DeleteThings {
    private static final String DELETE_DEVICE_OPERATION = "deletedevice";
    private static final String DELETE_SNIFFER_OPERATION = "deletesniffer";
    private final String listPath;
    private final String snifferID;
    private final ListReader reader;
    
    public DeleteThings(String listPath, String snifferID) {
        this.listPath = listPath;
        this.snifferID = snifferID;
        this.reader = new ListReader(listPath);
    }
    
    public void deleteSniffer(String rSnifferID) throws JSONException{
        //remove device from list
        System.out.println("Deleting the sniffer: " + rSnifferID);
        ListWriter writer = new ListWriter(listPath);
        writer.setList(reader.getList());
        writer.removeSniffer(rSnifferID);
        writer.saveListFile();     
    }
    
    public void deleteDevice(String rSnifferID, String rDeviceID) throws JSONException{
        //remove device from list
        System.out.println("Deleting the device: " + rDeviceID + ", that corresponds to sniffer: " + rSnifferID);
        ListWriter writer = new ListWriter(listPath);
        writer.setList(reader.getList());
        writer.removeDevice(rSnifferID, rDeviceID);
        writer.saveListFile();        
    }
    
    public void sendDeleteDevice(String rSnifferID, String rDeviceID) throws JSONException{
        JSONArray sniffersArray = reader.getList().getJSONArray("sniffers");
        int positionSniffer = reader.findSniffer(rSnifferID);
        JSONObject snifferJSON = sniffersArray.getJSONObject(positionSniffer);
        
        JSONArray devicesArray = snifferJSON.getJSONArray("devices");
        int positionDevice = reader.findDevice(positionSniffer, rDeviceID);
        JSONObject deviceJSON = devicesArray.getJSONObject(positionDevice);
        
        deviceJSON.put("sniffer_id", rSnifferID);
        
        System.out.println("Sending a delete device operation to the rest of the network.");
        BroadcastSniffers broad = new BroadcastSniffers(listPath, snifferID);
        broad.sendObject(DELETE_DEVICE_OPERATION, deviceJSON);
    }
    
    public void sendDeleteSniffer(String rSnifferID) throws JSONException{
        JSONObject snifferJSON = new JSONObject();
        snifferJSON.put("sniffer_id", rSnifferID);
        
        System.out.println("Sending a delete sniffer operation to the rest of the network.");
        BroadcastSniffers broad = new BroadcastSniffers(listPath, snifferID);
        broad.sendObject(DELETE_SNIFFER_OPERATION, snifferJSON);        
    }
}
