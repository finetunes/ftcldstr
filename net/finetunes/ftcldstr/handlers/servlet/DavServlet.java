package net.finetunes.ftcldstr.handlers.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        }
        catch (IOException e) {
        	System.out.println("Something went wrong.");
        }
        
        // Call method map here and handle the request
        
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
