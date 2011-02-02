package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.ArrayList;
import java.util.HashMap;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.handlers.PropertyRequestHandler;
import net.finetunes.ftcldstr.routines.webdav.LockingService;
import net.finetunes.ftcldstr.routines.webdav.properties.StatusResponse;
import net.finetunes.ftcldstr.routines.xml.XMLParser;
import net.finetunes.ftcldstr.routines.xml.XMLService;

/**
 * The PROPPATCH method processes instructions specified in the request
 * body to set and/or remove properties defined on the resource
 * identified by the Request-URI.
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class ProppatchActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        Logger.debug("PROPPATCH: " + fn);
        
        String status = "403 Forbidden";
        String type = "text/plain";
        String content = "";
        
        if (FileOperationsService.file_exits(fn) && !LockingService.isAllowed(requestParams, fn)) {
            status = "423 Locked";
        }
        else if (FileOperationsService.file_exits(fn)) {
            
            String xml = requestParams.getRequestBody();
            
            Logger.debug("PROPPATCH: REQUEST: " + xml);
  
            XMLParser xmlParser = new XMLParser();
            HashMap<String, Object> dataRef = xmlParser.simpleXMLParser(xml, ConfigService.CHARSET);
            if (dataRef == null || dataRef.size() == 0) {
                Logger.debug("PROPPATCH: invalid XML request: " + xml);
                OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
                return;
            }
            
            ArrayList<StatusResponse> resps = new ArrayList<StatusResponse>();
            StatusResponse resp_200 = null;
            StatusResponse resp_403 = null;
            
            PropertyRequestHandler.handlePropertyRequest(requestParams, xml, dataRef, resp_200, resp_403);

            if (resp_200 != null && resp_200.getHref() != null) {
                resps.add(resp_200);
            }
            
            if (resp_200 != null && resp_200.getHref() != null) {
                resps.add(resp_200);
            }
            
            status = "207 Multi-Status";
            type = "text/xml";
            
            HashMap<String, Object> multistatus = new HashMap<String, Object>();
            multistatus.put("response", StatusResponse.statusResponseListToHashMap(resps));
            HashMap<String, Object> proppatch = new HashMap<String, Object>();
            proppatch.put("multistatus", multistatus);
            content = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, proppatch, false);            
        }
        else {
            status = "404 Not Found";
        }
        
        Logger.debug("PROPPATCH: RESPONSE: " + content);
        
        OutputService.printHeaderAndContent(requestParams, status, type, content);
        
    }    
    
}
