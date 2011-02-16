package net.finetunes.ftcldstr.routines.fileoperations;

import java.util.ArrayList;
import java.util.Iterator;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;

public class BasicSearch {

	// TODO: clarify the type of expression param
	// TODO: matches requires to be passes by reference
	public static void doBasicSearch(
	        RequestParams requestParams,
			String expr, 
			String base, String href, 
			int depth, int limit, 
			ArrayList<String[]> matches, 
			ArrayList<String> visited) {
	    
	    if (limit > 0 && matches != null && matches.size() >= limit) {
	        return;
	    }
	    
	    if (depth != Integer.MAX_VALUE && depth < 0) {
	        return;
	    }
	    
	    if (FileOperationsService.is_directory(base) && !base.endsWith("/")) {
	        base += "/";
	    }
	    
	    if (FileOperationsService.is_directory(base) && !href.endsWith("/")) {
	        href += "/";
	    }
	    
	    String filename = new String(base);
	    String request_uri = new String(href);
	    
/*
 * TODO:	    
    my $res = eval  $expr ;
    if ($@) {
        debug("doBasicSearch: problem in $expr: $@");
    } elsif ($res) {
        debug("doBasicSearch: $base MATCHED");
        push @{$matches}, { fn=> $base, href=> $href };
    }
*/
	    String nbase = FileOperationsService.full_resolve(requestParams, base);
	    
	    if (visited == null) {
	        visited = new ArrayList<String>();
	    }
	    
	    if (visited.contains(nbase) && (depth == Integer.MAX_VALUE || depth < 0)) {
	        return;
	    }
	    
	    visited.add(nbase);
	    if (FileOperationsService.is_directory(base)) {
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
	                    doBasicSearch(requestParams, expr, base + sf, href + sf, dp, limit, matches, visited);
	                }
	            }
	        }
	    }
	}	
	
    public static void doBasicSearch(
            RequestParams requestParams,
            String expr, 
            String base, String href, 
            int depth, int limit, 
            ArrayList<String[]> matches) {
        doBasicSearch(requestParams, expr, base, href, depth, limit, matches, null);
    }
}
