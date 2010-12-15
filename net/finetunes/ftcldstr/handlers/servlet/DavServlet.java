package net.finetunes.ftcldstr.handlers.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.finetunes.ftcldstr.handlers.request.MethodsMap;

@SuppressWarnings("serial")
public class DavServlet extends MyServlet {

    public DavServlet() throws Exception {
        super();
    }

    @SuppressWarnings("unchecked")
    public void handleRequest(final HttpServletRequest request, final HttpServletResponse response,
            final String method) {
    	
    	// request processing

        String pathinfo = request.getPathInfo();
        if (pathinfo == null) {
            pathinfo = "";
        }

        System.out.println("Pathinfo: [" + pathinfo + "]");

        String cmd = null;
        
        try {
        	response.getOutputStream().write(("Hallo, I'm a WebDAV servlet.").getBytes());
        	response.getOutputStream().write(("<br><br><a href=\"/\">GET me</a>").getBytes());
        	response.getOutputStream().write(("<br><form method=\"post\" action=\"/\"><input type=submit name=\"b\" value=\"POST me\"></form>").getBytes());
        }
        catch (IOException e) {
        	System.out.println("Something went wrong.");
        }
        
        // Call method map here and handle the request
        
        MethodsMap methodsMap = new MethodsMap();
        methodsMap.handleRequest(request, response, method);
        
    }


    public void init(final ServletConfig config) throws ServletException  {
        super.init(config);
    }

    public void service(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException,
            IOException {
    	
    	String requestedMethod = request.getMethod();
    	System.out.println("Requested method: " + requestedMethod);
    	handleRequest(request, response, requestedMethod);
    	
        // super.service(request, response);
    }
}
