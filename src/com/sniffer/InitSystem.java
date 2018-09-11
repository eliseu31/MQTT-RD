/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer;

import com.sniffer.list.operations.ListSubscriber;
import com.sniffer.list.operations.RegistSniffer;
import com.sniffer.list.sniffer.SnifferThread;
import com.sniffer.udp.CheckNetworkThread;
import com.sniffer.list.utils.ListReader;
import com.sniffer.list.utils.ListWriter;
import com.sniffer.udp.NetworkOperations;
import java.io.File;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import io.moquette.server.Server;
import java.io.FileWriter;
import java.util.Properties;

/**
 *
 * @author eliseu
 */
public class InitSystem {
    private static final String MQTT_PORT = "1883";
    private String configPath = "/src/com/sniffer/resources/config1.json";
    private String logPath = "/src/com/sniffer/resources/logMessages.csv"; 
    private static final String IP_AUTO_DETECTED = "autodetected";
    private static final String TYPE_INTERNET = "internet";
    private String listPath;
    
    public void startSniffer() throws JSONException, InterruptedException, IOException{
        System.out.println("Starting the sniffer...");
        //Configuration file
        String rootPath = new File("").getAbsolutePath();
        JSONObject config = getConfig(rootPath);
        
        //Reset the list
        ListWriter writer = new ListWriter(listPath);
        writer.resetList();
        
        //Get the network parametres 
        JSONObject networkJSON = config.getJSONObject("network");
        String networkIP = networkJSON.getString("ip");
        String networkPort = networkJSON.getString("port");
        NetworkOperations net = new NetworkOperations();
        String listIP = net.sendPacketNetwork(networkIP, Integer.parseInt(networkPort));
        
        String snifferID = config.getString("sniffer_id");
        String snifferType = config.getString("sniffer_type");
        
        //start mosquitto broker
        String startBroker = config.getString("start_broker");
        if ("yes".equals(startBroker)) {
            JSONObject configBroker = config.getJSONObject("local_broker");
            String port = configBroker.getString("port");
            String ip = configBroker.getString("ip");
            if (ip.equals(IP_AUTO_DETECTED)) {
                ip = net.getLocalIP();
            }
            startMQTTBroker(ip, port);
        }
        
        //reset the logPath
        logPath = rootPath.concat(logPath);
        File file = new File(logPath);
        FileWriter fw = new FileWriter(file,false);
        fw.append("LOG WITH THE MESSAGES\n");
        fw.close();
        
        if (listIP != null) {
            //Get the list from other sniffer
            ListSubscriber subList = new ListSubscriber(listPath);
            subList.getLocalList(snifferID, listIP, MQTT_PORT);
            
        }else if (snifferType.equals(TYPE_INTERNET)) {
            ListSubscriber subList = new ListSubscriber(listPath);
            subList.getRemoteList(snifferID, config);
        }
        
        //Register the sniffer if necessary
        RegistSniffer serviceReg = new RegistSniffer(listPath);
        serviceReg.createSnifferRegist(snifferID, snifferType, config.getString("transport"), networkJSON);
        serviceReg.setLocalBroker(config);
        if (snifferType.equals(TYPE_INTERNET)) {
            serviceReg.setRemoteBroker(config);
            serviceReg.registSniffer();
        } else {
            serviceReg.registSniffer();
        }    
        
        //Create thread for the devices registration
        (new Thread(new CheckNetworkThread(networkIP, Integer.parseInt(networkPort), listPath, snifferID), "NetworkChecker")).start();
        
        //Create thread for the devices registration
        (new Thread(new SnifferThread(listPath, snifferID), "SnifferThread")).start();
        
        //Thread to monitor the machine resources
        (new Thread(new MonitorResources())).start();
    }
    
    private void startMQTTBroker(String ip, String port){
        try {
            System.out.println("\nStarting the MQTT broker...");
            
            Properties prop = new Properties();
            prop.setProperty("port", port);
            prop.setProperty("host", ip);
            prop.setProperty("allow_anonymous", "true");
            
            Server mqttBroker = new Server();
            mqttBroker.startServer(prop);
            
            System.out.println("Moquette MQTT broker started, press ctrl-c to shutdown..");
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.out.println("\nStopping moquette MQTT broker..");
                    mqttBroker.stopServer();
                    System.out.println("Moquette MQTT broker stopped");
                }
            });
        } catch (IOException ex) {
            System.err.println("Cann't execute the MQTT broker.");
        }
    }
    
    private JSONObject getConfig(String rootPath) throws JSONException{
        configPath = rootPath.concat(configPath);
        
        ListReader reader = new ListReader(configPath);
        JSONObject config = reader.getList();
        System.out.println("Configuration file path: " + configPath);
        listPath = config.getString("list_path");
        listPath = rootPath.concat(listPath);
        return config;
    }
}
