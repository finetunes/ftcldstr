package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.processing.RoundEnvironment;

import com.oreilly.servlet.MultipartRequest;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.rendering.RenderingService;
import net.finetunes.ftcldstr.routines.fileoperations.FileHelper;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.QueryService;

/**
 * The POST method is used to request that the origin server accept the
 * entity enclosed in the request as a new subordinate of the resource
 * identified by the Request-URI in the Request-Line.
 * 
 * Since by definition the actual function performed by POST is
 * determined by the server and often depends on the particular
 * resource, the behavior of POST when applied to collections cannot be
 * meaningfully modified because it is largely undefined.  Thus, the
 * semantics of POST are unmodified when applied to a collection.
 * 
 * Descriptions from RF4918 (c) The IETF Trust (2007)
 * and RFC 2616 (C) The Internet Society (1999).
 * 
 * http://www.ietf.org/rfc/rfc4918.txt
 * http://www.ietf.org/rfc/rfc2616.txt
 * 
 */

public class PostActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {

        String fn = requestParams.getPathTranslated();
        Logger.debug("POST: " + fn);
        
        /*
        TODO
        if (!$cgi->param('file_upload') && $cgi->cgi_error) {
            printHeaderAndContent($cgi->cgi_error,undef,$cgi->cgi_error);   
            exit 0;
        }
        */
        
        String msg = null;
        String msgparam = null;
        String errmsg = null;
        ArrayList<String> err = new ArrayList<String>();

        String redirtarget = requestParams.getRequestURI();
        int index = redirtarget.indexOf("?");
        if (index >= 0) {
            redirtarget = redirtarget.substring(0, index);
        }
        
        if (ConfigService.ALLOW_FILE_MANAGEMENT && (
                requestParams.multipartRequestParamExists("delete") || 
                requestParams.multipartRequestParamExists("rename") || 
                requestParams.multipartRequestParamExists("mkcol") || 
                requestParams.multipartRequestParamExists("changeperm"))) {
            
            
            String[] files = requestParams.getMultipartRequestParamValues("file");
            String fileList = RenderingHelper.joinArray(files, ",");
            Logger.debug("POST: file management " + fileList);
            
            if (requestParams.requestParamExists("delete")) {
                if (files.length > 0) {
                    
                    int count = 0;
                    for (int i = 0; i < files.length; i++) {
                        String file = files[i];
                        Logger.debug("POST: delete " + fn + file);
                        
                        if (ConfigService.ENABLE_TRASH) {
                            FileHelper.moveToTrash(fn + file); // TODO: implement
                        }
                        else {
                            // $count += deltree($PATH_TRANSLATED.$file, \my @err);
                            // TODO: count += FileHelper.deltree(fn + file, err); // param?
                        }
                        
                        Logger.log("DELETE(" + fn + ") via POST");
                    }
                    
                    if (count > 0) {
                        if (count > 1) {
                            msg = "deletedmulti"; 
                        }
                        else {
                            msg = "deletedsingle";
                        }
                        
                        msgparam = "p1=" + count;
                    }
                    else {
                        errmsg = "deleteerr";
                    }
                }
                else {
                    errmsg = "deletenothingerr";
                }
            }
            else if (requestParams.multipartRequestParamExists("rename")) {
                if (files.length > 0) {
                    if (requestParams.multipartRequestParamExists("newname")) {
                        
                        
                        String newname = requestParams.getMultipartRequestParam("newname");
                        
                        if (files.length > 1 && !FileOperationsService.is_directory(fn + newname)) {
                            OutputService.printHeaderAndContent(requestParams, "403 Forbidden", "text/plain", "403 Forbidden");
                            return;
                        }
                        else {
                            msg = "rename";
                            String fileList2 = RenderingHelper.joinArray(files, ", ");
                            msgparam = "p1=" + RenderingHelper.uri_escape(fileList2) + 
                                ConfigService.URL_PARAM_SEPARATOR + "p2=" + RenderingHelper.uri_escape(newname);
                            
                            for (int i = 0; i < files.length; i++) {
                                String file = files[i];
                                
                                if (FileOperationsService.rmove(fn + file, fn + newname)) { // TODO: implement
                                    Logger.log("MOVE(" + fn + "," + fn + "" + newname + ") via POST");
                                }
                                else {
                                    errmsg = "renameerr";
                                }
                            }
                        }
                    }
                    else {
                        errmsg = "renamenotargeterr";
                    }
                }
                else {
                    errmsg = "renamenothingerr";
                }
            }
            else if (requestParams.multipartRequestParamExists("mkcol")) {
                if (requestParams.multipartRequestParamExists("colname")) {
                    
                    String mkcol = requestParams.getMultipartRequestParam("mkcol");
                    String colname = requestParams.getMultipartRequestParam("colname");
                    
                    msgparam = "p1=" + RenderingHelper.uri_escape(colname);
                    ArrayList<String> mkdirErrors = new ArrayList<String>();
                    if (FileOperationsService.mkdir(fn + colname, mkdirErrors)) { // TODO: implement
                        Logger.log("MKCOL(" + fn + colname + ") via POST");
                        msg = "foldercreated";
                    }
                    else {
                        errmsg = "foldererr";
                        String em = "";
                        if (mkdirErrors.size() > 0) {
                            em = mkdirErrors.get(0);
                        }
                        msgparam += ConfigService.URL_PARAM_SEPARATOR + "p2=" + RenderingHelper.uri_escape(em); 
                    }
                }
                else {
                    errmsg = "foldernothingerr";
                }
            }
            else if (requestParams.multipartRequestParamExists("changeperm")) {
                if (files.length > 0) {
                    
                    int mode = 0000;
                    String[] fp_user = requestParams.getMultipartRequestParamValues("fp_user");
                    for (int i = 0; i < fp_user.length; i++) {
                        String userperm = fp_user[i];
                        if (userperm.equals("r") && ConfigService.PERM_USER.contains("r")) { mode = mode | 0400; }
                        if (userperm.equals("w") && ConfigService.PERM_USER.contains("w")) { mode = mode | 0200; }
                        if (userperm.equals("x") && ConfigService.PERM_USER.contains("x")) { mode = mode | 0100; }
                        if (userperm.equals("s") && ConfigService.PERM_USER.contains("s")) { mode = mode | 04000; }
                    }
                    
                    String[] fp_group = requestParams.getMultipartRequestParamValues("fp_group");
                    for (int i = 0; i < fp_group.length; i++) {
                        String userperm = fp_group[i];
                        if (userperm.equals("r") && ConfigService.PERM_GROUP.contains("r")) { mode = mode | 0040; }
                        if (userperm.equals("w") && ConfigService.PERM_GROUP.contains("w")) { mode = mode | 0020; }
                        if (userperm.equals("x") && ConfigService.PERM_GROUP.contains("x")) { mode = mode | 0010; }
                        if (userperm.equals("s") && ConfigService.PERM_GROUP.contains("s")) { mode = mode | 02000; }
                    }
                    
                    String[] fp_others = requestParams.getMultipartRequestParamValues("fp_others");
                    for (int i = 0; i < fp_others.length; i++) {
                        String userperm = fp_others[i];
                        if (userperm.equals("r") && ConfigService.PERM_USER.contains("r")) { mode = mode | 0004; }
                        if (userperm.equals("w") && ConfigService.PERM_USER.contains("w")) { mode = mode | 0002; }
                        if (userperm.equals("x") && ConfigService.PERM_USER.contains("x")) { mode = mode | 0001; }
                        if (userperm.equals("t") && ConfigService.PERM_USER.contains("t")) { mode = mode | 01000; }
                    }
                    
                    msg = "changeperm";
                    msgparam = String.format("p1=%04o", mode);
                    String fp_type = requestParams.getMultipartRequestParam("fp_type");
                    String fp_recursive = requestParams.getMultipartRequestParam("fp_recursive");
                    
                    for (int i = 0; i < files.length; i++) {
                        String file = files[i];
                        FileOperationsService.changeFilePermissions(fn + file, mode, 
                                fp_type, ConfigService.ALLOW_CHANGEPERMRECURSIVE && (fp_recursive != null && !fp_recursive.isEmpty()));
                    }
                }
                else {
                    errmsg = "chpermnothingerr";
                }
            }
            
            String rt = redirtarget + RenderingService.createMsgQuery(msg, msgparam, errmsg, msgparam); 
            try {
                
                requestParams.getResponse().sendRedirect(rt);
            }
            catch (IOException e) {
                Logger.log("Error: Unable to perform redirect (" + rt + "): " + e.getMessage());
                System.err.println("Error: Unable to perform redirect (" + rt + "): " + e.getMessage());
            }
        }
        else if (ConfigService.ALLOW_POST_UPLOADS && FileOperationsService.is_directory(fn) && requestParams.requestParamExists("file_upload")) {
/*            
  
        my @filelist;
        foreach my $filename ($cgi->param('file_upload')) {
            next if $filename eq "";
            my $rfn= $filename;
            $rfn=~s/\\/\//g; # fix M$ Windows backslashes
            my $destination = $PATH_TRANSLATED.basename($rfn);
            debug("_POST: save $filename to $destination.");
            push(@filelist, basename($rfn));
            if (open(O,">$destination")) {
                while (read($filename,my $buffer,$BUFSIZE)>0) {
                    print O $buffer;
                }
                close(O);
            } else {
                printHeaderAndContent('403 Forbidden','text/plain','403 Forbidden');
                last;
            }
        }
        if ($#filelist>-1) {
            $msg=($#filelist>0)?'uploadmulti':'uploadsingle';
            $msgparam='p1='.($#filelist+1).';p2='.$cgi->escape(substr(join(', ',@filelist), 0, 150));
        } else {
            $errmsg='uploadnothingerr';
        }
        print $cgi->redirect($redirtarget.createMsgQuery($msg,$msgparam,$errmsg,$msgparam));  
  
*/            
        }
        else if (ConfigService.ALLOW_ZIP_DOWNLOAD && requestParams.requestParamExists("zip")) {
/*            
            my $zip =  Archive::Zip->new();     
            foreach my $file ($cgi->param('file')) {
                if (-d $PATH_TRANSLATED.$file) {
                    $zip->addTree($PATH_TRANSLATED.$file, $file);
                } else {
                    $zip->addFile($PATH_TRANSLATED.$file, $file);
                }
            }
            my $zfn = basename($PATH_TRANSLATED).'.zip';
            $zfn=~s/ /_/;
            print $cgi->header(-status=>'200 OK', -type=>'application/zip',-Content_disposition=>'attachment; filename='.$zfn);
            $zip->writeToFileHandle(\*STDOUT,0);
*/            
        }
        else if (ConfigService.ALLOW_ZIP_UPLOAD && requestParams.requestParamExists("uncompress")) {
/*

        my @zipfiles;
        foreach my $fh ($cgi->param('zipfile_upload')) {
            my $rfn= $fh;
            $rfn=~s/\\/\//g; # fix M$ Windows backslashes
            $rfn=basename($rfn);
            if (open(F,">$PATH_TRANSLATED$rfn")) {
                push @zipfiles, $rfn;
                print F $_ while (<$fh>);
                close(F);
                my $zip = Archive::Zip->new();
                my $status = $zip->read($PATH_TRANSLATED.$rfn);
                if ($status eq $zip->AZ_OK) {
                    $zip->extractTree(undef, $PATH_TRANSLATED);
                    unlink($PATH_TRANSLATED.$rfn);
                }
            }
        }
        if ($#zipfiles>-1) {
            $msg=($#zipfiles>0)?'zipuploadmulti':'zipuploadsingle';
            $msgparam='p1='.($#zipfiles+1).';p2='.$cgi->escape(substr(join(', ',@zipfiles), 0, 150));
        } else {
            $errmsg='zipuploadnothingerr';
        }
        print $cgi->redirect($redirtarget.createMsgQuery($msg,$msgparam,$errmsg,$msgparam)); 
*/
        }
        else if (ConfigService.ENABLE_CALDAV_SCHEDULE && FileOperationsService.is_directory(fn)) {
            // ## NOT IMPLEMENTED YET // PZ: that was original perl code; TODO?
        }
        else {
/*            
            debug("_POST: forbidden POST to $PATH_TRANSLATED");
            printHeaderAndContent('403 Forbidden','text/plain','403 Forbidden');
*/            
        }
    }
    
}
