package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.HashMap;
import java.util.regex.Pattern;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.xml.XMLParser;

/**
 * The REBIND method removes a binding to a resource from a collection,
 * and adds a binding to that resource into the collection identified by
 * the Request-URI.  The request body specifies the binding to be added
 * (segment) and the old binding to be removed (href).  It is
 * effectively an atomic form of a MOVE request, and MUST be treated the
 * same way as MOVE for the purpose of determining access permissions.
 * 
 * Description from RFC5842 (c) 2010 IETF Trust and the persons identified 
 * as the document authors
 * http://www.ietf.org/rfc/rfc5842.txt
 * 
 */

public class RebindActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String status = "200 OK";
        String type = "";
        String content = "";
        
        String overwrite = "T";
        if (requestParams.headerExists("Overwrite")) {
            overwrite = requestParams.getHeader("Overwrite"); 
        }
        
        String xml = requestParams.getRequestBody();
        String host = requestParams.getHeader("Host");
        
        XMLParser xmlParser = new XMLParser();
        HashMap<String, Object> xmldata = xmlParser.simpleXMLParser(xml, ConfigService.CHARSET, false);
        if (xmldata == null || xmldata.size() == 0) {
            status = "400 Bad Request";
            type = "text/plain";
            content = "400 Bad Request";
        }
        else {
            String segment = (String)xmldata.get("{DAV:}segment");
            String href = (String)xmldata.get("{DAV:}href");
            
            href = href.replaceFirst("^https?://" + Pattern.quote(host) + "+" + ConfigService.VIRTUAL_BASE, "");
            href = RenderingHelper.uri_unescape(RenderingHelper.uri_unescape(href));
            String src = ConfigService.DOCUMENT_ROOT + href;
            String dst = requestParams.getPathTranslated() + segment;
            
            String nsrc = new String(src).replaceFirst("/$", "");
            String ndst = new String(dst).replaceFirst("/$", "");
            
            if (!FileOperationsService.file_exits(src)) {
                status = "404 Not Found";
            }
            else if (!FileOperationsService.is_symbolic_link(requestParams, nsrc)) {
                status = "403 Forbidden";
            }
            else if (FileOperationsService.file_exits(dst) && !overwrite.equals("T")) {
                status = "403 Forbidden";
            }
            else if (FileOperationsService.file_exits(dst) && !FileOperationsService.is_symbolic_link(requestParams, ndst)) {
                status = "403 Forbidden";
            }
            else {
                if (FileOperationsService.is_symbolic_link(requestParams, ndst)) {
                    status = "204 No Content";
                }
                else {
                    status = "201 Created";
                }
                
                if (FileOperationsService.is_symbolic_link(requestParams, ndst)) {
                    FileOperationsService.unlink(requestParams, ndst);
                }
                
                if (!FileOperationsService.rename(nsrc, ndst)) {
                    String orig = FileOperationsService.readlink(requestParams, nsrc);
                    if (!(FileOperationsService.symlink(orig, dst) && FileOperationsService.unlink(requestParams, nsrc))) {
                        status = "403 Forbidden";
                    }
                }
                 
            }
        }
        
        OutputService.printHeaderAndContent(requestParams, status, type, content);
    }
    
}
