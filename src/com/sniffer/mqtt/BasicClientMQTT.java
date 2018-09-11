/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.mqtt;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author eliseu
 */
public class BasicClientMQTT {
    private MqttClient client;
    private static final int QOS = 1;
    private final MqttConnectOptions conOptions = new MqttConnectOptions();
    
    public void setUserPass(String user, String pass){
        conOptions.setUserName(user);
        conOptions.setPassword(pass.toCharArray());
    }
    
    public void connectWithCallback(String brokerURL, String clientID, MqttCallback callback) {
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            client = new MqttClient(brokerURL, clientID, persistence);
            client.setCallback(callback);
            conOptions.setCleanSession(true);
            conOptions.setKeepAliveInterval(60);
            conOptions.setAutomaticReconnect(true);
            client.connect(conOptions);
            System.out.println("Connected to broker ID: " + clientID + ", at URL: "+ brokerURL);
            
        } catch (MqttException ex) {
            System.err.println("Cann't connect to broker: " + clientID + ", at URL: "+ brokerURL);
        }
    }
    
    public MqttClient getClient(){
        return client;
    }
    
    public int connect(String brokerURL, String clientID) {
        
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            client = new MqttClient(brokerURL, clientID, persistence);
            conOptions.setCleanSession(true);
            conOptions.setKeepAliveInterval(60);
            conOptions.setAutomaticReconnect(true);
            client.connect(conOptions);
            return 1;
        } catch (MqttException ex) {
            System.err.println("Cann't connect to broker: " + clientID + ", at URL: "+ brokerURL);
            return -1;
        }
    }
    
    public void publish(String msg, String topicString) {
        //System.out.println("Publishing message: " + msg);
        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(QOS);
        
        try {
            client.publish(topicString, message);
        } catch (MqttException ex) {
            System.err.println("Cann't publish in this topic: " + topicString);
            try {
                System.out.println("Trying again...");
                Thread.sleep(500);
                MqttMessage message1 = new MqttMessage(msg.getBytes());
                message1.setQos(0);
                client.publish(topicString, message1);
            } catch (InterruptedException | MqttException ex1) {
                System.err.println("Error trying again to publish...");
            }
            
        }
    }    
    
    public void subscribe(String topicString) {
        System.out.println("Subscribing to topic \"" + topicString
                + "\" for client instance \"" + client.getClientId()
                + "\" using QoS " + QOS + ".");

        try {
            client.subscribe(topicString, QOS);
        } catch (MqttException ex) {
            System.err.println("Cann't subscribe this topic: " + topicString);
        }
    }
    
    public void unsubscribe(String topicString){
        try {
            client.unsubscribe(topicString);
        } catch (MqttException ex) {
            System.err.println("Cann't unsubscribe this topic: " + topicString);
        }
    }
    
    public void disconnect() {
        try {
            client.disconnect();
            client.close();
        } catch (MqttException ex) {
            System.err.println("Cann't disconnect from this broker: " + client.getClientId());
            try {
                System.out.println("Trying again...");
                Thread.sleep(500);
                client.close();
            } catch (InterruptedException | MqttException ex1) {
                Logger.getLogger(BasicClientMQTT.class.getName()).log(Level.SEVERE, null, ex1);
            }            
        }
    }
}
