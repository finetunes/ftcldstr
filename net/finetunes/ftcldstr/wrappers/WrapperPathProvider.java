package net.finetunes.ftcldstr.wrappers;

import java.util.Hashtable;


// TODO: rewrite this using config or approve the technique.
public class WrapperPathProvider {
    
    Hashtable<String, String> pathMap;
    private static WrapperPathProvider instance = null;
    
    private WrapperPathProvider() {
        
        pathMap = new Hashtable<String, String>();
        initHashtable();
    }
    
    private void initHashtable() {
        
        pathMap.put("READ_DIRECTORY_WRAPPER", "wrappers/readdir.sh");
        pathMap.put("READ_DIRECTORY_WRAPPER_2", "wrappers/readdir.sh");
        // ...
        // here the rest of the paths
    }
    
    private String lookupPath(String key) {
        if (key != null) {
            return pathMap.get(key);
        }
        
        return null;
    }
    
    public static synchronized WrapperPathProvider getInstance() {
        if (instance == null) {
            instance = new WrapperPathProvider();
        }
        
        return instance;
    }
    
    
    public static String getWrapperPath(String key) {
        
        return getInstance().lookupPath(key);
    }
    

}
