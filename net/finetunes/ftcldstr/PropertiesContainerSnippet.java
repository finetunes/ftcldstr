package net.finetunes.ftcldstr;

import java.util.Hashtable;

public final class PropertiesContainerSnippet
{
    private static PropertiesContainerSnippet instance = null;
    private static Hashtable<String, String> properties = null;
 
    public static synchronized PropertiesContainerSnippet getInstance() {
        if (instance == null)
            instance = new PropertiesContainerSnippet();
 
        return instance;
    }
 
    private PropertiesContainerSnippet() {
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
