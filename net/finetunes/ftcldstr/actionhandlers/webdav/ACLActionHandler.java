package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService.StatData;
import net.finetunes.ftcldstr.routines.webdav.LockingService;
import net.finetunes.ftcldstr.routines.xml.XMLParser;

public class ACLActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        String status = "200 OK";
        String content = "";
        String type = "";
        // my %error;
        Logger.debug("ACL(" + fn + ")");
        String xml = requestParams.getRequestBody();
        
        XMLParser xmlParser = new XMLParser();
        HashMap<String, Object> xmldata = xmlParser.simpleXMLParser(xml, ConfigService.CHARSET, true);
        if (xmldata == null || xmldata.size() == 0) {
            Logger.debug("ACL: invalid XML request: " + xml);
            status = "400 Bad Request";
            type = "text/plain";
            content = "400 Bad Request";
        }
        else if (!FileOperationsService.file_exits(fn)) {
            status = "404 Not Found";
            type = "text/plain";
            content = "404 Not Found";
        }
        else if (!LockingService.isAllowed(requestParams, fn)) {
            status = "423 Locked";
            type = "text/plain";
            content = "423 Locked";   
        }
        else if (!xmldata.containsKey("{DAV:}acl")) {
            status = "400 Bad Request";
            type = "text/plain";
            content = "400 Bad Request";   
        }
        else {
            ArrayList<HashMap<String, Object>> ace = new ArrayList<HashMap<String,Object>>();
            if (((HashMap<String, Object>)xmldata.get("{DAV:}acl")).get("{DAV:}ace") instanceof HashMap<?, ?>) {

                ace.add((HashMap<String, Object>)((HashMap<String, Object>)xmldata.get("{DAV:}acl")).get("{DAV:}ace"));
            }
            else if (((HashMap<String, Object>)xmldata.get("{DAV:}acl")).get("{DAV:}ace").getClass().isArray()) {
                
                HashMap<String, Object>[] aclArray = (HashMap<String, Object>[])((HashMap<String, Object>)xmldata.get("{DAV:}acl")).get("{DAV:}ace");
                List<HashMap<String, Object>> aclList = Arrays.asList(aclArray);
                ace.addAll(aclList);
            }
            else {
                OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
                return;
            }
            
            Iterator<HashMap<String, Object>> it = ace.iterator();
            while (it.hasNext()) {
                HashMap<String, Object> ac = it.next();

                HashMap<String, Object> p;
                boolean user = false;
                boolean group = false;
                boolean other = false;
                
                p = (HashMap<String, Object>)ac.get("{DAV:}principal");
                if (p != null) {
                    if (p.get("{DAV:}property") instanceof HashMap<?, ?> && ((HashMap<String, Object>)p.get("{DAV:}property")).containsKey("{DAV:}owner")) {
                        user = true;
                    }
                    else if (p.get("{DAV:}property") instanceof HashMap<?, ?> && ((HashMap<String, Object>)p.get("{DAV:}property")).containsKey("{DAV:}group")) {
                        group = true;
                    }
                    else if (p.containsKey("{DAV:}all")) {
                        other = true;
                    }
                    else {
                        OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
                        return;
                    }
                }
                else {
                    OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
                    return;
                }
                
                int read = 0;
                int write = 0;
                
                if (ac.containsKey("{DAV:}grant")) {
                    if (ac.get("{DAV:}grant") != null &&
                            ((HashMap<String, Object>)ac.get("{DAV:}grant")).get("{DAV:}privilege") != null &&
                            ((HashMap<String, Object>)((HashMap<String, Object>)ac.get("{DAV:}grant")).get("{DAV:}privilege")).containsKey("{DAV:}read")) {
                        read = 1;
                    }
                    if (ac.get("{DAV:}grant") != null &&
                            ((HashMap<String, Object>)ac.get("{DAV:}grant")).get("{DAV:}privilege") != null &&
                            ((HashMap<String, Object>)((HashMap<String, Object>)ac.get("{DAV:}grant")).get("{DAV:}privilege")).containsKey("{DAV:}write")) {
                        write = 1;
                    }
                }
                else if (ac.containsKey("{DAV:}deny")) {
                    if (ac.get("{DAV:}deny") != null &&
                            ((HashMap<String, Object>)ac.get("{DAV:}deny")).get("{DAV:}privilege") != null &&
                            ((HashMap<String, Object>)((HashMap<String, Object>)ac.get("{DAV:}deny")).get("{DAV:}privilege")).containsKey("{DAV:}read")) {
                        read = -1;
                    }
                    if (ac.get("{DAV:}deny") != null &&
                            ((HashMap<String, Object>)ac.get("{DAV:}deny")).get("{DAV:}privilege") != null &&
                            ((HashMap<String, Object>)((HashMap<String, Object>)ac.get("{DAV:}deny")).get("{DAV:}privilege")).containsKey("{DAV:}write")) {
                        write = -1;
                    }
                }
                else {
                    OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
                    return;
                }
                
                StatData stat = FileOperationsService.stat(fn);
                int mode = stat.getMode();
                mode = mode & 07777;
                
                int newperm = mode;
                if (read != 0) {
                    int mask;
                    if (user) {
                        mask = 0400;
                    }
                    else if (group) {
                        mask = 0040;
                    }
                    else {
                        mask = 0004;
                    }
                    if (read > 0) {
                        newperm = newperm | mask;
                    }
                    else {
                        newperm = newperm & ~mask;
                    }
                }
                if (write != 0) {
                    int mask;
                    if (user) {
                        mask = 0200;
                    }
                    else if (group) {
                        mask = 0020;
                    }
                    else {
                        mask = 0002;
                    }
                    if (write > 0) {
                        newperm = newperm | mask;
                    }
                    else {
                        newperm = newperm & ~mask;
                    }
                }
                
                Logger.debug("ACL: old perm=" + String.format("%4o", mode) + ", new perm=" + String.format("%4o", newperm));
                
                if (!FileOperationsService.chmod(newperm, fn)) {
                    status = "403 Forbidden";
                    type = "text/plain";
                    content = "403 Forbidden";
                }
            }
        }
   
        OutputService.printHeaderAndContent(requestParams, status, type, content);
    }
    
}
