package net.finetunes.ftcldstr.actionhandlers.webdav;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;

/**
 * The HEAD method is identical to GET except that the server MUST NOT
 * return a message-body in the response. The metainformation contained
 * in the HTTP headers in response to a HEAD request SHOULD be identical
 * to the information sent in response to a GET request. This method can
 * be used for obtaining metainformation about the entity implied by the
 * request without transferring the entity-body itself. This method is
 * often used for testing hypertext links for validity, accessibility,
 * and recent modification.
 * 
 * Similarly, since the definition of HEAD is a GET without a response
 * message body, the semantics of HEAD are unmodified when applied to
 * collection resources.
 * 
 * Descriptions from RF4918 (c) The IETF Trust (2007)
 * and RFC 2616 (C) The Internet Society (1999).
 * 
 * http://www.ietf.org/rfc/rfc4918.txt
 * http://www.ietf.org/rfc/rfc2616.txt
 * 
 */

public class HeadActionHandler extends AbstractActionHandler {

    public void handle(final HttpServletRequest request, final HttpServletResponse response,
            String pathTranslated) {
        
        String fn = pathTranslated;

        if (FileOperationsService.is_directory(fn)) {
            Logger.debug("HEAD: " + fn + " is a folder!");
            OutputService.printHeaderAndContent(request, response, "200 OK", "httpd/unix-directory");
        }
        else if (FileOperationsService.file_exits(fn)) {
            Logger.debug("HEAD: " + fn + " exists!");
            OutputService.printFileHeader(fn);
        }
        else {
            Logger.debug("HEAD: " + fn + " does not exist!");
            OutputService.printHeaderAndContent(request, response, "404 Not Found");
        }
    }
    
    
    
}
