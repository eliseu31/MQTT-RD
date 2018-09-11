/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer;

import java.io.IOException;
import org.json.JSONException;

/**
 *
 * @author eliseu
 */
public class Main {
    
    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws org.json.JSONException
     */
    public static void main(String[] args) throws JSONException, InterruptedException, IOException{
        InitSystem init = new InitSystem();
        init.startSniffer();
    }
}
