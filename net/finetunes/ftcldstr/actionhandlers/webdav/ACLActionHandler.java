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

                // my $p; // TODO: type
                HashMap<String, Object> p;
                int user = 0;
                int group = 0;
                int other = 0;
                
                p = (HashMap<String, Object>)ac.get("{DAV:}principal");
                if (p != null) {
                    if (p.get("{DAV:}property") instanceof HashMap<?, ?> && ((HashMap<String, Object>)p.get("{DAV:}property")).containsKey("{DAV:}owner")) {
                        user = 1;
                    }
                    else if (p.get("{DAV:}property") instanceof HashMap<?, ?> && ((HashMap<String, Object>)p.get("{DAV:}property")).containsKey("{DAV:}group")) {
                        group = 1;
                    }
                    else if (p.containsKey("{DAV:}all")) {
                        other = 1;
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
                
                /*        
                 * TODO:
                     my ($read,$write) = (0,0);
                    if (exists $$ace{'{DAV:}grant'}) {
                        $read=1 if exists $$ace{'{DAV:}grant'}{'{DAV:}privilege'}{'{DAV:}read'};
                        $write=1 if exists $$ace{'{DAV:}grant'}{'{DAV:}privilege'}{'{DAV:}write'};
                    } elsif (exists $$ace{'{DAV:}deny'}) {
                        $read=-1 if exists $$ace{'{DAV:}deny'}{'{DAV:}privilege'}{'{DAV:}read'};
                        $write=-1 if exists $$ace{'{DAV:}deny'}{'{DAV:}privilege'}{'{DAV:}write'};
                    } else {
                        printHeaderAndContent('400 Bad Request');
                        return;
                        
                    }
                    if ($read==0 && $write==0) {
                        printHeaderAndContent('400 Bad Request');
                        return;
                    }
                    my @stat = stat($fn);
                    my $mode = $stat[2];
                    $mode = $mode & 07777;
                    
                    my $newperm = $mode;
                    if ($read!=0) {
                        my $mask = $user? 0400 : $group ? 0040 : 0004;
                        $newperm = ($read>0) ? $newperm | $mask : $newperm & ~$mask
                    } 
                    if ($write!=0) {
                        my $mask = $user? 0200 : $group ? 0020 : 0002;
                        $newperm = ($write>0) ? $newperm | $mask : $newperm & ~$mask;
                    }
                    debug("_ACL: old perm=".sprintf('%4o',$mode).", new perm=".sprintf('%4o',$newperm));
                    if (!chmod($newperm, $fn)) {
                        $status='403 Forbidden';
                        $type='text/plain';
                        $content='403 Forbidden';
                    }
        */                     
                
            }
        }
   
        // TODO: implement
        OutputService.printHeaderAndContent(requestParams, status, type, content);
    }
    
}
