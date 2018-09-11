/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.operations;

import com.sniffer.list.sniffer.BrokerInternal;
import com.sniffer.list.utils.ListReader;
import com.sniffer.mqtt.GenericClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public class TimeoutsChecker {
    private final String snifferID;
    private final String listPath;
    private final String confBroker;
    private static final int TIMER_INTERVAL = 40 * 1000;
    private static final String TYPE_INTERNET = "internet";
    private Timer timer = new Timer();
    private final Map<String,Boolean> timeouts = new HashMap<>();
    private Map<String,GenericClient> dataBrokers;

    public TimeoutsChecker(String listPath, String snifferID, String confBroker) {
        this.listPath = listPath;
        this.snifferID = snifferID;
        this.confBroker = confBroker;
        System.out.println("Configuring the timer responsible by the sniffers timeouts...");
    }
    
    public void setSubscritionsActive(Map<String,GenericClient> dataBrokers){
        this.dataBrokers = dataBrokers;
    }
    
    public void setTimeout(String pSnifferID){
        timeouts.put(pSnifferID, true);
    }
    
    public void resetTimeouts() throws JSONException{
        timeouts.clear();
        timer.cancel();
        timer.purge();
        
        ListReader reader = new ListReader(listPath);
        List<String[]> sniffersList = reader.getSniffers(snifferID, confBroker);
        for (String[] iterator : sniffersList) {
            // check only in the network
            if(!iterator[0].equals(snifferID)){
                timeouts.put(iterator[0], false);
            }
        }
        
        timer = new Timer();
        timer.scheduleAtFixedRate(createTask(), TIMER_INTERVAL, TIMER_INTERVAL);
    }
    
    private TimerTask createTask(){
        
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    //Cheack if all timeouts
                    for (Map.Entry<String, Boolean> entry : timeouts.entrySet()) {
                        String rSnifferID = entry.getKey();
                        Boolean timeoutState = entry.getValue();

                        if (!timeoutState) {
                            System.out.println("\nTimeout in sniffer: " + rSnifferID);
                            System.out.println("Starting delete sniffer operation...");

                            ListReader reader = new ListReader(listPath);
                            String snifferType = reader.getSnifferType(snifferID);
                            String rSnifferNetwork = reader.getSnifferNetwork(rSnifferID);
                            String snifferNetwork = reader.getSnifferNetwork(snifferID);
                            if (snifferType.equals(TYPE_INTERNET) && rSnifferNetwork.equals(snifferNetwork)) {
                                DataManager manager = new DataManager(listPath, snifferID);
                                manager.deleteDataSniffer(rSnifferID, dataBrokers);
                            }

                            DeleteThings del = new DeleteThings(listPath, snifferID);
                            del.deleteSniffer(rSnifferID);
                            timeouts.remove(rSnifferID);
                            del.sendDeleteSniffer(rSnifferID);
                            break;
                        }
                    }
                    resetTimeouts();

                } catch (JSONException ex) {
                    Logger.getLogger(BrokerInternal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        
        return task;
    }
}
