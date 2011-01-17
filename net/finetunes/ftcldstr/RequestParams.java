package net.finetunes.ftcldstr;

import javax.servlet.ServletContext;
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
}
