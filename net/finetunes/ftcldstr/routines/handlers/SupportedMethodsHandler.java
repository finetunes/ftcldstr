package net.finetunes.ftcldstr.routines.handlers;

import java.util.ArrayList;
import java.util.Arrays;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;

public class SupportedMethodsHandler {
	
	// returns methods list
	// FIXME: try to find a more suitable wrapper for an array of strings 
	public static ArrayList<String> getSupportedMethods(RequestParams requestParams, String path) {
		
	    ArrayList<String> methods = new ArrayList<String>();
	    
        ArrayList<String> rmethods = new ArrayList<String>(Arrays.asList("OPTIONS", "TRACE", "GET", "HEAD", "PROPFIND", "PROPPATCH", "COPY", "GETLIB"));
        ArrayList<String> wmethods = new ArrayList<String>(Arrays.asList("POST", "PUT", "MKCOL", "MOVE", "DELETE"));
        
        if (ConfigService.ENABLE_LOCK) {
            rmethods.addAll(Arrays.asList("LOCK", "UNLOCK"));
        }
        
        if (ConfigService.ENABLE_ACL || ConfigService.ENABLE_CALDAV || ConfigService.ENABLE_CALDAV_SCHEDULE || ConfigService.ENABLE_CARDDAV) {
            rmethods.add("REPORT");
        }
        
        if (ConfigService.ENABLE_SEARCH) {
            rmethods.add("SEARCH");
        }
        
        if (ConfigService.ENABLE_ACL || ConfigService.ENABLE_CALDAV || ConfigService.ENABLE_CARDDAV) {
            wmethods.add("ACL");
        }
        
        if (ConfigService.ENABLE_CALDAV || ConfigService.ENABLE_CALDAV_SCHEDULE) {
            wmethods.add("MKCALENDAR");
        }
        
        if (ConfigService.ENABLE_BIND) {
            wmethods.addAll(Arrays.asList("BIND", "UNBIND", "REBIND"));
        }
        
        methods.addAll(rmethods);
        
        if (path == null || path.isEmpty() || FileOperationsService.is_file_writable(requestParams, path)) {
            methods.addAll(wmethods);
        }
        
        return methods;
	}

}
