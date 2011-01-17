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
                if (k.matches("(?i).*\\b" + Pattern.quote(extension) + "\\b.*")) {
    	            return ConfigService.MIMETYPES.get(k);
    	        }
    	    }
	    }
	    
        return ConfigService.MIMETYPES.get("default");
	}

}
