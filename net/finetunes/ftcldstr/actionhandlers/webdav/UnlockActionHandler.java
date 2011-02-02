package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.webdav.LockingService;

/**
 * The UNLOCK method removes the lock identified by the lock token in
 * the Lock-Token request header from the Request-URI, and all other
 * resources included in the lock.  If all resources which have been
 * locked under the submitted lock token can not be unlocked then the
 * UNLOCK request MUST fail.
 * 
 * Any DAV compliant resource which supports the LOCK method MUST
 * support the UNLOCK method.
 * 
 * Description from RFC2518 (c) The Internet Society (1999).
 * http://www.ietf.org/rfc/rfc2518.txt
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
