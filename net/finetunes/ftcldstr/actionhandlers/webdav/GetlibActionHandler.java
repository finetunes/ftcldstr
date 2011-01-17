package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.HashMap;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;

public class GetlibActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        String status = "200 OK";
        String type = "";
        String content = "";
        HashMap<String, String> params = new HashMap<String, String>();
        
        if (!FileOperationsService.file_exits(fn)) {
            status = "404 Not Found";
            type = "text/plain";
        }
        else {
            String su = requestParams.getScriptURI();
            params.put("MS-Doclib", su);
        }
        
        OutputService.printHeaderAndContent(requestParams, status, type, content, params);
    }
}
