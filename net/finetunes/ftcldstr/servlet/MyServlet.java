package net.finetunes.ftcldstr.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
    private ServletContext context;

    public MyServlet() throws Exception {
        super();
        System.out.println("Initialising MyServlet");
        
        // config reading
        // db init
        // other initialization
    }

    public void service(final HttpServletRequest request, final HttpServletResponse response) throws 
    		ServletException, IOException {
        // String serverUrl = extractServerUrl(request);
        // setServerUrl(serverUrl);
        super.service(request, response);
    }

    public void init(final ServletConfig serverConfig) throws ServletException {
        super.init(serverConfig);
        this.context = getServletContext();
        // setContextPath(context.getContextPath());
    }

    private String extractServerUrl(final HttpServletRequest request) {
        StringBuffer serverUrl = new StringBuffer("");
        serverUrl.append(request.getScheme());
        serverUrl.append("://");
        serverUrl.append(request.getServerName());
        serverUrl.append(":");
        serverUrl.append(request.getServerPort());
        return serverUrl.toString();
    }
    

}