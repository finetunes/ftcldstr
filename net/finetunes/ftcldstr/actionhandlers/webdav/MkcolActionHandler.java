package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.handlers.PropertyRequestHandler;
import net.finetunes.ftcldstr.routines.webdav.LockingService;
import net.finetunes.ftcldstr.routines.webdav.properties.StatusResponse;

/**
 * MKCOL creates a new collection resource at the location specified by
 * the Request-URI.  If the Request-URI is already mapped to a resource,
 * then the MKCOL MUST fail.  During MKCOL processing, a server MUST
 * make the Request-URI an internal member of its parent collection,
 * unless the Request-URI is "/".  If no such ancestor exists, the
 * method MUST fail.  When the MKCOL operation creates a new collection
 * resource, all ancestors MUST already exist, or the method MUST fail
 * with a 409 (Conflict) status code.  For example, if a request to
 * create collection /a/b/c/d/ is made, and /a/b/c/ does not exist, the
 * request must fail.
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class MkcolActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        mkCol(requestParams, false);
    }
    
    public void mkCol(RequestParams requestParams, boolean cal) {

        String fn = requestParams.getPathTranslated();
        String status = "201 Created";
        String type = "";
        String content = "";
        Logger.debug("MKCOL: " + fn);
        
        String body = requestParams.getRequestBody();
        String dataRef = null; // TODO: datatype
        
        if (!body.isEmpty()) {
            String requestContentType = requestParams.getRequest().getContentType();
            Logger.debug("MKCOL: yepp #1" + requestContentType);
            // maybe extended mkcol (RFC5689)
            if (requestContentType.matches(".*/xml.*")) {
                
                // original code:
                // eval { $dataRef = simpleXMLParser($body) }; 
                // if ($@) { ...
                
                // TODO: simpleXMLParser should throw exceptions if something goes wrong
                // dataRef = simpleXMLParser(body);
                
/*
 * TODO
            eval { $dataRef = simpleXMLParser($body) }; 
            if ($@) {
                debug("_MKCOL: invalid XML request: $@");
                printHeaderAndContent('400 Bad Request');
                return;
            }
            if (ref($$dataRef{'{DAV:}set'}) !~ /(ARRAY|HASH)/) {
                printHeaderAndContent('400 Bad Request');
                return;
            }                
                
*/                
            }
            else {
                status = "415 Unsupported Media Type";
                OutputService.printHeaderAndContent(requestParams, status, type, content);
                return;
            }
        }
        
        if (FileOperationsService.file_exits(fn)) {
            status = "405 Method Not Allowed";
        }
        else if (!FileOperationsService.file_exits(FileOperationsService.dirname(fn))) {
            status = "409 Conflict";
        }
        else if (!FileOperationsService.is_file_writable(FileOperationsService.dirname(fn))) {
            status = "403 Forbidden";
        }
        else if (!LockingService.isAllowed(requestParams, fn)) {
            Logger.debug("MKCOL: not allowed!");
            status = "423 Locked";
        }
        else if (FileOperationsService.file_exits(fn)) {
            status = "409 Conflict";
        }
        else if (FileOperationsService.is_directory(FileOperationsService.dirname(fn))) {
            Logger.debug("MKCOL: create " + fn);
            
            if (FileOperationsService.mkdir(fn)) {
                
                StatusResponse resp_200 = null;
                StatusResponse resp_403 = null;
                
                PropertyRequestHandler.handlePropertyRequest(body, dataRef, resp_200, resp_403); // TODO: implement
                // ignore errors from property request
                LockingService.inheritLock(requestParams);
                Logger.log("MKCOL(" + fn + ")");
                
                
            }
            else {
                status = "403 Forbidden";
            }
            
        }
        else {
            Logger.debug("MKCOL: parent directory does not exist");
            status = "409 Conflict";
        }
        
        OutputService.printHeaderAndContent(requestParams, status, type, content);
    }
    
}
