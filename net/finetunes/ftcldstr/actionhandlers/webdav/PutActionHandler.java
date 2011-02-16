package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.handlers.PreconditionsHandler;
import net.finetunes.ftcldstr.routines.webdav.LockingService;

/**
 * A PUT performed on an existing resource replaces the GET response
 * entity of the resource.  Properties defined on the resource may be
 * recomputed during PUT processing but are not otherwise affected.  For
 * example, if a server recognizes the content type of the request body,
 * it may be able to automatically extract information that could be
 * profitably exposed as properties.
 * 
 * A PUT request to an existing collection MAY be treated as an error 
 * (405 Method Not Allowed).
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class PutActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        
        String status = "204 No Content";
        String type = "text/plain";
        String content = "";
        String buffer;
        
        Logger.debug("_PUT " + fn + "; dirname=" + FileOperationsService.splitFilename(fn)[0]);
        
        if (requestParams.headerExists("Content-Range")) {
            status = "501 Not Implemented";
        }
        else if (FileOperationsService.is_directory(FileOperationsService.dirname(fn)) && !FileOperationsService.is_file_writable(requestParams, fn)) {
            status = "403 Forbidden";
        }
        else if (PreconditionsHandler.preConditionFailed(requestParams, fn)) {
            status = "412 Precondition Failed";
        }
        else if (!LockingService.isAllowed(requestParams, fn)) {
            status = "423 Locked";
        }
        // #} elsif (defined $ENV{HTTP_EXPECT} && $ENV{HTTP_EXPECT} =~ /100-continue/) {
        // #   $status='417 Expectation Failed';
        else if (FileOperationsService.is_directory(FileOperationsService.dirname(fn))) {
            if (!FileOperationsService.file_exits(fn)) {
                Logger.debug("PUT: created...");
                status = "201 Created";
                type = "text/html";
                
                content = "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n<html><head><title>201 Created</title></head>";
                content += "<body><h1>Created</h1><p>Resource " + requestParams.getRequest().getQueryString() + " has been created.</p></body></html>\n";
            }
            
            String c = requestParams.getRequestBody();
            if (!FileOperationsService.write_file(fn, c)) {
                status = "403 Forbidden";
                content = "";
                type = "text/plain";
            }
        }
        else {
            status = "409 Conflict";
        }
        
        OutputService.printHeaderAndContent(requestParams, status, type, content);
    }
    
}
