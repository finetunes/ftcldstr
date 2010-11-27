package net.finetunes.ftcldstr;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.http11.*;
import com.bradmcevoy.http.webdav.*;

public class MServlet extends HttpServlet {
	ServletConfig config;
//	HttpManager httpManager;
//	AuthenticationService authService;

	private static final ThreadLocal<HttpServletRequest> originalRequest = new ThreadLocal<HttpServletRequest>();
	private static final ThreadLocal<HttpServletResponse> originalResponse = new ThreadLocal<HttpServletResponse>();

	public static HttpServletRequest request() {
		return originalRequest.get();
	}

	public static HttpServletResponse response() {
		return originalResponse.get();
	}

	public static void forward(String url) {
		try {
			request().getRequestDispatcher(url).forward(originalRequest.get(),
					originalResponse.get());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (ServletException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void init(ServletConfig config) throws ServletException {
		try {
			this.config = config;
			httpManager = new HttpManager(getResourceFactory(),
					getCompressingHandler(), getAuthService());
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	public void service(javax.servlet.ServletRequest servletRequest,
			javax.servlet.ServletResponse servletResponse)
			throws ServletException, IOException {
		HttpServletRequest req = (HttpServletRequest) servletRequest;
		HttpServletResponse resp = (HttpServletResponse) servletResponse;
		try {
			originalRequest.set(req);
			originalResponse.set(resp);
			WebdavServletRequest request = new WebdavServletRequest(req);
			ServletResponse response = new ServletResponse(resp);
			httpManager.process(request, response);
		} finally {
			originalRequest.remove();
			originalResponse.remove();
			servletResponse.getOutputStream().flush();
			servletResponse.flushBuffer();
		}
	}

	public String getServletInfo() {
		return "WebdavServlet";
	}

	public ServletConfig getServletConfig() {
		return config;
	}

	public void destroy() {
	}

	private ResourceFactory getResourceFactory() {
		return new WebdavResourceFactory();
	}

	private AuthenticationService getAuthService() {
		if (authService == null) {
			List<AuthenticationHandler> handlers = new ArrayList<AuthenticationHandler>();
			handlers.add(new BasicAuthHandler());
			authService = new AuthenticationService(handlers);
		}
		return authService;
	}

	private WebDavResponseHandler getCompressingHandler() {
		return new CompressingResponseHandler(new DefaultWebDavResponseHandler(
				getAuthService()));
	}

}