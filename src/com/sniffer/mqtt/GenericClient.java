/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.mqtt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public abstract class GenericClient extends BasicClientMQTT{
    private final Map<String, Queue<String>> topicsMap = new HashMap<>();
    private String logPath = "/src/com/sniffer/resources/logMessages.csv"; 

    public GenericClient() {
        String rootPath = new File("").getAbsolutePath();
        logPath = rootPath.concat(logPath);
    }
    
    private final MqttCallback callback = new MqttCallback() {

        @Override
        public void connectionLost(Throwable cause) {
            System.out.println("Connection lost on instance \"" + getClient().getClientId()
                    + "\" with cause \"" + cause.getMessage() + "\" Reason code "
                    + ((MqttException) cause).getReasonCode() + "\" Cause \""
                    + ((MqttException) cause).getCause() + "\"");
            
            lostConnection();
            try {
                System.out.println("Reconecting to the server...");
                getClient().reconnect();
                System.out.println("Done.");
            } catch (MqttException ex) {
                System.out.println("Can't reconnect to the broker.");
            }
        }

        @Override
        public void messageArrived(String string, MqttMessage mm) throws JSONException {
            //System.out.println("Message arrived: \"" + mm.toString()
            //        + "\" on topic \"" + string + "\" for instance \""
            //        + getClient().getClientId() + "\"");
            
            processMessage(mm.toString(), string);
            saveMessage(string, mm.toString());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {
            //System.out.println("Delivery token \"" + imdt.hashCode()
            //        + "\" received by instance \"" + getClient().getClientId() + "\"");
        }
    };
    
    public Map<String, Queue<String>> getTopicsMap(){
        return topicsMap;
    }
    
    public void connectExternalBroker(String clientID, String ip, String port, String user, String pass) {
        String brokerURL = "tcp://" + ip + ":" + port;
        setUserPass(user, pass);
        connectWithCallback(brokerURL, clientID, callback);
    }
    
    public void connectInternalBroker(String clientID, String ip, String port){
        String brokerURL = "tcp://" + ip + ":" + port;
        connectWithCallback(brokerURL, clientID, callback);    
    }
    
    private void saveMessage(String topic, String message){
        StringBuilder line = new StringBuilder();
        line.append(topic);
        line.append(",");
        line.append(message);
        line.append("\n");
        
        writeMessage(line.toString());
    }
    
    private synchronized void writeMessage(String line){
        try {
            Writer output = new BufferedWriter(new FileWriter(logPath, true));
            output.append(line);
            output.close();
            
        } catch (IOException ex) {
            Logger.getLogger(GenericClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    abstract public void processMessage(String message, String topic);
    
    abstract public void lostConnection();
}
