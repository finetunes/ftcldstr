package net.finetunes.ftcldstr.helper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implements log and debug messages logging. 
 * 
 */

public class Logger {
    
    public static final SimpleDateFormat format = new SimpleDateFormat("'['dd-MM-yyyy HH:mm:ss'] '");    
	
	public static void log(String message) {
		
	    if (ConfigService.LOGFILE != null) {
            try {
                FileWriter fstream = new FileWriter(ConfigService.LOGFILE, true);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(message + "\n");
                out.close();
            } catch (IOException e) {
                System.err.println("Unable to write to the log fix: " + e.getMessage());
            }
            
            System.err.println(format.format(new Date()) + "*   LOG: " + message);
	    }
	}
	
    public static void debug(String message) {
        
        if (ConfigService.DEBUG) {
            System.err.println(format.format(new Date()) + "* DEBUG: " + message);
        }
    }
	

}
