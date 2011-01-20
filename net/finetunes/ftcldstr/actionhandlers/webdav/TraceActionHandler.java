package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;

/**
 * The TRACE method is used to invoke a remote, application-layer loop-
 * back of the request message. TRACE allows the client to see what is
 * being received at the other end of the request chain and use that 
 * data for testing or diagnostic information. The value of the Via 
 * header field (section 14.45) is of particular interest, since it 
 * acts as a trace of the request chain.
 * 
 * Description from RFC 2616 (C) The Internet Society (1999).
 * http://www.ietf.org/rfc/rfc2616.txt
 * 
 */

public class TraceActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        Logger.debug("TRACE");
        
        String status = "200 OK";
        
        // original perl code: my $content = join("",<>);
        String content = requestParams.getRequestBody();
        String type = "message/http";
        
        String viaHeader = "Via: " + requestParams.getRequest().getServerName() + ":" + requestParams.getRequest().getServerPort();
        String via = requestParams.getRequest().getHeader("Via");
        if (via != null) {
            viaHeader += ", " + via;
        }
        
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("Via", viaHeader);        
        
        OutputService.printHeaderAndContent(requestParams, status, type, content, params);
    }   
}
