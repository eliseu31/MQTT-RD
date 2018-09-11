/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eliseu
 */
public class MonitorResources implements Runnable{
    private String logPath = "/src/com/sniffer/resources/machineResources.csv";
    private static final int SAMPLING_RATE = 2000;
    
    @Override
    public void run() {
        try {
            String rootPath = new File("").getAbsolutePath();
            logPath = rootPath.concat(logPath);
            File file = new File(logPath);
            FileWriter fw = new FileWriter(file,false);
            fw.append("Load Average,Heap,Non Heap\n");
            fw.close();
            
            MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
            OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();         
            
            while (true) {
                String cpu = String.valueOf(operatingSystem.getSystemLoadAverage() / operatingSystem.getAvailableProcessors());
                
                MemoryUsage nonHeap = memory.getNonHeapMemoryUsage();
                String nonHeapUsage = String.valueOf(nonHeap.getUsed() / 1024);
                
                MemoryUsage memHeap = memory.getHeapMemoryUsage();
                String heapUsage = String.valueOf(memHeap.getUsed() / 1024);
                
                String line = cpu + "," + heapUsage + "," + nonHeapUsage + "\n";
                
                Writer output = new BufferedWriter(new FileWriter(logPath, true));
                output.append(line);
                output.close();
                
                Thread.sleep(SAMPLING_RATE);
            }
            
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(MonitorResources.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
