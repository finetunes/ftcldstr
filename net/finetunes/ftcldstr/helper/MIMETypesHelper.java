package net.finetunes.ftcldstr.helper;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

public class MIMETypesHelper {
	
	public static String getMIMEType(String filename) {
	    
	    if (filename != null) {
    	    String extension = "default";
    	    String ext = filename.replaceFirst("^.*\\.([^\\.]+)$", "$1");
    	    
    	    if (ext != null && !ext.isEmpty()) {
    	        extension = ext;
    	    }
    	    
    	    Set<String> keys = ConfigService.MIMETYPES.keySet();
    	    Iterator<String> it = keys.iterator();
    	    while (it.hasNext()) {
    	        String k = it.next();
                if (k.matches("(?is).*\\b" + Pattern.quote(extension) + "\\b.*")) {
    	            return ConfigService.MIMETYPES.get(k);
    	        }
    	    }
	    }
	    
        return ConfigService.MIMETYPES.get("default");
	}
	
	public static String getFileExtentionByMimeType(String fn) {
	    
	    if (fn != null && ConfigService.MIMETYPES != null) {
            String imageMimeType = MIMETypesHelper.getMIMEType(fn);
            Set<String> keys = ConfigService.MIMETYPES.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (ConfigService.MIMETYPES.get(key).equals(imageMimeType)) {
                    return key;
                }
            }
            
            return imageMimeType;
	    }
	    
	    return null; 
	}

}
