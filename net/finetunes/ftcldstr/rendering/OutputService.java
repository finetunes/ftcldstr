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

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesHelper;

public class OutputService {

	/**
	 * Sends the header and the content directly to the out
	 */
	public static void printHeaderAndContent(
	        RequestParams requestParams,
	        String status, String type, String content, HashMap<String, String> headers) {
	    
	    // print header

	    if (status == null) {
	        status = "403 Forbidden";
	    }
	    
	    if (type == null) {
	        type = "text/plain";
	    }
	    
	    if (content == null) {
	        content = "";
	    }
	    
	    if (status != null) {
	        requestParams.getResponse().setStatus(extractStatusCode(status));
	    }
	    
	    requestParams.getResponse().addHeader("Content-Type", type + "; charset=" + ConfigService.CHARSET);
	    requestParams.getResponse().addHeader("Content-Length", String.valueOf(getContentLength(content)));
	    requestParams.getResponse().addHeader("ETag", PropertiesHelper.getETag(requestParams, requestParams.getPathTranslated()));

        if (requestParams.getRequest().getHeader("Translate") != null) {
            requestParams.getResponse().addHeader("Translate", "f");
        }
        
        requestParams.getResponse().addHeader("MS-Author-Via", "DAV");
        requestParams.getResponse().addHeader("DAV", ConfigService.DAV);

        // adding extra headers
        if (headers != null) {
            Set<String> headerKeys = headers.keySet();
            Iterator<String> it = headerKeys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                requestParams.getResponse().addHeader(key, headers.get(key));
            }
        }
        
        // print content
        printContent(requestParams, content);
    }
    
    public static void printContent(RequestParams requestParams, String content) {
        
        try {
            OutputStream outStream = requestParams.getResponse().getOutputStream();
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
	
	
    /**
     * Sends the header and the content directly to the out
     */
    public static void printHeader(RequestParams requestParams, String status, String type, 
            HashMap<String, String> headers) {
        
        // print header
        
        if (status != null && !status.isEmpty()) {
            requestParams.getResponse().setStatus(extractStatusCode(status));
        }
        
        requestParams.getResponse().addHeader("Content-Type", type);

        // adding extra headers
        if (headers != null) {
            Set<String> headerKeys = headers.keySet();
            Iterator<String> it = headerKeys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                requestParams.getResponse().addHeader(key, headers.get(key));
            }
        }
    }	
	
    public static void printHeaderAndContent(
            RequestParams requestParams,
            String status, String type, String content) {
        printHeaderAndContent(requestParams, status, type, content, null);
    }

    public static void printHeaderAndContent(
            RequestParams requestParams,
            String status, String type) {
        printHeaderAndContent(requestParams, status, type, null);
    }

    public static void printHeaderAndContent(
            RequestParams requestParams,
            String status) {
        printHeaderAndContent(requestParams, status, "text/plain");
    }
    
    public static void printHeaderAndContent(RequestParams requestParams) {
        printHeaderAndContent(requestParams, "403 Forbidden");
    }	
    
	/*
	 * sends header directly to the out
	 */
    public static void printFileHeader(RequestParams requestParams, String fn) {

        Object[] stat = FileOperationsService.stat(fn);
        
        // print header
        requestParams.getResponse().setStatus(200);
        requestParams.getResponse().addHeader("Content-Type", MIMETypesHelper.getMIMEType(fn));
        requestParams.getResponse().addHeader("Content-Length", String.valueOf(((Integer)stat[7]).intValue()));
        requestParams.getResponse().addHeader("ETag", PropertiesHelper.getETag(requestParams, fn));
        
        long timestamp = (new Long((String)stat[9]).longValue()) * 1000;  // msec  
        java.util.Date lastModified = new java.util.Date(timestamp);  
        
        requestParams.getResponse().addHeader("Last-Modified", String.format("%a, %d %b %Y %T GMT", lastModified));
        requestParams.getResponse().addHeader("charset", ConfigService.CHARSET);
        
        requestParams.getResponse().addHeader("MS-Author-Via", "DAV");
        requestParams.getResponse().addHeader("DAV", ConfigService.DAV);

        if (requestParams.getRequest().getHeader("Translate") != null) {
            requestParams.getResponse().addHeader("Translate", "f");
        }
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
