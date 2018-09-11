/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class ListReader {
    private JSONObject listJSON;
    private final String listPath;
    private static final String TYPE_INTERNET = "internet";
    private static final String TYPE_LOCAL = "local";

    public ListReader(String listPath) {
        this.listPath = listPath;
        this.readFile();
        
    }
    
    public final synchronized void readFile() {
        File f = new File(listPath);
        FileReader file;
        JSONObject json = null;
        try {
            file = new FileReader(f);

            char[] cbuf = new char[(int) f.length()];
            file.read(cbuf);
            String list = new String(cbuf);
            json = new JSONObject(list);
            
        } catch (FileNotFoundException ex) {
            System.err.println("File not found in this path: " + listPath);
        } catch (IOException | JSONException ex) {
            Logger.getLogger(ListReader.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Can not read the list from the file.");
        }
        
        this.listJSON = json;
    }
    
    /*
    RETURN the posisition of the sniffer
    OR -1 if doesn't exist
    */
    public int findSniffer(String snifferID) throws JSONException{
        JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
        
        JSONObject iterator;
        for (int i = 0; i < sniffersArray.length(); i++) {
            iterator = sniffersArray.getJSONObject(i);
            
            if(iterator.getString("sniffer_id").equals(snifferID)){
                return i;
            }
        }
        
        return -1;
    }
    
    public int findDevice(int positionSniffer, String deviceID) throws JSONException{
        JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
        JSONObject snifferJSON = (JSONObject) sniffersArray.get(positionSniffer);
        JSONArray devicesArray = snifferJSON.getJSONArray("devices");
        
        JSONObject iterator;
        for (int i = 0; i < devicesArray.length(); i++) {
            iterator = devicesArray.getJSONObject(i);
            
            if(iterator.getString("device_id").equals(deviceID)){
                return i;
            }
        }
        return -1;
    }
    
    public JSONObject getList(){
        return listJSON;
    }    
    
    public String getSnifferType(String snifferID) throws JSONException{
        int position = findSniffer(snifferID);
        if(position < 0){
            System.err.println("Sniffer doesn't exist");
            return null;
        }
        
        JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
        JSONObject snifferJSON = (JSONObject) sniffersArray.get(position);
        
        return snifferJSON.getString("sniffer_type");
    }
    
    public String[] getSniffeLocalBroker(String snifferID) throws JSONException{
        int position = findSniffer(snifferID);
        if(position < 0){
            System.err.println("Sniffer doesn't exist");
            return null;
        }
        
        JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
        JSONObject snifferJSON = (JSONObject) sniffersArray.get(position);
        JSONObject broker = snifferJSON.getJSONObject("local_broker");
        String url[] = new String[2];
        url[0] = broker.getString("ip");
        url[1] = broker.getString("port");
        return url;
    }
    
    public String[] getSnifferRemoteBroker(String snifferID) throws JSONException{
        int positionSniffer = findSniffer(snifferID);
        if(positionSniffer < 0){
            System.err.println("Sniffer doesn't exist");
            return null;
        }
        
        JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
        JSONObject snifferJSON = (JSONObject) sniffersArray.get(positionSniffer);
        JSONObject rBroker = snifferJSON.getJSONObject("remote_broker");
        
        String[] auth = new String[4];
        auth[0] = rBroker.getString("ip");
        auth[1] = rBroker.getString("port");
        auth[2] = rBroker.getString("user");
        auth[3] = rBroker.getString("pass");
        
        return auth;
    }   
    
    public String getSnifferNetwork(String snifferID) throws JSONException{
        int position = findSniffer(snifferID);
        if (position < 0) {
            System.err.println("Sniffer doesn't exist");
            return null;
        }

        JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
        JSONObject snifferJSON = (JSONObject) sniffersArray.get(position);
        JSONObject networkJSON = snifferJSON.getJSONObject("network");

        return networkJSON.getString("network_id");
    }    
    
    /*
    RETURNS devices IDs and MQTT hosts
    null if doesn't exist
    */
    public String[][] getSnifferDevices(String snifferID) throws JSONException{
        String prefix = "tcp://";
        int position = findSniffer(snifferID);
        if(position < 0){
            System.err.println("Sniffer doesn't exist");
            return null;
        }
        
        JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
        JSONObject snifferJSON = (JSONObject) sniffersArray.get(position);
        JSONArray devicesArray = snifferJSON.getJSONArray("devices");
        
        String[][] matrix = new String[devicesArray.length()][2];
        JSONObject iterator;
        JSONObject localBroker;
        for (int i = 0; i < devicesArray.length(); i++) {
            iterator = (JSONObject) devicesArray.get(i);
            localBroker = snifferJSON.getJSONObject("local_broker");            
            matrix[i][0] = iterator.getString("device_id");
            matrix[i][1] = prefix + localBroker.getString("ip") + ":" + localBroker.getString("port");
        }
        
        return matrix;
    }
    
    /*
    RETURNS device's attributes string list
    null if doesn't exist
    */
    public String[] getDeviceAttributes(String snifferID, String deviceID) throws JSONException{
        int positionSniffer = findSniffer(snifferID);
        if(positionSniffer < 0){
            System.err.println("Sniffer doesn't exist");
            return null;
        }
        
        int positionDevice = findDevice(positionSniffer, deviceID);
        if(positionDevice < 0){
            System.err.println("Device doesn't exist");
            return null;
        }
        
        JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
        JSONObject snifferJSON = (JSONObject) sniffersArray.get(positionSniffer);
        JSONArray devicesArray = snifferJSON.getJSONArray("devices");
        JSONObject deviceJSON = (JSONObject) devicesArray.get(positionDevice);
        JSONArray attributes = deviceJSON.getJSONArray("attributes");
        
        String[] vector = new String[attributes.length()];
        JSONObject iterator;
        for (int i = 0; i < attributes.length(); i++) {
            iterator = (JSONObject) attributes.get(i);
            vector[i] = iterator.getString("object_id");
        }
        
        return vector;
    }
    
    public List<String[]> getSniffers(String snifferID, String confBroker) throws JSONException{
        List<String[]> sniffersList = null;
        
        switch (confBroker) {
            case TYPE_INTERNET:
                // get all internet sniffers
                sniffersList = getInternetSniffers();
                break;
            case TYPE_LOCAL:
                // get the sniffers in the same network
                sniffersList = getNetworkSniffers(snifferID);
                break;
            default:
                break;

        }
        return sniffersList;
    }
    
    private List<String[]> getNetworkSniffers(String snifferID) throws JSONException{
        String snifferNetwork = getSnifferNetwork(snifferID);
        JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
        List<String[]> sniffersList = new LinkedList<>();
        
        for (int i = 0; i < sniffersArray.length(); i++) {
            JSONObject iteratorJSON = sniffersArray.getJSONObject(i);
            String iteratorID = iteratorJSON.getString("sniffer_id");
            String iteratorNetwork = getSnifferNetwork(iteratorID);
            
            if(iteratorNetwork.equals(snifferNetwork)){
                String[] listItem = new String[3];
                listItem[0] = iteratorID;
                JSONObject lBroker = iteratorJSON.getJSONObject("local_broker");
                listItem[1] = lBroker.getString("ip");
                listItem[2] = lBroker.getString("port");
                sniffersList.add(listItem);
            }
        }
        
        return sniffersList;
    }
    
    private List<String[]> getInternetSniffers() throws JSONException{
        List<String[]> sniffersList = new LinkedList<>();
        JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
        
        for (int i = 0; i < sniffersArray.length(); i++) {
            JSONObject iteratorJSON = sniffersArray.getJSONObject(i);
            String iteratorID = iteratorJSON.getString("sniffer_id");
            String iteratorType = getSnifferType(iteratorID);
            
            if (iteratorType.equals(TYPE_INTERNET)) {
                String[] listItem = new String[3];
                listItem[0] = iteratorID;
                JSONObject rBroker = iteratorJSON.getJSONObject("remote_broker");
                listItem[1] = rBroker.getString("ip");
                listItem[2] = rBroker.getString("port");
                sniffersList.add(listItem);
            }
        }
        
        return sniffersList;
    }
}
