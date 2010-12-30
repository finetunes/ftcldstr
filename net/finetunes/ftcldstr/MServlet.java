package net.finetunes.ftcldstr;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class MServlet extends HttpServlet {
	ServletConfig config;
    private ServletContext context;
//	HttpManager httpManager;
//	AuthenticationService authService;

//	private static final ThreadLocal<HttpServletRequest> rawRequest = new ThreadLocal<HttpServletRequest>();
//	private static final ThreadLocal<HttpServletResponse> rawResponse = new ThreadLocal<HttpServletResponse>();

//	public static HttpServletRequest getRawRequest() {
//		return rawRequest.get();
//	}
//
//	public static HttpServletResponse getRawResponse() {
//		return rawResponse.get();
//	}

//	public static void forward(String url) {
//		try {
//			HttpServletRequest re = getRawRequest();
//			HttpServletResponse rs = getRawResponse();
//			
//			re.getRequestDispatcher(url).forward(
//					re,
//					rs);
//		} catch (IOException ex) {
//			throw new RuntimeException(ex);
//		} catch (ServletException ex) {
//			throw new RuntimeException(ex);
//		}
//	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			this.config = config;
	        this.context = getServletContext();
	        // setContextPath(context.getContextPath());
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	public void service(
			javax.servlet.http.HttpServletRequest servletRequest,
			javax.servlet.http.HttpServletResponse servletResponse)
		throws ServletException, IOException {
		
		System.out.println("MServlet::service::METHOD: "+servletRequest.getMethod());
//		super.service(servletRequest, servletResponse);
//		
//		HttpServletRequest req = (HttpServletRequest)servletRequest;
//		HttpServletResponse resp = (HttpServletResponse)servletResponse;
//		try {
//			rawRequest.set(req);
//			rawResponse.set(resp);
//			
////			WebdavServletRequest request = new WebdavServletRequest(req);
////			ServletResponse response = new ServletResponse(resp);
////			httpManager.process(request, response);
//		} 
//		finally {
//			rawRequest.remove();
//			rawResponse.remove();
//			servletResponse.getOutputStream().flush();
//			servletResponse.flushBuffer();
//		}
		
		servletResponse.getOutputStream().flush();
		servletResponse.flushBuffer();
	}

	public String getServletInfo() {
		return "MServlet - based on webdav.pl";
	}

	public ServletConfig getServletConfig() {
		return super.getServletConfig();
//		return config;
	}

	public void destroy() {
		super.destroy();
	}
}

