package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileHelper;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.LockingService;
import net.finetunes.ftcldstr.routines.xml.XMLService;

/**
 * The DELETE method requests that the origin server delete the resource
 * identified by the Request-URI. This method MAY be overridden by human
 * intervention (or other means) on the origin server. The client cannot
 * be guaranteed that the operation has been carried out, even if the
 * status code returned from the origin server indicates that the action
 * has been completed successfully. However, the server SHOULD NOT
 * indicate success unless, at the time the response is given, it
 * intends to delete the resource or move it to an inaccessible
 * location.
 *
 * The DELETE method on a collection MUST act as if a "Depth: infinity"
 * header was used on it.  A client MUST NOT submit a Depth header with
 * a DELETE on a collection with any value but infinity.
 * 
 * DELETE instructs that the collection specified in the Request-URI and
 * all resources identified by its internal member URLs are to be
 * deleted.
 * 
 * Descriptions from RF4918 (c) The IETF Trust (2007)
 * and RFC 2616 (C) The Internet Society (1999).
 * 
 * http://www.ietf.org/rfc/rfc4918.txt
 * http://www.ietf.org/rfc/rfc2616.txt
 * 
 */

public class DeleteActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        String status = "204 No Content";
        // check all files are writeable and than remove it
        
        Logger.debug("_DELETE: " + fn);
        
        ArrayList<HashMap<String, String>> resps = new ArrayList<HashMap<String,String>>(); 
        
        if (!FileOperationsService.file_exits(fn)) {
            status = "404 Not Found";
        }
        else if (requestParams.getRequestURI().matches(".*\\#.*") && !fn.matches(".*\\#.*") || 
                (requestParams.getRequest().getQueryString() != null && !requestParams.getRequest().getQueryString().isEmpty())) {
            status = "400 Bad Request";
        }
        else if (LockingService.isAllowed(requestParams, fn)) {
            status = "423 Locked";
        }
        else {
            if (ConfigService.ENABLE_TRASH) {
                if (!FileHelper.moveToTrash(requestParams, fn)) {
                    status = "403 Forbidden";
                    
                    // PZ: original message was 404 Forbidden
                    // suppose 403, not 404 should be here
                }
            }
            else {
                
                ArrayList<String[]> err = new ArrayList<String[]>();
                FileHelper.deltree(requestParams, fn, err);
                Logger.log("DELETE(" + fn + ")");
                
                Iterator<String[]> it = err.iterator();
                while (it.hasNext()) {
                    String[] e = it.next();
                    String file = e[0];
                    String message = e[1];
                    HashMap<String, String> r = new HashMap<String, String>();
                    r.put("href", file);
                    r.put("status", "403 Forbidden - $message");
                    resps.add(r);
                }
                
                if (resps.size() > 0) {
                    status = "207 Multi-Status";
                }
            }
        }
        
        String content = "";
        if (resps.size() > 0) {
            HashMap<String, Object> response = new HashMap<String, Object>();
            response.put("response", resps);
            HashMap<String, Object> multistatus = new HashMap<String, Object>();
            multistatus.put("multistatus", response);
            content = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, multistatus);
        }
        
        String type = null;
        if (resps.size() > 0) {
            type = "text/xml";
        }
        OutputService.printHeaderAndContent(requestParams, status, type, content);
        
        Logger.debug("DELETE RESPONSE (status=" + status + "): " + content);
      
    }    
    
}
