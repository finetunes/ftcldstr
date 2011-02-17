package net.finetunes.ftcldstr.wrappers;

import net.finetunes.ftcldstr.RequestParams;

public class AuthenticationWrapper extends AbstractWrapper {
    
    public static final String WRAPPER_ID = "AUTHENTICATION_WRAPPER";
    
    public AuthenticationWrapper(RequestParams requestParams) {
        super();
        setWrapperID(requestParams, WRAPPER_ID);
    }
}