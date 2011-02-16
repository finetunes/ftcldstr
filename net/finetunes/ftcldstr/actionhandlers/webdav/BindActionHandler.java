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
 * The BIND method modifies the collection identified by the Request-
 * URI, by adding a new binding from the segment specified in the BIND
 * body to the resource identified in the BIND body.
 * 
 * If a server cannot guarantee the integrity of the binding, the BIND
 * request MUST fail.
 * 
 * Description from RFC5842 (c) 2010 IETF Trust and the persons identified 
 * as the document authors
 * http://www.ietf.org/rfc/rfc5842.txt
 * 
 */

public class BindActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String status = "200 OK";
        String type = null;
        String content = null;
        
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
            
            String ndst = new String(dst).replaceFirst("/$", "");
            
            if (!FileOperationsService.file_exits(src)) {
                status = "404 Not Found";
            }
            else if (FileOperationsService.file_exits(dst) && !FileOperationsService.is_symbolic_link(requestParams, ndst)) {
                status = "403 Forbidden";
            }
            else if (FileOperationsService.file_exits(dst) && FileOperationsService.is_symbolic_link(requestParams, ndst) &&
                    overwrite.equals("F")) {
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
                
                if (!FileOperationsService.symlink(src, dst)) {
                    status = "403 Forbidden";
                }
            }
        }
        
        OutputService.printHeaderAndContent(requestParams, status, type, content);
    }    
}
