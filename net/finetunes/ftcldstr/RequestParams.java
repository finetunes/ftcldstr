package net.finetunes.ftcldstr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;

import com.oreilly.servlet.MultipartRequest;

public class RequestParams {
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    private String requestedMethod;
    
    private String pathTranslated;
    private String requestURI;
    private String scriptURI;
    private ServletContext servletContext;
    private String username = "anonymous"; // logged as
    private String userIP = "0.0.0.0";
    private boolean umaskInitialized = false;
    
    private MultipartRequest multipartRequest;
    
    public HttpServletRequest getRequest() {
        return request;
    }
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    public HttpServletResponse getResponse() {
        return response;
    }
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
    public String getRequestedMethod() {
        return requestedMethod;
    }
    public void setRequestedMethod(String requestedMethod) {
        this.requestedMethod = requestedMethod;
    }
    public String getPathTranslated() {
        return pathTranslated;
    }
    public void setPathTranslated(String pathTranslated) {
        this.pathTranslated = pathTranslated;
    }
    public String getRequestURI() {
        return requestURI;
    }
    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }
    public String getScriptURI() {
        return scriptURI;
    }

    public void setScriptURI(String scriptURI) {
        this.scriptURI = scriptURI;
    }
    
    public InputStream getRequestBodyInputStream() {
        try {
            return request.getInputStream();
        }
        catch (IOException e) {
            Logger.log("Failed to obtain input stream of the request.");
            return null;
        }
    }
    
    public String getRequestBody() {
        
        if (request != null) {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = null;
            try {
                InputStream inputStream = request.getInputStream();
                if (inputStream != null) {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    char[] charBuffer = new char[256];
                    int bytesRead = -1;
                    while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                        stringBuilder.append(charBuffer, 0, bytesRead);
                    }
                } 
                else {
                    stringBuilder.append("");
                }
            } catch (IOException ex) {
                // do nothing
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ex) {
                        // do nothing
                    }
                }
            }
            String body = stringBuilder.toString();
            return body;
        }
        
        return "";
    }

    public MultipartRequest getMultipartRequest() {
        return multipartRequest;
    }

    public void setMultipartRequest(MultipartRequest multipartRequest) {
        this.multipartRequest = multipartRequest;
    }

    public boolean headerExists(String headername) {
        
        if (request != null) {
            String h = request.getHeader(headername);
            return (h != null && !h.isEmpty());
        }
        
        return false;
    }
    
    public String getHeader(String headername) {
        
        return request.getHeader(headername);
    }      
    
    public boolean requestParamExists(String name) {
        
        if (request != null) {
            String param = request.getParameter(name);
            
            return (param != null && !param.isEmpty());
        }
        
        return false;
    }
    
    public String getRequestParam(String param) {
        if (requestParamExists(param)) {
            return request.getParameter(param);
        }
        
        return null;
    }    
    
    public String[] getRequestParamValues(String param) {
        if (requestParamExists(param)) {
            return request.getParameterValues(param);
        }
        
        return null;
    }      
    
    public boolean multipartRequestParamExists(String name) {
        
        if (multipartRequest == null) {
            createMultipartRequestWrapper();
        }
        
        if (multipartRequest != null && request != null) {
            String param = multipartRequest.getParameter(name);
            
            return (param != null && !param.isEmpty());
        }
        
        return false;
    }
    
    public String getMultipartRequestParam(String param) {

        if (multipartRequest == null) {
            createMultipartRequestWrapper();
        }
        
        if (multipartRequest != null && multipartRequestParamExists(param)) {
            return multipartRequest.getParameter(param);
        }
        
        return null;
    }    
    
    public String[] getMultipartRequestParamValues(String param) {

        if (multipartRequest == null) {
            createMultipartRequestWrapper();
        }
        
        if (multipartRequest != null && multipartRequestParamExists(param)) {
            return multipartRequest.getParameterValues(param);
        }
        
        return null;
    }    
    
    public File getFile(String fn) {

        if (multipartRequest != null && fn != null) {
            return multipartRequest.getFile(fn);
        }
        
        return null;
    }
    
    public ArrayList<String> getFileNames() {

        if (multipartRequest == null) {
            createMultipartRequestWrapper();
        }        
        
        ArrayList<String> files = null;
        if (multipartRequest != null) {
            java.util.Enumeration<String> names = multipartRequest.getFileNames();
            files = Collections.list(names);
        }
        
        return files;
    }    
        
    
    private void createMultipartRequestWrapper() {
        try {
            MultipartRequest m = new MultipartRequest(getRequest(), getPathTranslated(), ConfigService.POST_MAX_SIZE);
            setMultipartRequest(m);
        }
        catch (IOException e) {
            Logger.log("Error on multipart request parsing: " + e.getMessage());
        }
        catch (IllegalArgumentException e) {
            Logger.log("Error on multipart request parsing: " + e.getMessage());
        }
    }
    
    public ServletContext getServletContext() {
        return servletContext;
    }
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getUserIP() {
        return userIP;
    }
    public void setUserIP(String userIP) {
        this.userIP = userIP;
    }
    public boolean isUmaskInitialized() {
        return umaskInitialized;
    }
    public synchronized void setUmaskInitialized(boolean umaskInitialized) {
        this.umaskInitialized = umaskInitialized;
    }
    
    
}
