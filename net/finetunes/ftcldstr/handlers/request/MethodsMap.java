package net.finetunes.ftcldstr.handlers.request;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.finetunes.ftcldstr.handlers.request.actions.AbstractActionHandler;
import net.finetunes.ftcldstr.handlers.request.actions.GetActionHandler;
import net.finetunes.ftcldstr.handlers.request.actions.HeadActionHandler;
import net.finetunes.ftcldstr.handlers.request.actions.NotSupportedMethodActionHandler;
import net.finetunes.ftcldstr.handlers.request.actions.PostActionHandler;

/**
 * Stores the method map and call the required handler
 * depending on the request type.
 *
 */
public class MethodsMap {
	
	public static final String NOT_SUPPORTED = "NOT_SUPPORTED";
	private HashMap<String, AbstractActionHandler> methods;
	
	public MethodsMap() {
		
		initMethods();
		
	}
	
	public void initMethods() {
		
		methods = new HashMap<String, AbstractActionHandler>();
		addMethodHandler("GET", new GetActionHandler());
		addMethodHandler("HEAD", new HeadActionHandler());
		addMethodHandler("POST", new PostActionHandler());
		
		// TODO: add the rest of the method handlers
		
		addMethodHandler(MethodsMap.NOT_SUPPORTED, new NotSupportedMethodActionHandler());
	}
	
	public void addMethodHandler(String method, AbstractActionHandler handler) {
		methods.put(method, handler);
	}
	
	public AbstractActionHandler getMethodHandler(String method) {
		return methods.get(method);
	}
	
	public void handleRequest(final HttpServletRequest request, final HttpServletResponse response,
            final String method) {
		
		AbstractActionHandler actionHandler = getMethodHandler(method);
		
		if (actionHandler == null) {
			actionHandler = getMethodHandler(MethodsMap.NOT_SUPPORTED);
		}
		
		// do some work
		// actionHandler.handle();
		try {
			response.getOutputStream().write(("<br><br><br>Got " + method + " request").getBytes());
		}
		catch (IOException e) {
			System.out.println("Something went wrong with the output.");
		}
		
	}
	
}
