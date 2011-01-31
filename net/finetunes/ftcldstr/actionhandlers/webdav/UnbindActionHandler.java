package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.HashMap;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.xml.XMLParser;

public class UnbindActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String status = "204 No Content";
        String type = "";
        String content = "";
        
        String xml = requestParams.getRequestBody();
        
        XMLParser xmlParser = new XMLParser();
        HashMap<String, Object> xmldata = xmlParser.simpleXMLParser(xml, ConfigService.CHARSET, false);
        if (xmldata == null || xmldata.size() == 0) {
            status = "400 Bad Request";
            type = "text/plain";
            content = "400 Bad Request";
        }
        else {
            String segment = (String)xmldata.get("{DAV:}segment");
            String dst = requestParams.getPathTranslated() + segment;
            if (!FileOperationsService.file_exits(dst)) {
                status = "404 Not Found";
            }
            else if (!FileOperationsService.is_symbolic_link(dst)) {
                status = "403 Forbidden";
            }
            else if (!FileOperationsService.unlink(dst)) {
                status = "403 Forbidden";
            }
        }
        
        OutputService.printHeaderAndContent(requestParams, status, type, content);
    }
}
