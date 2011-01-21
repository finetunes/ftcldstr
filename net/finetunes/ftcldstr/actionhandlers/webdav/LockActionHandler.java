package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.HashMap;

import javax.annotation.processing.FilerException;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.GeneratorService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.LockingService;
import net.finetunes.ftcldstr.routines.xml.XMLService;

/**
 * LOCK method is used to take out a lock of any access type and to 
 * refresh an existing lock. Any resource that supports the LOCK method 
 * MUST, at minimum, support the XML request and response formats defined 
 * herein.
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class LockActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        Logger.debug("LOCK: " + fn);
        String ru = requestParams.getRequestURI();
        String depth = requestParams.getHeader("Depth");
        
        if (depth == null) {
            depth = "infinity";
        }
        
        String status = "200 OK";
        String type = "application/xml";
        String content = "";
        HashMap<String, String> addheader = new HashMap<String, String>();
        
        String xml = requestParams.getRequestBody();
        // my $xmldata = $xml ne "" ? simpleXMLParser($xml) : { }; // TODO
        Object xmldata = null;
        
        String token = "opaquelocktoken:" + GeneratorService.getuuid(fn);
        if (!FileOperationsService.file_exits(fn) && !FileOperationsService.file_exits(FileOperationsService.dirname(fn))) {
            status = "409 Conflict";
            type = "text/plain";
        }
        else if (!LockingService.isLockable(fn, xmldata)) {
            Logger.debug("LOCK: not lockable ... but...");
            if (LockingService.isAllowed(requestParams, fn)) {
                status = "200 OK";
                // LockingService.lockResource();
                // lockResource($fn, $ru, $xmldata, $depth, $timeout, $token); // TODO
                content = XMLService.createXML(/* {prop=>{lockdiscovery => getLockDiscovery($fn)}}  */ null); // TODO 
            }
            else {
                status = "423 Locked";
                type = "text/plain";
            }
        }
        else if (!FileOperationsService.file_exits(fn)) {
/*       
 TODO: implement     
  
        if (open(F,">$fn")) {
            print F '';
            close(F);
            my $resp = lockResource($fn, $ru, $xmldata, $depth, $timeout,$token);
            if (defined $$resp{multistatus}) {
                $status = '207 Multi-Status'; 
            } else {
                $addheader="Lock-Token: $token";
                $status='201 Created';
            }
            $content=createXML($resp);
        } else {
            $status='403 Forbidden';
            $type='text/plain';
        }            
            
*/            
        }
        else {
            Object resp = LockingService.lockResource(/* $fn, $ru, $xmldata, $depth, $timeout, $token */); // TODO: params and return type
            
            addheader.put("Lock-Token", token);
            
            /*
             * TODO: implement
            $content=createXML($resp);
            $status = '207 Multi-Status' if defined $$resp{multistatus};
            */
        }
        
        Logger.debug("LOCK: REQUEST: " + xml);
        Logger.debug("LOCK: RESPONSE: " + content);
        Logger.debug("LOCK: status: " + status + ", type=" + type);
        
        OutputService.printHeaderAndContent(requestParams, status, type, content, addheader);
    }
}
