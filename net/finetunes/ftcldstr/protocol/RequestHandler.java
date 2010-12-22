package net.finetunes.ftcldstr.protocol;

/**
 * Protocol handling
 * 
 * Separate classes are used for WebDAV protocol implementation 
 * and each extension to WebDAV protocol implementation. These classes 
 * receive http requests for the corresponding method from the web servlet 
 * class and organize the further processing. To arrange this, the 
 * corresponding classes of protocol implementation are called.
 * 
 * The protocol handling classes have to parse the http request, call the 
 * corresponding classes, which implement the particular method of WebDAV protocol, 
 * get the result and send it back in the response.
 *
 */


public class RequestHandler {
	
	// main method to handle requests and
	// call the request handler depending
	// on the request type
	public static void handleRequest() {
		
		// TODO: implement
		
	}

}
