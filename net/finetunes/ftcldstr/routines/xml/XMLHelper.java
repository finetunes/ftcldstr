package net.finetunes.ftcldstr.routines.xml;

import java.util.HashMap;

import net.finetunes.ftcldstr.helper.ConfigService;

public class XMLHelper {
	
	public static String convXML2Str(HashMap<String, Object> xml) {
		
	    if (xml != null) {
	        String x = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, xml, true);
	        if (x != null) {
	            return x.toLowerCase();
	        }
	        
	        return x;
	    }
	    
	    return null;
	}
}
