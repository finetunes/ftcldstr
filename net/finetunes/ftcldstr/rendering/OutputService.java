package net.finetunes.ftcldstr.rendering;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesHelper;

public class OutputService {

	/**
	 * Sends the header and the content directly to the out
	 */
	public static void printHeaderAndContent(
	        HttpServletRequest request, HttpServletResponse response,
	        String pathTranslated,
	        String status, String type, String content, HashMap<String, String> headers) {
	    
	    // print header
	    
	    if (status != null && !status.isEmpty()) {
	        response.setStatus(extractStatusCode(status));
	    }
	    
        response.addHeader("Content-Type", type + "; charset=" + ConfigService.CHARSET);
        response.addHeader("Content-Length", String.valueOf(getContentLength(content)));
        response.addHeader("ETag", PropertiesHelper.getETag(pathTranslated));

        if (request.getHeader("Translate") != null &&
                !request.getHeader("Translate").isEmpty()) {
            response.addHeader("Translate", "f");
        }
        
        response.addHeader("MS-Author-Via", "DAV");
        response.addHeader("DAV", ConfigService.DAV);

        // adding extra headers
        if (headers != null) {
            Set<String> headerKeys = headers.keySet();
            Iterator<String> it = headerKeys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                response.addHeader(key, headers.get(key));
            }
        }
        
        // print content

        try {
            OutputStream outStream = response.getOutputStream();
            
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(outStream, ConfigService.CHARSET));
            pw.println(content);

            pw.flush();
            pw.close();        
        }
        catch (UnsupportedEncodingException e) {
            System.err.println("Unsupported charset encoding: " + ConfigService.CHARSET);
            e.printStackTrace();
        }
        catch (IOException e) {
            System.err.println("Unable to get output stream for writing.");
            e.printStackTrace();
        }
	}
	
    public static void printHeaderAndContent(
            HttpServletRequest request, HttpServletResponse response,
            String pathTranslated,
            String status, String type, String content) {
        printHeaderAndContent(request, response, pathTranslated, status, type, content, null);
    }

    public static void printHeaderAndContent(
            HttpServletRequest request, HttpServletResponse response,
            String pathTranslated,
            String status, String type) {
        printHeaderAndContent(request, response, pathTranslated, status, type, "");
    }

    public static void printHeaderAndContent(
            HttpServletRequest request, HttpServletResponse response,
            String pathTranslated,
            String status) {
        printHeaderAndContent(request, response, pathTranslated, status, "text/plain");
    }
    
    public static void printHeaderAndContent(HttpServletRequest request, HttpServletResponse response,
            String pathTranslated) {
        printHeaderAndContent(request, response, pathTranslated, "403 Forbidden");
    }	
    
	/*
	 * sends header directly to the out
	 */
	public static void printFileHeader(String filename) {
		
		// TODO: implement
		
	}
	
	/* 
	 * Helper function to obtain integer status code from the 
	 * status string.
	 */
	
	public static int extractStatusCode(String status) {
	    if (status != null) {
	        try {
	            String[] parts = status.split(" ");
	            int statusCode = Integer.valueOf(parts[0]);
	            return statusCode;
	        }
	        catch (NumberFormatException e) {
	            System.err.println("Invalid status string format: " + status);
	        }
	    }
	    
	    // default code
	    return HttpServletResponse.SC_OK;
	}
	
	public static int getContentLength(String content) {

	    if (content != null) {
	        // using the current charset value
	        // to calculate the length of the content
	        // in bytes
	     
	        byte[] contentBytes;
	        try {
	            contentBytes = content.getBytes(ConfigService.CHARSET);
	            return contentBytes.length;
	        }
	        catch (UnsupportedEncodingException e) {
	            System.err.println("Unsupported config charset: " + ConfigService.CHARSET);
	            e.printStackTrace();
	        }
	    }
	    
	    return 0;
	    
	}
}
