package net.finetunes.ftcldstr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestParams {
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    private String requestedMethod;
    
    private String pathTranslated;
    private String requestURI;
    
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

    
    
}
