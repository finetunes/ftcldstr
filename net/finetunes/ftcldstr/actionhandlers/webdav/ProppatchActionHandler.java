package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.ArrayList;
import java.util.HashMap;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.handlers.PropertyRequestHandler;
import net.finetunes.ftcldstr.routines.webdav.LockingService;
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
        
        if (FileOperationsService.file_exits(fn) && !LockingService.isAllowed(fn)) { // TODO: implement isAllowed method
            status = "423 Locked";
        }
        else if (FileOperationsService.file_exits(fn)) {
            
            String xml = requestParams.getRequestBody();
            
            Logger.debug("PROPPATCH: REQUEST: " + xml);
  
            String dataRef = null; // TODO: datatype? 
/*
 * TODO
        my $dataRef;
        eval { $dataRef = simpleXMLParser($xml) };  
        if ($@) {
            debug("_PROPPATCH: invalid XML request: $@");
            printHeaderAndContent('400 Bad Request');
            return;
        }
*/        
            
            // TODO: set types
            ArrayList<Object> resps = new ArrayList<Object>();
            HashMap<Object, Object> resp_200 = new HashMap<Object, Object>(); 
            HashMap<Object, Object> resp_403 = new HashMap<Object, Object>();
            
            
            PropertyRequestHandler.handlePropertyRequest(xml, dataRef, resp_200, resp_403); // TODO: implement

            // TODO: datatypes?
            // push @resps, \%resp_200 if defined $resp_200{href};
            // push @resps, \%resp_403 if defined $resp_403{href};
            
            status = "207 Multi-Status";
            type = "text/xml";
            content = XMLService.createXML(null, false); // TODO: implement
            // TODO: ^^ // $content = createXML( { multistatus => { response => \@resps} });  
        }
        else {
            status = "404 Not Found";
        }
        
        Logger.debug("PROPPATCH: RESPONSE: " + content);
        
        OutputService.printHeaderAndContent(requestParams, status, type, content);
        
    }    
    
}
