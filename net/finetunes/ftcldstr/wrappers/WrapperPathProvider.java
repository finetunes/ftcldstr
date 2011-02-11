package net.finetunes.ftcldstr.wrappers;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;

import net.finetunes.ftcldstr.RequestParams;


// TODO: rewrite this using config or approve the technique.
public class WrapperPathProvider {
    
    Hashtable<String, String> pathMap;
    private static WrapperPathProvider instance = null;
    
    private WrapperPathProvider(RequestParams requestParams) {
        
        pathMap = new Hashtable<String, String>();
        initHashtable(requestParams);
    }
    
    private void initHashtable(RequestParams requestParams) {
        
        String pt = requestParams.getServletContext().getRealPath("");
        if (!pt.endsWith( System.getProperty("file.separator"))) {
            pt +=  System.getProperty("file.separator");
        }
        
        pathMap.put(AuthenticationWrapper.WRAPPER_ID, pt + "wrappers/checkpass.sh"); // PZ: DEBUG
        pathMap.put(CommonContentWrapper.WRAPPER_ID, pt + "wrappers/operation.sh"); // PZ: DEBUG
        // pathMap.put(CommonContentWrapper.WRAPPER_ID, pt + "wrappers\\1.bat"); // PZ: DEBUG
        // pathMap.put("READ_DIRECTORY_WRAPPER", "wrappers/readdir.sh");
        // pathMap.put("READ_DIRECTORY_WRAPPER_2", "wrappers/readdir.sh");
        // ...
        // here the rest of the paths
        
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
