package net.finetunes.ftcldstr.routines;

import net.finetunes.ftcldstr.helper.ConfigService;

public class NamespaceService {
	
	public static String getNameSpace(String prop) {
		
	    if (ConfigService.ELEMENTS.containsKey(prop)) {
	        return ConfigService.ELEMENTS.get(prop);
	    }
	    else {
	        return ConfigService.ELEMENTS.get("default");
	    }
	}
	
	public static String getNameSpaceUri(String prop) {
	
	    return ConfigService.NAMESPACEABBR.get(getNameSpace(prop));
	}
	
	public static String nonamespace(String prop) {
	    
	    if (prop != null) {
	        prop = prop.replaceFirst("^{[^}]*}", "");
	    }
	    
	    return prop;
	}  

}
