package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.ArrayList;
import java.util.HashMap;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.handlers.SupportedMethodsHandler;

/**
 * The OPTIONS method represents a request for information about the
 * communication options available on the request/response chain
 * identified by the Request-URI. This method allows the client to
 * determine the options and/or requirements associated with a resource,
 * or the capabilities of a server, without implying a resource action
 * or initiating a resource retrieval.
 * 
 * Description from RFC 2616 (C) The Internet Society (1999).
 * http://www.ietf.org/rfc/rfc2616.txt
 * 
 */

public class OptionsActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        Logger.debug("OPTIONS: " + fn);
        
        String methods = "";  
        String status = "200 OK";
        String type;
        
        if (FileOperationsService.file_exits(fn)) {
            if (FileOperationsService.is_directory(fn)) {
                type = "httpd/unix-directory";
            }
            else {
                type = MIMETypesHelper.getMIMEType(fn);
            }
            
            ArrayList<String> supportedMethods = SupportedMethodsHandler.getSupportedMethods(fn);
            methods = RenderingHelper.joinArray(supportedMethods.toArray(new String[] {}), ", ");
        }
        else {
            status = "404 Not Found";
            type = "text/plain";
        }
        
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("Content-Length", "0");
        
        if (ConfigService.ENABLE_SEARCH) {
            params.put("DASL", "<DAV:basicsearch>");
        }
        
        if (methods != null) {
            params.put("MS-Author-Via", "DAV");
            params.put("DAV", ConfigService.DAV);
            params.put("Allow", methods);
            params.put("Public", methods);
            params.put("DocumentManagementServer", "Properties Schema");
        }

        OutputService.printHeader(requestParams, status, type, params);
    }    
}
