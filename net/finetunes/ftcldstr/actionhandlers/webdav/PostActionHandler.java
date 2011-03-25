package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesHelper;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;

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
        
        // if (!$cgi->param('file_upload') && $cgi->cgi_error) {
        //     printHeaderAndContent($cgi->cgi_error,undef,$cgi->cgi_error);   
        //     exit 0;
        // }
        
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
            
            if (requestParams.multipartRequestParamExists("delete")) {
                if (files != null && files.length > 0) {
                    
                    int count = 0;
                    for (int i = 0; i < files.length; i++) {
                        String file = files[i];
                        Logger.debug("POST: delete " + fn + file);
                        
                        if (ConfigService.ENABLE_TRASH) {
                            FileHelper.moveToTrash(requestParams, fn + file);
                        }
                        else {
                            count += FileHelper.deltree(requestParams, fn + file, new ArrayList<String[]>());
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
                if (files != null && files.length > 0) {
                    if (requestParams.multipartRequestParamExists("newname")) {
                        
                        
                        String newname = requestParams.getMultipartRequestParam("newname");
                        
                        if (files.length > 1 && !FileOperationsService.is_directory(requestParams, fn + newname)) {
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
                                
                                if (FileOperationsService.rmove(requestParams, fn + file, fn + newname)) {
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
                    if (FileOperationsService.mkdir(requestParams, fn + colname, mkdirErrors)) {
                        Logger.log("MKCOL(" + fn + colname + ") via POST");
                        msg = "foldercreated";
                    }
                    else {
                        errmsg = "foldererr";
                        String em = "";
                        if (mkdirErrors != null && mkdirErrors.size() > 0) {
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
                if (files != null && files.length > 0) {
                    
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
                        FileOperationsService.changeFilePermissions(requestParams, fn + file, mode, 
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
        else if (ConfigService.ALLOW_POST_UPLOADS && FileOperationsService.is_directory(requestParams, fn) && requestParams.multipartRequestParamExists("filesubmit")) {

            ArrayList<String> filelist = new ArrayList<String>();
            ArrayList<String> files = requestParams.getFileNames();
            if (files != null) {
                Iterator<String> it = files.iterator();
                while (it.hasNext()) {
                    String filename = it.next();
                    if (filename.isEmpty()) {
                        continue;
                    }
                    
                    File f = requestParams.getFile(filename);

                    String rfn = new String(f.getName());
                    rfn = rfn.replaceAll("\\\\", "/"); // # fix M$ Windows backslashes
                    String destination = fn + FileOperationsService.basename(rfn);
                    Logger.debug("POST: save " + f.getName() + " to " + destination + ".");
                    filelist.add(FileOperationsService.basename(rfn));
                    FileInputStream is = null;
                    try {
                        is = new FileInputStream(f);
                    }
                    catch (FileNotFoundException e) {
                        Logger.log("Exception: Unable to read the file. " + e.getMessage());
                    }
                    
                    if (!(is != null && FileOperationsService.writeFileFromStream(requestParams, destination, is))) {
                        OutputService.printHeaderAndContent(requestParams, "403 Forbidden", "text/plain", "403 Forbidden");
                        break;
                    }
                }
                
                if (filelist.size() > 0) {
                    if (filelist.size() > 1) {
                        msg = "uploadmulti";
                    }
                    else {
                        msg = "uploadsingle";
                    }

                    String m = RenderingHelper.joinArray(filelist.toArray(new String[]{}), ", ");
                    if (m.length() > 150) {
                        m = m.substring(0, 150);
                    }

                    msgparam = "p1=" + filelist.size() + ConfigService.URL_PARAM_SEPARATOR +
                        "p2=" + RenderingHelper.uri_escape(m);
                }
                else {
                    errmsg = "uploadnothingerr"; 
                }
    
                try {
                    requestParams.getResponse().sendRedirect(redirtarget + RenderingService.createMsgQuery(msg, msgparam, errmsg, msgparam));
                }
                catch (IOException e) {
                    Logger.log("Exception: Unable to perform redirect. " + e.getMessage());
                }
            }
        }
        else if (ConfigService.ALLOW_ZIP_DOWNLOAD && requestParams.multipartRequestParamExists("zip")) {
            
            try {
                String zfn = FileOperationsService.basename(fn) + ".tar.gz";
                zfn = zfn.replaceAll(" ", "_");
                ZipOutputStream out = new ZipOutputStream(requestParams.getResponse().getOutputStream());
                
                requestParams.getResponse().setStatus(200);
                requestParams.getResponse().addHeader("Content-Type", "application/x-gzip");
                requestParams.getResponse().addHeader("Content-Disposition", "attachment; filename=" + zfn);
                
                String[] files = requestParams.getMultipartRequestParamValues("file");
                ArrayList<String> filesToArchive = new ArrayList<String>();
                
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        String file = files[i];
                        // String filename = fn + file;
                        String filename = file; // fn is passed as tar argument (-C)
                        filesToArchive.add(filename);
                    }
                    
                    String fileList = RenderingHelper.joinArray(filesToArchive.toArray(new String[]{}), "' '");
                    if (fileList != null && !fileList.isEmpty()) {
                        fileList = "'" + fileList + "'";
                        OutputService.printContentStream(requestParams, WrappingUtilities.getZippedContentReadStream(requestParams, fn, fileList)); 
                    }
                }
            }
            catch (IOException e) {
                Logger.log("Exception on creating an archive: " + e.getMessage());
            }
        }
        else if (ConfigService.ALLOW_ZIP_UPLOAD && requestParams.multipartRequestParamExists("uncompress")) {
            
          ArrayList<String> files = requestParams.getFileNames();
          ArrayList<String> uploadedFiles = new ArrayList<String>();
          //    requestParams.getMultipartRequestParamValues("zipfile_upload");
          if (files != null) {
              Iterator<String> it = files.iterator();
              while (it.hasNext()) {
                  String fh = it.next();
                  String rfn = new String(fh);
                  rfn = rfn.replaceAll("\\\\", "/"); // # fix M$ Windows backslashes
                  rfn = FileOperationsService.basename(rfn);
                  
                  File f = requestParams.getFile(fh);
                  uploadedFiles.add(f.getName());
                  FileInputStream is = null;
                  try {
                      is = new FileInputStream(f);
                  }
                  catch (FileNotFoundException e) {
                      Logger.log("Exception: Unable to read the file: " + fh + ". Message: " + e.getMessage());
                  }              
                  
                  FileOperationsService.writeFileFromStream(requestParams, f.getAbsolutePath(), is);
                  WrappingUtilities.unzip(requestParams, f.getAbsolutePath(), fn);
                  FileOperationsService.unlink(requestParams, f.getAbsolutePath());
              }
          }

          if (uploadedFiles != null && uploadedFiles.size() > 0) {
              if (uploadedFiles.size() > 1) {
                  msg = "zipuploadmulti";
              }
              else {
                  msg = "zipuploadsingle"; 
              }
              
              String m = RenderingHelper.joinArray(uploadedFiles.toArray(new String[]{}), ", ");
              if (m.length() > 150) {
                  m = m.substring(0, 150);
              }
              
              msgparam = "p1=" + (files.size()) + ConfigService.URL_PARAM_SEPARATOR +
                  "p2=" + RenderingHelper.uri_escape(m);
          }
          else {
              errmsg = "zipuploadnothingerr";
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
        else if (ConfigService.ENABLE_CALDAV_SCHEDULE && FileOperationsService.is_directory(requestParams, fn)) {
            // ## NOT IMPLEMENTED YET // was original perl code;
        }
        else {
            Logger.debug("POST: forbidden POST to " + fn);
            OutputService.printHeaderAndContent(requestParams, "403 Forbidden", "text/plain", "403 Forbidden");
        }
    }
    
}
