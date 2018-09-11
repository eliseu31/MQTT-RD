/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.sniffer;

import com.sniffer.list.operations.TimeoutsChecker;
import com.sniffer.list.operations.AddThings;
import com.sniffer.list.operations.DataManager;
import com.sniffer.list.operations.DeleteThings;
import com.sniffer.list.utils.ListReader;
import com.sniffer.mqtt.BroadcastSniffers;
import com.sniffer.mqtt.GenericClient;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class SnifferOperations {
    private final String snifferID;
    private final String listPath;
    private final String snifferType;
    
    private static final String REGIST_OPERATION = "regist";
    private static final String DELETE_DEVICE_OPERATION = "deletedevice";
    private static final String DELETE_SNIFFER_OPERATION = "deletesniffer";
    private static final String PING_ALIVE = "pingalive";
    
    private static final String TYPE_INTERNET = "internet";
    private static final String CONF_BROKER_EXTERNAL = "internet";
    private static final String CONF_BROKER_LOCAL = "local";
    private final String confBroker;
    
    private GenericClient internetBroker;
    private Map<String,GenericClient> dataBrokers;
    private final TimeoutsChecker timer;
    
    public SnifferOperations(String listPath, String snifferID, String confBroker) throws JSONException {
        this.listPath = listPath;
        this.snifferID = snifferID;
        this.confBroker = confBroker;
        
        ListReader reader = new ListReader(listPath);
        this.snifferType = reader.getSnifferType(snifferID);
        
        timer = new TimeoutsChecker(listPath, snifferID, confBroker);
        timer.resetTimeouts();
    }
    
    protected void setSubscritionsActive(GenericClient internetBroker, Map<String,GenericClient> dataBrokers) throws JSONException{
        this.internetBroker = internetBroker;
        this.dataBrokers = dataBrokers;
        
        DataManager manager = new DataManager(listPath, snifferID);
        manager.initDataBrokers(internetBroker, dataBrokers);
        
        timer.setSubscritionsActive(dataBrokers);
    }
    
    public void snifferProtocol(String message) throws JSONException{        
        JSONObject messageJSON = new JSONObject(message);
        String operation = messageJSON.getString("operation");
        JSONObject objectJSON = (JSONObject) messageJSON.get("object");
        
        switch (operation) {
            case REGIST_OPERATION:
                System.out.println("\nNew external operation in the " + confBroker +" broker.");
                System.out.println("New request for regist operation.");
                AddThings add = new AddThings(listPath, snifferID);
                
                if (snifferType.equals(TYPE_INTERNET)) {
                    add.setSubscritionActive(internetBroker, dataBrokers);
                    middleCommunication(operation, objectJSON);
                }
                add.addThing(objectJSON);
                timer.resetTimeouts();                
                break;

            case DELETE_DEVICE_OPERATION:
                System.out.println("\nNew external operation in the " + confBroker +" broker.");
                String rSnifferID = objectJSON.getString("sniffer_id");
                String rDeviceID = objectJSON.getString("device_id");
                System.out.println("New request to delete device: " + rDeviceID + ", at sniffer: " + rSnifferID);
                
                ListReader reader = new ListReader(listPath);
                String rSnifferNetwork = reader.getSnifferNetwork(rSnifferID);
                String snifferNetwork = reader.getSnifferNetwork(snifferID);
                if (snifferType.equals(TYPE_INTERNET) && rSnifferNetwork.equals(snifferNetwork)) {
                    DataManager manager = new DataManager(listPath, snifferID);
                    manager.deleteDataDevice(rSnifferID, rDeviceID, dataBrokers);
                }
                
                DeleteThings deleteDevice = new DeleteThings(listPath, snifferID);
                deleteDevice.deleteDevice(rSnifferID, rDeviceID);
                timer.resetTimeouts();
                
                if (snifferType.equals(TYPE_INTERNET)) {
                    middleCommunication(operation, objectJSON);
                }              
                break;

            case DELETE_SNIFFER_OPERATION:
                System.out.println("\nNew external operation in the " + confBroker +" broker.");               
                String removeSnifferID = objectJSON.getString("sniffer_id");
                System.out.println("New request to delete: " + removeSnifferID);
                
                ListReader reader1 = new ListReader(listPath);
                
                if(removeSnifferID.equals(snifferID)){
                    System.out.println("Trying to remove this sniffer.");
                    
                    int snifferPosition = reader1.findSniffer(snifferID);
                    JSONArray sniffers = reader1.getList().getJSONArray("sniffers");
                    JSONObject mySniffer = sniffers.getJSONObject(snifferPosition);
                    
                    System.out.println("Registering this sniffer in the network...");
                    //register on other sniffers
                    middleCommunication(REGIST_OPERATION, mySniffer);
                    
                }else {
                    DeleteThings deleteSniffer = new DeleteThings(listPath, snifferID);
                    
                    String removeSnifferNetwork = reader1.getSnifferNetwork(removeSnifferID);
                    String snifferNetwork1 = reader1.getSnifferNetwork(snifferID);
                    if (snifferType.equals(TYPE_INTERNET) && removeSnifferNetwork.equals(snifferNetwork1)) {
                        DataManager manager = new DataManager(listPath, snifferID);
                        manager.deleteDataSniffer(removeSnifferID, dataBrokers);
                    }
                    
                    deleteSniffer.deleteSniffer(removeSnifferID);
                    timer.resetTimeouts();
                    
                    if (snifferType.equals(TYPE_INTERNET)){
                        middleCommunication(operation, objectJSON);
                    }
                }
                break;
                
            case PING_ALIVE:
                String pSnifferID = objectJSON.getString("sniffer_id");
                timer.setTimeout(pSnifferID);
                break;

            default:
                break;
        }
    }
    
    private void middleCommunication(String operation, JSONObject objectJSON) throws JSONException {
        BroadcastSniffers broadcast = new BroadcastSniffers(listPath, snifferID);

        switch (confBroker) {
            case CONF_BROKER_EXTERNAL:
                //broadcast to the rest of the network
                System.out.println("Sendding the new thing registration to the rest of local network.");
                broadcast.sendLocal(operation, objectJSON);
                break;
                
            case CONF_BROKER_LOCAL:
                //send to the internet
                System.out.println("Sendding the new thing registration to the rest of internet sniffers.");
                broadcast.sendInternet(operation, objectJSON);
                break;
                
            default:
                break;
        }
    }
}
