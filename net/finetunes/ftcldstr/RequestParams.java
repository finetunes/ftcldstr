package net.finetunes.ftcldstr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestParams {
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    private String requestedMethod;
    
    private String pathTranslated;
    private String requestURI;
    private String scriptURI;
    
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
}
