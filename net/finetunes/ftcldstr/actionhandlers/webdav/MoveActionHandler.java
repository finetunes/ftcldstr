package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.LockingService;
import net.finetunes.ftcldstr.serialization.BasicRoutines;
import net.finetunes.ftcldstr.serialization.DBPropertiesOperations;

/**
 * The MOVE operation on a non-collection resource is the logical
 * equivalent of a copy (COPY), followed by consistency maintenance
 * processing, followed by a delete of the source, where all three
 * actions are performed in a single operation.  The consistency
 * maintenance step allows the server to perform updates caused by the
 * move, such as updating all URLs, other than the Request-URI that
 * identifies the source resource, to point to the new destination
 * resource.
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class MoveActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
  
        String fn = requestParams.getPathTranslated();
        String status = "201 Created";
        String host = requestParams.getHeader("Host");
        String destination = requestParams.getHeader("Destination");
        String overwrite = "T";
        if (requestParams.headerExists("Overwrite")) {
            overwrite = requestParams.getHeader("Overwrite");
        }
        
        Logger.debug("MOVE: " + fn + " => " + destination);
        
        destination = destination.replaceFirst("^https?://([^@]+@)?\\Q" + host + "\\E" + ConfigService.VIRTUAL_BASE, "");
        destination = RenderingHelper.uri_unescape(destination);
        destination = RenderingHelper.uri_unescape(destination); // PZ: yes, it was unescaped twice in the perl code
        destination = ConfigService.DOCUMENT_ROOT + destination;

        if (destination == null || destination.isEmpty() || (fn.equals(destination))) {
            status = "403 Forbidden";
        }
        else if (FileOperationsService.file_exits(destination) && overwrite.equals("F")) {
            status = "412 Precondition Failed";
        }
        else if (!FileOperationsService.is_directory(destination)) {
            status = "409 Conflict - " + destination;
        }
        else if (!LockingService.isAllowed(requestParams, destination, FileOperationsService.is_directory(fn)) ||
                !LockingService.isAllowed(requestParams, destination, FileOperationsService.is_directory(destination))) {
            status = "423 Locked";
        }
        else {
            if (FileOperationsService.is_plain_file(destination)) {
                FileOperationsService.unlink(destination);
            }
            
            if (FileOperationsService.file_exits(destination)) {
                status = "204 No Content";
            }
            
            if (FileOperationsService.rmove(fn, destination)) {
                ConfigService.properties.moveProperties(fn, destination);
                ConfigService.locks.deleteLock(fn);
                LockingService.inheritLock(requestParams, destination, true);
                Logger.log("MOVE(" + fn + ", " + destination + ")");
            }
            else {
                status = "403 Forbidden";
            }
        }
        
        Logger.debug("MOVE: status=" + status);
        
        OutputService.printHeaderAndContent(requestParams, status);        
    }
    
}
