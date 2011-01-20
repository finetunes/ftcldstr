package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.ArrayList;

import javax.xml.soap.Detail;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.LockingService;

/**
 * The COPY method creates a duplicate of the source resource identified
 * by the Request-URI, in the destination resource identified by the URI
 * in the Destination header.  The Destination header MUST be present.
 * The exact behavior of the COPY method depends on the type of the
 * source resource.
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class CopyActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        String status = "201 Created";
        String depthHeader = requestParams.getHeader("Depth");
        int depth = 0;
        try {
            depth = Integer.valueOf(depthHeader).intValue();
        }
        catch (NumberFormatException e) {
            // do nothing
        }
        
        String host = requestParams.getHeader("Host");
        String destination = requestParams.getHeader("Destination");
        
        String overwrite = "T";
        if (requestParams.headerExists("Overwrite")) {
            overwrite = requestParams.getHeader("Overwrite");
        }
        
        destination = destination.replaceFirst("^https?://([^@]+@)?\\Q" + host + "\\E" + ConfigService.VIRTUAL_BASE, "");
        destination = RenderingHelper.uri_unescape(destination);
        destination = RenderingHelper.uri_unescape(destination); // PZ: yes, it was unescaped twice in the perl code
        destination = ConfigService.DOCUMENT_ROOT + destination;
        
        Logger.debug("COPY: " + fn + " => " + destination);
        
        if (destination == null || destination.isEmpty() || (fn.equals(destination))) {
            status = "403 Forbidden";
        }
        else if (FileOperationsService.file_exits(destination) && overwrite.equals("F")) {
            status = "412 Precondition Failed";
        }
        else if (!FileOperationsService.is_directory(destination)) {
            status = "409 Conflict - " + destination;
        }
        else if (!LockingService.isAllowed(requestParams, destination, FileOperationsService.is_directory(fn))) {
            status = "423 Locked";
        }
        else if (FileOperationsService.is_directory(fn) && depth == 0) {
            if (FileOperationsService.file_exits(destination)) {
                status = "204 No Content";
            }
            else {
                ArrayList<String> err = new ArrayList<String>();
                if (FileOperationsService.mkdir(destination, err)) {
                    LockingService.inheritLock(requestParams, destination);
                }
                else {
                    status = "403 Forbidden";
                }
            }
        }
        else {
            if (FileOperationsService.file_exits(destination)) {
                status = "204 No Content";
            }
            
            if (FileOperationsService.rcopy(fn, destination)) {
                LockingService.inheritLock(requestParams, destination, true);
                Logger.log("COPY(" + fn + ", " + destination + ")");
            }
            else {
                status = "403 Forbidden - copy failed";
            }
        }
        
        OutputService.printHeaderAndContent(requestParams, status);
    }
    
}
