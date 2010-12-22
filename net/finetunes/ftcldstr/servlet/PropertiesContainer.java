package net.finetunes.ftcldstr.servlet;

import java.util.Hashtable;

public final class PropertiesContainer
{
    private static PropertiesContainer instance = null;
    private static Hashtable<String, String> properties = null;
 
    public static synchronized PropertiesContainer getInstance() {
        if (instance == null)
            instance = new PropertiesContainer();
 
        return instance;
    }
 
    private PropertiesContainer() {
    	properties = new Hashtable<String, String>();
    	
    	properties.put("a", "1");
    	properties.put("b", "2");
    	properties.put("c", "3");    	
    }
    
    public Hashtable<String, String> getProperties() {
    	return properties;
    }
 
//    public synchronized static void init(HttpServletRequest request) {
//
//    }
}
