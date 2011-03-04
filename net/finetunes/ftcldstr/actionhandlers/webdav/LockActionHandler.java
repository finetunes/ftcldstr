package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.ArrayList;
import java.util.HashMap;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.GeneratorService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.LockingService;
import net.finetunes.ftcldstr.routines.xml.XMLParser;
import net.finetunes.ftcldstr.routines.xml.XMLService;

/**
 * LOCK method is used to take out a lock of any access type and to 
 * refresh an existing lock. Any resource that supports the LOCK method 
 * MUST, at minimum, support the XML request and response formats defined 
 * herein.
 * 
 * A LOCK method invocation creates the lock specified by the lockinfo 
 * XML element on the Request-URI. Lock method requests SHOULD have a 
 * XML request body which contains an owner XML element for this lock 
 * request, unless this is a refresh request. The LOCK request may 
 * have a Timeout header.
 * 
 * Description from RF4918 (c) The IETF Trust (2007)
 * and RFC2518 (c) he Internet Society (1999)
 * http://www.ietf.org/rfc/rfc4918.txt
 * http://www.ietf.org/rfc/rfc2518.txt
 * 
 */

public class LockActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        Logger.debug("LOCK: " + fn);
        String ru = requestParams.getRequestURI();
        String depths = requestParams.getHeader("Depth");
        int depth = 0;
        
        if (depths == null) {
            depth = Integer.MAX_VALUE;
        }
        else {
            try {
                depth = Integer.valueOf(depths);
            }
            catch (NumberFormatException e) {
                depth = 0;
                Logger.log("Exception: Invalid depth value: " + depths);
            }
        }
        
        String timeouts = requestParams.getHeader("Timeout");
        int timeout = 0;
        if (timeouts != null && !timeouts.isEmpty()) {
            try {
                timeout = Integer.valueOf(timeouts);
            }
            catch (NumberFormatException e) {
                Logger.log("Exception: Invalid timeout value: " + timeouts);
            }
        }
        
        String status = "200 OK";
        String type = "application/xml";
        String content = "";
        HashMap<String, String> addheader = new HashMap<String, String>();
        
        String xml = requestParams.getRequestBody();
        HashMap<String, Object> xmldata = null;
        if (xml != null && !xml.isEmpty()) {
            XMLParser xmlParser = new XMLParser();
            xmldata = xmlParser.simpleXMLParser(xml, ConfigService.CHARSET);
        }
        else {
            xmldata = new HashMap<String, Object>();
        }
        
        String token = "opaquelocktoken:" + GeneratorService.getuuid(fn);
        if (!FileOperationsService.file_exits(requestParams, fn) && !FileOperationsService.file_exits(requestParams, FileOperationsService.dirname(fn))) {
            status = "409 Conflict";
            type = "text/plain";
        }
        else if (!LockingService.isLockable(requestParams, fn, xmldata)) {
            Logger.debug("LOCK: not lockable ... but...");
            if (LockingService.isAllowed(requestParams, fn)) {
                status = "200 OK";
                LockingService.lockResource(requestParams, fn, ru, xmldata, depth, timeout, token);
                HashMap<String, Object> prop = new HashMap<String, Object>();
                ArrayList<HashMap<String, Object>> lockdiscovery = LockingService.getLockDiscovery(fn);
                prop.put("lockdiscovery", lockdiscovery);
                content = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, prop); 
            }
            else {
                status = "423 Locked";
                type = "text/plain";
            }
        }
        else if (!FileOperationsService.file_exits(requestParams, fn)) {
            if (FileOperationsService.create_file(requestParams, fn, "")) {
                HashMap<String, Object> resp = LockingService.lockResource(requestParams, fn, ru, xmldata, depth, timeout, token);
                if (resp != null && resp.get("multistatus") != null) {
                    status = "207 Multi-Status";
                }
                else {
                    addheader.put("Lock-Token", token);
                    status = "201 Created";
                }
                content = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, resp);
            }
            else {
                status = "403 Forbidden";
                type = "text/plain";
            }
        }
        else {
            HashMap<String, Object> resp = LockingService.lockResource(requestParams, fn, ru, xmldata, depth, timeout, token);
            content = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, resp);
            if (resp != null && resp.containsKey("multistatus") && resp.get("multistatus") != null) {
                status = "207 Multi-Status";
            }
        }
        
        Logger.debug("LOCK: REQUEST: " + xml);
        Logger.debug("LOCK: RESPONSE: " + content);
        Logger.debug("LOCK: status: " + status + ", type=" + type);
        
        OutputService.printHeaderAndContent(requestParams, status, type, content, addheader);
    }
}
