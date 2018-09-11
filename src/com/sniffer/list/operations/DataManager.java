/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.operations;

import com.sniffer.list.sniffer.BrokerData;
import com.sniffer.list.utils.ListReader;
import com.sniffer.mqtt.BasicClientMQTT;
import com.sniffer.mqtt.GenericClient;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public class DataManager {
    private final String listPath;
    private final String snifferID;
    private final ListReader reader;
    private static final String TYPE_INTERNET = "internet";
    private static final String TYPE_LOCAL = "local";
    
    public DataManager(String listPath, String snifferID) throws JSONException {
        this.listPath = listPath;
        this.snifferID = snifferID;
        
        reader = new ListReader(listPath);
    }
    
    public void initDataBrokers(GenericClient internetBroker, Map<String,GenericClient> dataBrokers) throws JSONException{
        List<String[]> networkSniffers = reader.getSniffers(snifferID, TYPE_LOCAL);
        for (String[] localSniffer : networkSniffers) {
            addDataSniffer(localSniffer[0], internetBroker, dataBrokers);
        }
    }
    
    public void addDataSniffer(String newSnifferID, GenericClient internetBroker, Map<String,GenericClient> dataBrokers) throws JSONException{
        System.out.println("Subscribing data from sniffer: " + newSnifferID);
        String snifferType = reader.getSnifferType(snifferID);
        String snifferNetwork = reader.getSnifferNetwork(snifferID);
        String newSnifferNetwork = reader.getSnifferNetwork(newSnifferID);
        if (snifferType.equals(TYPE_INTERNET) && newSnifferNetwork.equals(snifferNetwork)) {
            BrokerData dataSniffer = new BrokerData(listPath, newSnifferID, internetBroker);
            String[] brokerConn = reader.getSniffeLocalBroker(newSnifferID);
            dataSniffer.connectInternalBroker(newSnifferID + "DataSubscriber", brokerConn[0], brokerConn[1]);
            subscribeSnifferDevices(newSnifferID, dataSniffer);
            dataBrokers.put(newSnifferID, dataSniffer);
        }
    }
    
    public void addDataDevice(String newDeviceID, String newSnifferID, Map<String,GenericClient> dataBrokers) throws JSONException{
        System.out.println("Subscribing data from device: " + newDeviceID + ", at sniffer: " + newSnifferID);
        String snifferType = reader.getSnifferType(snifferID);
        String snifferNetwork = reader.getSnifferNetwork(snifferID);
        String newSnifferNetwork = reader.getSnifferNetwork(newSnifferID);
        if (snifferType.equals(TYPE_INTERNET) && newSnifferNetwork.equals(snifferNetwork)) {
            GenericClient dataSniffer = dataBrokers.get(newSnifferID);
            subscribeDeviceTopics(newSnifferID, newDeviceID, dataSniffer);
            dataBrokers.put(newSnifferID, dataSniffer);
        }
    }
    
    private void subscribeSnifferDevices(String subSnifferID, GenericClient mqtt) throws JSONException{
        String [][] devices = reader.getSnifferDevices(subSnifferID);
        for (String[] device : devices) {
            subscribeDeviceTopics(subSnifferID, device[0], mqtt);
        }
    }
    
    public void subscribeDeviceTopics(String subSnifferID, String subDeviceID, GenericClient mqtt) throws JSONException {
        String[] deviceAttributes = reader.getDeviceAttributes(subSnifferID, subDeviceID);
        for (String deviceAttribute : deviceAttributes) {
            String topicString = "/" + subDeviceID + "/attrs/" + deviceAttribute;
            mqtt.subscribe(topicString);
            Queue<String> topicData = new LinkedList<>();
            mqtt.getTopicsMap().put(topicString, topicData);
        }
    }
    
    public void newDataMessageDevice(String message, String topic, Map<String, Queue<String>> topicsMap, int limitMessages) throws JSONException{
        if (topicsMap.containsKey(topic)) {
            Queue<String> topicData = topicsMap.get(topic);
            topicData.add(message);
            if (topicData.size() > limitMessages) {
                while (topicData.size() > 1) {                    
                    topicData.remove();
                }
                sendData(topicData.remove(), topic);
            }
            topicsMap.put(topic, topicData);
        }
    }
    
    private synchronized void sendData(String message, String topic) throws JSONException {
        String ipAndPort[] = reader.getSniffeLocalBroker(snifferID);
        String snifferURL = "tcp://" + ipAndPort[0] + ":" + ipAndPort[1];
        BasicClientMQTT mqtt = new BasicClientMQTT();
        mqtt.connect(snifferURL, snifferID + "Publisher");
        mqtt.publish(message, topic);
        mqtt.disconnect();
    }

    
    public void newDataMessageSniffer(String message, String topic, GenericClient internetBroker, Map<String, Queue<String>> topicsMap, int limitMessages){
        if (topicsMap.containsKey(topic)) {
            Queue<String> topicData = topicsMap.get(topic);
            topicData.add(message);
            if (topicData.size() > limitMessages) {
                while (topicData.size() > 1) {                    
                    topicData.remove();
                }
                internetBroker.publish(topicData.remove(), topic);
            }
            topicsMap.put(topic, topicData);
        }        
    }
    
    public void deleteDataSniffer(String delSnifferID, Map<String,GenericClient> dataBrokers) throws JSONException{
        System.out.println("Removing data subscriber from sniffer: " + delSnifferID);
        GenericClient delDataBroker = dataBrokers.remove(delSnifferID);
        unsubscribeSnifferDevices(delSnifferID, delDataBroker);
        delDataBroker.getTopicsMap().clear();
        delDataBroker.disconnect();
    }
    
    public void deleteDataDevice(String delSnifferID, String delDeviceID, Map<String,GenericClient> dataBrokers) throws JSONException{
        System.out.println("Removing data subscriber from device: " + delDeviceID + ", at sniffer: " + delSnifferID);
        GenericClient delDataBroker = dataBrokers.get(delSnifferID);
        unsubscribeDeviceTopics(delSnifferID, delDeviceID, delDataBroker);
    }
    
    private void unsubscribeSnifferDevices(String unsubSnifferID, GenericClient mqtt) throws JSONException{
        String [][] devices = reader.getSnifferDevices(unsubSnifferID);
        for (String[] device : devices) {
            unsubscribeDeviceTopics(unsubSnifferID, device[0], mqtt);
        }
    }
    
    private void unsubscribeDeviceTopics(String unsubSnifferID, String unsubDeviceID, GenericClient mqtt) throws JSONException{
        String[] deviceAttributes = reader.getDeviceAttributes(unsubSnifferID, unsubDeviceID);
        for (String deviceAttribute : deviceAttributes) {
            String topicString = "/" + unsubDeviceID + "/attrs/" + deviceAttribute;
            mqtt.unsubscribe(topicString);
        }        
    }

}
