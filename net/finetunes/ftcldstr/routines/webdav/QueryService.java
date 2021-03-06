package net.finetunes.ftcldstr.routines.webdav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;


public class QueryService {
	
	public static HashMap<String, Object> getIfHeaderComponents(String header) {
	    
	    String rtag = "";
	    ArrayList<HashMap<String, String>> tokens = new ArrayList<HashMap<String, String>>();
	    
	    if (header != null && !header.isEmpty()) {
	        
	        // original code:
            //	        if ($if =~ s/^<([^>]+)>\s*//) {
            //          $rtag=$1;
            //      }
	        
	        if (header.matches("(?s)<([^>]+)>\\s*.*")) {
	            rtag = header.replaceFirst("(?s)<([^>]+)>\\s*.*", "$1");
	            header = header.replaceFirst("(?s)<([^>]+)>\\s*(.*)", "$2");
	        }
	        
	        Pattern p = Pattern.compile("^\\((Not\\s*)?([^\\[\\)]+\\s*)?\\s*(\\[([^\\]\\)]+)\\])?\\)\\s*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
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
	
	public static String getQueryParams(RequestParams requestParams) {
	    
	    // preserve query parameters
	    ArrayList<String> query = new ArrayList<String>();
	    ArrayList<String> params = new ArrayList<String>(Arrays.asList("order", "showall"));
	    Iterator<String> it = params.iterator();
	    while (it.hasNext()) {
	        String param = it.next();
	        
	        if (requestParams.requestParamExists(param)) {
	            query.add(param + "=" + requestParams.getRequestParam(param));
	        }
	    }
	    
	    if (query.size() > 0) {
	        return RenderingHelper.joinArray(query.toArray(new String[]{}), ConfigService.URL_PARAM_SEPARATOR);
	    }
	    
	    return null;
	}
}
