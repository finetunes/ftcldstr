package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;

public class MkCalendarActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        MkcolActionHandler mkcol = new MkcolActionHandler();
        mkcol.mkCol(requestParams, true);
    }
    
}
