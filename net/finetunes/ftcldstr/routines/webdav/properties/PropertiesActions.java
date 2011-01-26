package net.finetunes.ftcldstr.routines.webdav.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.rendering.OutputService;

public class PropertiesActions {
	
	
	// TODO: params and return type
	public static void removeProperty() {
		
	}
	
	// TODO: params and return type
	public static Object[] handlePropFindElement(Object xmldata) {
		
	    return new Object[]{"", "", ""};
	    
	}
	
	public static void handlePropElement(RequestParams requestParams, HashMap<String, Object> xmldata, ArrayList<String> props) {
	    
	    Set<String> keys = xmldata.keySet();
	    Iterator<String> it = keys.iterator();
	    
	    while (it.hasNext()) {
	        String prop = it.next();
	        String nons = new String(prop);
	        String ns = "";
	        
	        Pattern p = Pattern.compile("{([^}]*)}");
	        Matcher m = p.matcher(nons);
	        if (m.find()) {
	            ns = m.group(1);
	            nons = nons.replaceFirst("{([^}]*)}", "");
	        }
	        
	        Object ref = xmldata.get(prop);
	        // original code: if (ref($$xmldata{$prop}) !~/^(HASH|ARRAY)$/)
	        if ((!(ref instanceof HashMap<?, ?>)) || (!ref.getClass().isArray())) {
	            // ignore namespaces
	        }
	        // original code:  elsif ($ns eq "" && ! defined $$xmldata{$prop}{xmlns}) {
	        else if (ns.equals("") && (!(ref instanceof HashMap<?, ?> && ((HashMap<String, Object>)ref).get("xmlns") != null))) {
	            OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
	            return;
                // FIXME: was "exit;" -- check behaviour
	        }
	        else {
	            boolean contains = false;
                Iterator<String> it2 = ConfigService.KNOWN_FILE_PROPS.iterator();
                Iterator<String> it3 = ConfigService.KNOWN_COLL_PROPS.iterator();
                
                while (it2.hasNext() && !contains) {
                    String fp = it2.next();
                    if (fp.matches(".*" + Pattern.quote(nons) + ".*")) {
                        contains = true;
                        break;
                    }
                }
                
                while (it3.hasNext() && !contains) {
                    String fp = it3.next();
                    if (fp.matches(".*" + Pattern.quote(nons) + ".*")) {
                        contains = true;
                        break;
                    }
                }
                
                if (contains) {
                    props.add(nons);
                }
                else if (ns.equals("")) {
                    props.add("{}" + prop);
                }
                else {
                    props.add(prop);
                }
	        }
	    }
	}
	
	// TODO: params and return type
	public static void getProperty() {
		
	}
	
	// TODO: params and return type
	public static void setProperty() {
		
	}
	
}
