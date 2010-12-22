package net.finetunes.ftcldstr.rendering;

/**
 * The rendering classes provide a web interface for the WebDAV protocol. 
 * The web interface consists of 3 pages (file/folder browser, properties view, 
 * and search view). 
 * 
 * The rendering classes receive requests from the web application, process them, 
 * make a call to the protocol implementation classes to get the required 
 * WebDAV data, wrap the result into the html page and return in the response.
 *
 */

public class RenderingService {
	
	public static String start_html(String title) {
		
		// TODO: implement
		return null;
		
	}
	
	public static String getPageNavBar(String ru, int count) {
		
		// TODO: implement
		return null;

	}
	
	public static String getQuickNavPath(String ru, String query) {
		
		// TODO: implement
		return null;
		
	}
	
	public static String createMsgQuery(String message, String messageParam,
				String errorMessage, String errorMessageParam) {
		
		// TODO: implement
		return null;
		
	}

}
