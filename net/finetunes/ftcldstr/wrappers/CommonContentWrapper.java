package net.finetunes.ftcldstr.wrappers;

import net.finetunes.ftcldstr.RequestParams;

public class CommonContentWrapper extends AbstractWrapper {
    
    public static final String WRAPPER_ID = "COMMON_WRAPPER";
    
    public CommonContentWrapper(RequestParams requestParams) {
        super();
        setWrapperID(requestParams, WRAPPER_ID);
    }
}