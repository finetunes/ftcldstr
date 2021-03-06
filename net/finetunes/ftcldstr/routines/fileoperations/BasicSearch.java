package net.finetunes.ftcldstr.routines.fileoperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.routines.webdav.SearchService;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;

public class BasicSearch {

	public static void doBasicSearch(
	        RequestParams requestParams,
			Object whereref, 
			String base, String href, 
			int depth, int limit, 
			ArrayList<HashMap<String, Object>> matches, 
			ArrayList<String> visited,
			String op, Object xmlref, String superop) {
	    
	    if (limit > 0 && matches != null && matches.size() >= limit) {
	        return;
	    }
	    
	    if (depth != Integer.MAX_VALUE && depth < 0) {
	        return;
	    }
	    
	    if (FileOperationsService.is_directory(requestParams, base) && !base.endsWith("/")) {
	        base += "/";
	    }
	    
	    if (FileOperationsService.is_directory(requestParams, base) && !href.endsWith("/")) {
	        href += "/";
	    }
	    
	    String filename = new String(base);
	    String request_uri = new String(href);
	    
        Object[] rs = SearchService.buildExprFromBasicSearchWhereClause(null, whereref, null, 
                requestParams, filename, request_uri);
        
        if (rs != null && rs.length > 0 && rs[0] instanceof Boolean && (Boolean)rs[0]) {
            // matched
            Logger.debug("doBasicSearch: " + base + " MATCHED");
            HashMap<String, Object> match = new HashMap<String, Object>();
            match.put("fn", base);
            match.put("href", href);
            matches.add(match);
        }
	    
	    String nbase = FileOperationsService.full_resolve(requestParams, base);
	    
	    if (visited == null) {
	        visited = new ArrayList<String>();
	    }
	    
	    if (visited.contains(nbase) && (depth == Integer.MAX_VALUE || depth < 0)) {
	        return;
	    }
	    
	    visited.add(nbase);
	    if (FileOperationsService.is_directory(requestParams, base)) {
	        ArrayList<String> files = WrappingUtilities.getFileList(requestParams, base);
	        
	        if (files != null) {
	            Iterator<String> it = files.iterator();
	            while (it.hasNext()) {
	                String sf = it.next();
	                
	                if (!sf.matches("(\\.|\\.\\.)")) {
	                    // not used
	                    // String nbase = base + sf;
	                    // String nhref = href + sf;
                        int dp = depth;
	                    if (depth != Integer.MAX_VALUE) {
	                        dp = depth - 1;
	                    }
	                    doBasicSearch(requestParams, whereref, base + sf, href + sf, dp, limit, matches, visited,
	                            op, xmlref, superop);
	                }
	            }
	        }
	    }
	}	
}
