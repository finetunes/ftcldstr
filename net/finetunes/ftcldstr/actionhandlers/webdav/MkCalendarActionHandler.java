package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;

/**
 * An HTTP request using the MKCALENDAR method creates a new calendar
 * collection resource.  A server MAY restrict calendar collection
 * creation to particular collections.
 * 
 * Description from RF4791 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4791.txt
 * 
 */

public class MkCalendarActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        MkcolActionHandler mkcol = new MkcolActionHandler();
        mkcol.mkCol(requestParams, true);
    }
    
}
