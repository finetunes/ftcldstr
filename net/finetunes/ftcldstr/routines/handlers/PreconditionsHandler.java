package net.finetunes.ftcldstr.routines.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.QueryService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesHelper;

public class PreconditionsHandler {
	
	public static boolean preConditionFailed(RequestParams requestParams, String fn) {
	    
	    if (!FileOperationsService.file_exits(fn)) {
	        fn = FileOperationsService.dirname(fn) + "/";
	    }
	    
	    
	    HashMap<String, Object> ifheader = QueryService.getIfHeaderComponents(requestParams.getHeader("If")); // String, ArrayList<HashMap<String, String>>
	    
	    //  my $rowsRef = db_get( $fn ); // FROM webdav_locks
	    boolean t = false; // token found
	    boolean nnl = false; // not no-lock found
	    boolean nl = false; // no-lock found
	    boolean e = false; // wrong etag found
	    String etag = PropertiesHelper.getETag(fn);
	    
        // return {rtag=>$rtag, list=>\@tokens};

	    if (ifheader != null) {
	        ArrayList<HashMap<String, String>> ies = ((ArrayList<HashMap<String, String>>)ifheader.get("list"));
	        Iterator<HashMap<String, String>> it = ies.iterator();
	        while (it.hasNext()) {
	            HashMap<String, String> ie = it.next();
	            String ietoken = ie.get("token");
	            String ieetag = ie.get("etag");
	            
	            Logger.debug(" - ie{token}=" + ietoken);
	            
	            if (ietoken.matches("(?i).*Not\\s+<DAV:no-lock>.*")) {
	                nnl = true;
	            }
	            else if (ietoken.matches("(?i).*<DAV:no-lock>.*")) {
	                nl = true;
	            }
	            else if (ietoken.matches("(?i).*opaquelocktoken.*")) {
	                t = true;
	            }
	            
	            if (ieetag != null) {
	                e = !ieetag.equals(etag);
	            }
	        }
	    }
	    
        Logger.debug("checkPreCondition: t=" + t + ", nnl= " + nnl + " , e=" + e + ", nl=" + nl);
        return (t && nnl && e) || nl;
	}

}
