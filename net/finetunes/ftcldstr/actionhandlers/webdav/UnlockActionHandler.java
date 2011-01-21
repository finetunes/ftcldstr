package net.finetunes.ftcldstr.actionhandlers.webdav;

import sun.awt.SunGraphicsCallback.PrintHeavyweightComponentsCallback;
import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.webdav.LockingService;

/**
 * The UNLOCK method removes the lock identified by the lock token in
 * the Lock-Token request header.  The Request-URI MUST identify a
 * resource within the scope of the lock.
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class UnlockActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        String status = "403 Forbidden";
        String token = requestParams.getHeader("Lock-Token");

        if (token != null) {
            token = token.replaceAll("[<>]", "");
        }
        Logger.debug("UNLOCK: " + fn + " (token=" + token + ")");
        
        if (token == null) {
            status = "400 Bad Request";
        }
        else if (LockingService.isLocked(fn)) {
            if (LockingService.unlockResource(fn, token)) {
                status = "204 No Content";
            }
            else {
                status = "423 Locked";
            }
        }
        else {
            status = "409 Conflict";
        }
        
        OutputService.printHeaderAndContent(requestParams, status);
    }    
    
}
