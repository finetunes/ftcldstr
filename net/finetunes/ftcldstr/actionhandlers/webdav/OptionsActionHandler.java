package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.HashMap;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.rendering.OutputService;
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
            
            Object[] supportedMethods = SupportedMethodsHandler.getSupportedMethods(fn);
            
            if (supportedMethods.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append((String)(supportedMethods[0]));
                 
                for (int i = 1; i < supportedMethods.length; i++) {
                    sb.append(", ");
                    sb.append((String)(supportedMethods[i]));
                }
                 
                methods = sb.toString();
            }
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
        
        if (methods != null && !methods.isEmpty()) {
            params.put("MS-Author-Via", "DAV");
            params.put("DAV", ConfigService.DAV);
            params.put("Allow", methods);
            params.put("Public", methods);
            params.put("DocumentManagementServer", "Properties Schema");
        }

        OutputService.printHeader(requestParams, status, type, params);
    }    
}
