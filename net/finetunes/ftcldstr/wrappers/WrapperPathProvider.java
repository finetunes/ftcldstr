package net.finetunes.ftcldstr.wrappers;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;

public class WrapperPathProvider {
    
    Hashtable<String, String> pathMap;
    private static WrapperPathProvider instance = null;
    
    private WrapperPathProvider(RequestParams requestParams) {
        
        pathMap = new Hashtable<String, String>();
        initHashtable(requestParams);
    }
    
    private void initHashtable(RequestParams requestParams) {
        
        pathMap.put(AuthenticationWrapper.WRAPPER_ID, ConfigService.ROOT_PATH + "wrappers/checkpass.sh");
        pathMap.put(CommonContentWrapper.WRAPPER_ID, ConfigService.ROOT_PATH + "wrappers/operation.sh");
        // more wrappers here if required
        
        Set<String> keys = pathMap.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            File f = new File(pathMap.get(key));
            f.setExecutable(true);            
        }
        
    }
    
    private String lookupPath(String key) {
        if (key != null) {
            return pathMap.get(key);
        }
        
        return null;
    }
    
    public static synchronized WrapperPathProvider getInstance(RequestParams requestParams) {
        if (instance == null) {
            instance = new WrapperPathProvider(requestParams);
        }
        
        return instance;
    }
    
    public static String getWrapperPath(RequestParams requestParams, String key) {
        
        return getInstance(requestParams).lookupPath(key);
    }
}
