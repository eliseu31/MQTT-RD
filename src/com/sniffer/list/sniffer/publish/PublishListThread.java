/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.sniffer.publish;

import com.sniffer.list.utils.ListReader;
import com.sniffer.mqtt.GenericClient;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eliseu
 */
public class PublishListThread implements Runnable{
    private final String listPath;
    private static final String TOPIC_LIST = "/getlist";
    private static final int MICRO_SECONDS = 6000;
    private final GenericClient broker;

    public PublishListThread(String listPath, GenericClient broker) {
        this.listPath = listPath;
        this.broker = broker;
    }
    
    @Override
    public void run() {
        System.out.println("Publishing the list in this broker: " + broker.getClient().getCurrentServerURI());
        ListReader reader = new ListReader(listPath);
        try {
            while (true) {                
                reader.readFile();
                broker.publish(reader.getList().toString(), TOPIC_LIST);
                Thread.sleep(MICRO_SECONDS);
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(PublishListThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
