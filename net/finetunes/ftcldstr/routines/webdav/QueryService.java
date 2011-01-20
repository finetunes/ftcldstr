package net.finetunes.ftcldstr.routines.webdav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class QueryService {
	
	public static HashMap<String, Object> getIfHeaderComponents(String header) {
	    
	    String rtag = "";
	    ArrayList<HashMap<String, String>> tokens = new ArrayList<HashMap<String, String>>();
	    
	    if (header != null && !header.isEmpty()) {
	        
	        // original code:
            //	        if ($if =~ s/^<([^>]+)>\s*//) {
            //          $rtag=$1;
            //      }
	        
	        if (header.matches("<([^>]+)>\\s*.*")) {
	            rtag = header.replaceFirst("<([^>]+)>\\s*.*", "$1");
	            header = header.replaceFirst("<([^>]+)>\\s*(.*)", "$2");
	        }
	        
	        Pattern p = Pattern.compile("^\\((Not\\s*)?([^\\[\\)]+\\s*)?\\s*(\\[([^\\]\\)]+)\\])?\\)\\s*", Pattern.CASE_INSENSITIVE);
	        Matcher m = p.matcher(header);
	        
	        while (m.find()) {
	            HashMap<String, String> item = new HashMap<String, String>();
	            item.put("token", m.group(1) + m.group(2));
	            item.put("etag", m.group(4));
	            
	            tokens.add(item);
	        }
	        
	        // return {rtag=>$rtag, list=>\@tokens};
	        HashMap<String, Object> r = new HashMap<String, Object>();
	        r.put("rtag", rtag);
	        r.put("list", tokens);
	        return r;
	    }
	        
	    return null;
	}
	
	public static String getQueryParams() {
		
		// TODO: implement 
		return "";
	}

}
