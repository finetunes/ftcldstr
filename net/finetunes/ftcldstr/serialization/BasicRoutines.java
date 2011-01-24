package net.finetunes.ftcldstr.serialization;

import java.util.List;

import net.finetunes.ftcldstr.helper.ConfigService;

import org.w3c.dom.Element;

public class BasicRoutines {
	
	// returns list of DB rows
	// TODO: try to find a more suitable return type
	public static List db_get(String filename, String token) {
		
	    // deprecated
	    // replaced with ConfigService.locks.getLocks();
	    
		// TODO: implement
		return null;
		
	}
	
	public static boolean db_insert(
			String baseFilename, String filename, 
			String type, String scope, String token, 
			int depth, int timeout, 
			Element owner) {
		
		// TODO: implement
		return false;
		
	}
	
	public static boolean db_update(String baseFilename, String filename, int timeout) {
		
		// TODO: implement
		return false;
		
	}
	
	public static boolean db_delete(String filename, String token) {
		
		// TODO: implement
		return false;
		
	}

    public static boolean db_delete(String filename) {
        
        return db_delete(filename, null);
    }	
	
}
