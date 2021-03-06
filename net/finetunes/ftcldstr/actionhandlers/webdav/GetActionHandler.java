package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.MissingFormatArgumentException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.rendering.RenderingService;
import net.finetunes.ftcldstr.routines.NamespaceService;
import net.finetunes.ftcldstr.routines.fileoperations.DirectoryOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService.StatData;
import net.finetunes.ftcldstr.routines.webdav.QueryService;
import net.finetunes.ftcldstr.routines.webdav.SearchService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesActions;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesHelper;
import net.finetunes.ftcldstr.routines.webdav.properties.StatusResponse;
import net.finetunes.ftcldstr.routines.webdav.properties.Properties.FileProperties;
import net.finetunes.ftcldstr.routines.xml.XMLService;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;

/**
 * The GET method means retrieve whatever information (in the form of an
 * entity) is identified by the Request-URI. If the Request-URI refers
 * to a data-producing process, it is the produced data which shall be
 * returned as the entity in the response and not the source text of the
 * process, unless that text happens to be the output of the process.
 * 
 * The semantics of GET are unchanged when applied to a collection,
 * since GET is defined as, "retrieve whatever information (in the form
 * of an entity) is identified by the Request-URI" [RFC2616].  GET, when
 * applied to a collection, may return the contents of an "index.html"
 * resource, a human-readable view of the contents of the collection, or
 * something else altogether.  Hence, it is possible that the result of
 * a GET on a collection will bear no correlation to the membership of
 * the collection.
 * 
 * Descriptions from RF4918 (c) The IETF Trust (2007)
 * and RFC 2616 (C) The Internet Society (1999).
 * 
 * http://www.ietf.org/rfc/rfc4918.txt
 * http://www.ietf.org/rfc/rfc2616.txt
 * 
 */

public class GetActionHandler extends AbstractActionHandler {
    
    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        Logger.debug("GET: " + fn);
        
        if (FileOperationsService.is_hidden(fn)) {
            OutputService.printHeaderAndContent(requestParams, "404 Not Found", "text/plain", "404 - NOT FOUND");
        }
        else if (FileOperationsService.is_directory(requestParams, fn) && !ConfigService.FANCYINDEXING) {
            OutputService.printHeaderAndContent(requestParams, "404 Not Found", "text/plain", "404 - NOT FOUND");
        }
        else if (FileOperationsService.file_exits(requestParams, fn) && 
                requestParams.getRequest().getParameter("action") != null && requestParams.getRequest().getParameter("action").equals("davmount")) {
            doDavmountRequest(requestParams, fn);
        }
        else if (ConfigService.ENABLE_THUMBNAIL && FileOperationsService.is_plain_file(requestParams, fn) &&
                FileOperationsService.is_file_readable(requestParams, fn) &&
                requestParams.getRequest().getParameter("action") != null && requestParams.getRequest().getParameter("action").equals("thumb")) {
            doThumbnailRequest(requestParams, fn);
        }
        else if (FileOperationsService.file_exits(requestParams, fn) &&
                requestParams.getRequest().getParameter("action") != null && requestParams.getRequest().getParameter("action").equals("props")) {
            doFilePropertiesRequest(requestParams, fn);
        }
        else if (FileOperationsService.is_directory(requestParams, fn)) {
            doFileIsDirectory(requestParams, fn);
        }
        else if (FileOperationsService.file_exits(requestParams, fn) &&
                !FileOperationsService.is_file_readable(requestParams, fn)) {
            OutputService.printHeaderAndContent(requestParams, "403 Forbidden", "text/plain", "403 Forbidden");
        }
        else if (FileOperationsService.file_exits(requestParams, fn)) {
            doFileExists(requestParams, fn);
        }
        else {
            Logger.debug("GET: " + fn + " NOT FOUND!");
            OutputService.printHeaderAndContent(requestParams, "404 Not Found", "text/plain", "404 - FILE NOT FOUND");
        }
    }
    
    private void doDavmountRequest(RequestParams requestParams, String fn) {
        
        // my $su = $ENV{REDIRECT_SCRIPT_URI} || $ENV{SCRIPT_URI};
        String su = requestParams.getScriptURI();
        String bn = FileOperationsService.splitFilename(fn)[1];
        
        su = su.replaceFirst(Pattern.quote(bn) + "/?", ""); // $su =~ s/\Q$bn\E\/?//;
        if (FileOperationsService.is_directory(requestParams, fn) && !bn.endsWith("/")) {
            bn += "/";
        }
        
        OutputService.printHeaderAndContent(requestParams, "200 OK", "application/davmount+xml", 
                "<dm:mount xmlns:dm=\"http://purl.org/NET/webdav/mount\"><dm:url>" + su +  "</dm:url><dm:open>" + bn + "</dm:open></dm:mount>");
    }
    
    private void doThumbnailRequest(RequestParams requestParams, String fn) {
        
        int width = 22;
        if (ConfigService.THUMBNAIL_WIDTH > 0) {
            width = ConfigService.THUMBNAIL_WIDTH;
        }
        else if (ConfigService.ICON_WIDTH > 0) {
            width = ConfigService.ICON_WIDTH;
        }
        
        StatData statfn = FileOperationsService.stat(requestParams, fn);
        if (ConfigService.ENABLE_THUMBNAIL_CACHE) {
            String uniqname = fn;
            uniqname = uniqname.replaceAll("/", "_");
            
            String cachefile = ConfigService.THUMBNAIL_CACHEDIR + "/" + uniqname + ".thumb";
            if (!FileOperationsService.file_exits(requestParams, ConfigService.THUMBNAIL_CACHEDIR)) {
                FileOperationsService.mkdir(requestParams, ConfigService.THUMBNAIL_CACHEDIR);
            }
            
            StatData statcf = FileOperationsService.stat(requestParams, cachefile);
            if (!FileOperationsService.file_exits(requestParams, cachefile) || statfn.getMtimeDate().after(statcf.getMtimeDate())) {
                
                try {
                    BufferedImage image = ImageIO.read(FileOperationsService.getFileContentStream(requestParams, fn));
                    
                    int type = image.getType();
                    if (type == 0) {
                        type = BufferedImage.TYPE_INT_ARGB;
                    }
                    
                    int height = image.getHeight() * width / image.getWidth(); 
                    BufferedImage resizedImage = new BufferedImage(width, height, type);
                    Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(image, 0, 0, width, height, null);
                    g.dispose();
                    g.setComposite(AlphaComposite.Src);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    String imageType = MIMETypesHelper.getFileExtentionByMimeType(fn); 
                    ImageIO.write(resizedImage, imageType, FileOperationsService.getFileWriteStream(requestParams, cachefile));
                }
                catch (IOException e) {
                    Logger.log("Exception on creating cached thumbnail: " + e.getMessage());
                }
            }
            
            InputStream fs = FileOperationsService.getFileContentStream(requestParams, cachefile);
            if (fs != null) {
                StatData stat = FileOperationsService.stat(requestParams, cachefile);
                int contentLength = stat.getSize();
                requestParams.getResponse().setStatus(200);
                requestParams.getResponse().addHeader("Content-Type", MIMETypesHelper.getMIMEType(fn));
                requestParams.getResponse().addHeader("ETag", PropertiesHelper.getETag(requestParams, cachefile, statcf));
                requestParams.getResponse().addHeader("Content-Length", String.valueOf(contentLength));
                
                try {
                    OutputStream outStream = requestParams.getResponse().getOutputStream();
                    
                    byte[] buf = new byte[1024 * 20];
                    int len;
                    while ((len = fs.read(buf)) > 0) {
                        outStream.write(buf, 0, len);
                    }
                }
                catch (IOException e) {
                    Logger.log("Exception on sending the thumbnail: " + e.getMessage());
                }                
            }
        }
        else {
            requestParams.getResponse().setStatus(200);
            requestParams.getResponse().addHeader("Content-Type", MIMETypesHelper.getMIMEType(fn));
            requestParams.getResponse().addHeader("ETag", PropertiesHelper.getETag(requestParams, fn, statfn));
            
            try {
                BufferedImage image = ImageIO.read(FileOperationsService.getFileContentStream(requestParams, fn));
                
                int type = image.getType();
                if (type == 0) {
                    type = BufferedImage.TYPE_INT_ARGB;
                }
                
                int height = image.getHeight() * width / image.getWidth(); 
                BufferedImage resizedImage = new BufferedImage(width, height, type);
                Graphics2D g = resizedImage.createGraphics();
                g.drawImage(image, 0, 0, width, height, null);
                g.dispose();
                g.setComposite(AlphaComposite.Src);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                OutputStream outStream = requestParams.getResponse().getOutputStream();
                String imageType = MIMETypesHelper.getFileExtentionByMimeType(fn); 
                ImageIO.write(resizedImage, imageType, outStream);
            }
            catch (IOException e) {
                Logger.log("Exception on creating thumbnail: " + e.getMessage());
            }
        }
    }
    
    private void doFileIsDirectory(RequestParams requestParams, String fn) {
        
        String ru = requestParams.getRequestURI();
        String content = "";
        Logger.debug("GET: directory listing of " + fn);
        
        content += RenderingService.start_html(ru);
        if (ConfigService.HEADER != null) {
            content += String.format(ConfigService.HEADER, requestParams.getUsername());
        }
        
        if (ConfigService.ALLOW_SEARCH && FileOperationsService.is_file_readable(requestParams, fn)) {
            String search = requestParams.getRequest().getParameter("search");
            
            String form = "";
            form += "<form method=\"get\">";

            form += "<div style=\"text-align:right;font-size:0.8em;padding:2px 0 0 0;border:0;margin:0;\">";
            form += ConfigService.stringMessages.get("search");
            form += " ";
                    
            String inputSize = "10";
            if (search != null && search.length() > 10) {
                inputSize = String.valueOf(search.length());
            }
            
            String inputValue = "";
            if (search != null) {
                inputValue = search;
            }
            
            form += "<input title=\"" + ConfigService.stringMessages.get("searchtooltip") + "\"" +
            		" onkeyup=\"javascript:if (this.size<this.value.length || (this.value.length<this.size && this.value.length>10)) this.size=this.value.length;\"" +
            		" style=\"font-size: 0.8em;\"" +
            		" name=\"search\"" +
            		" size=\"" + inputSize + "\"" +
            		" value=\"" + inputValue + "\">";
            form += "</div>";
            form += "</form>";
            
            content += form;
        }
        
        String msg = requestParams.getRequest().getParameter("errmsg");
        if (msg == null || msg.isEmpty()) {
            msg = requestParams.getRequest().getParameter("msg");
        }
        
        if (msg != null && !msg.isEmpty()) {
            ArrayList<String> params = new ArrayList<String>();
            int p = 1;
            
            while (requestParams.getRequest().getParameter("p" + p) != null) {
                params.add(RenderingHelper.escapeHTML(requestParams.getRequest().getParameter("p" + p)));
                p++;
            }
            
            String backgroundColor = "#eeeeff";
            if (requestParams.getRequest().getParameter("errmsg") != null &&
                    !requestParams.getRequest().getParameter("errmsg").isEmpty()) {
                backgroundColor = "#ffeeee";
            }
            
            content += "<div style=\"background-color: " + backgroundColor + "\"> ";
            try {
                content += String.format(ConfigService.stringMessages.get("msg_" + msg), params.toArray());
            }
            catch (MissingFormatArgumentException e) {
                content += "msg_" + msg;
            }
            content += "</div>";
        }
        
        if (requestParams.getRequest().getParameter("search") != null &&
                !requestParams.getRequest().getParameter("search").isEmpty()) {
            
            content += SearchService.getSearchResult(requestParams, 
                    requestParams.getRequest().getParameter("search"), fn, ru);
        }
        else {
            
            if (!FileOperationsService.is_file_writable(requestParams, fn)) {
                content += "<div style=\"background-color:#ffeeee\">";
                content += ConfigService.stringMessages.get("foldernotwriteable");
                content += "</div>";
            }

            if (!FileOperationsService.is_file_readable(requestParams, fn)) {
                content += "<div style=\"background-color:#ffeeee\">";
                content += ConfigService.stringMessages.get("foldernotreadable");
                content += "</div>";
            }
            
            String list = "";
            int count = 0;
            
            Object[] folderList = DirectoryOperationsService.getFolderList(requestParams, fn, ru);
            
            if (folderList.length >= 2) {
                list = (String)folderList[0];
                count = ((Integer)folderList[1]).intValue();
            }
            
            content += list;
            
            if (ConfigService.ALLOW_FILE_MANAGEMENT && FileOperationsService.is_file_writable(requestParams, fn)) {
                
                content += "<hr>";
                content += "&bull; " + ConfigService.stringMessages.get("createfoldertext");
                content += "<input name=\"colname\" size=\"30\">";
                content += "<input type=\"submit\" name=\"mkcol\" value=\"" + ConfigService.stringMessages.get("createfolderbutton") + "\">";
                		
                if (count > 0) {
                    content += "<br>" + "&bull; " + ConfigService.stringMessages.get("movefilestext");
                    content += "<input name=\"newname\" size=\"30\">";
                    content += "<input type=\"submit\" " +
                    		" name=\"rename\" " +
                    		" value=\"" + ConfigService.stringMessages.get("movefilesbutton") + "\"" +
                    		" onclick=\"return window.confirm('" + ConfigService.stringMessages.get("movefilesconfirm") + "');\">";
                    content += "<br>" + "&bull; ";
                    content += "<input type=\"submit\" " +
                        " name=\"delete\" " +
                        " value=\"" + ConfigService.stringMessages.get("deletefilesbutton") + "\"" +
                        " onclick=\"return window.confirm('" + ConfigService.stringMessages.get("deletefilesconfirm") + "');\">";
                    content += ConfigService.stringMessages.get("deletefilestext");
                }
                
                if (ConfigService.ALLOW_CHANGEPERM) {
                    content += "<hr>";
                    content += "&bull; ";
                    content += ConfigService.stringMessages.get("changefilepermissions");
                    
                    HashMap<String, String> labels = new HashMap<String, String>();
                    labels.put("r", ConfigService.stringMessages.get("readable"));
                    labels.put("w", ConfigService.stringMessages.get("writeable"));
                    labels.put("x", ConfigService.stringMessages.get("executable"));
                    labels.put("t", ConfigService.stringMessages.get("sticky"));
                    
                    HashMap<String, String> labelsGroup = new HashMap<String, String>(labels); 
                    labels.put("s", ConfigService.stringMessages.get("setuid"));
                    labelsGroup.put("s", ConfigService.stringMessages.get("setgid"));
                    
                    if (ConfigService.PERM_USER != null) {
                        content += ConfigService.stringMessages.get("user");
                        Iterator<String> it = ConfigService.PERM_USER.iterator();
                        while (it.hasNext()) {
                            String value = it.next();
                            String label = labels.get(value);
                            content += "<input type=\"checkbox\" name=\"fp_user\" value=\"" + value + "\">" + label + "";
                        }
                    }
                        
                    if (ConfigService.PERM_GROUP != null) {
                        content += ConfigService.stringMessages.get("group");
                        Iterator<String> it = ConfigService.PERM_GROUP.iterator();
                        while (it.hasNext()) {
                            String value = it.next();
                            String label = labelsGroup.get(value);
                            content += "<input type=\"checkbox\" name=\"fp_group\" value=\"" + value + "\">" + label + "";
                        }
                    }
                        
                    if (ConfigService.PERM_OTHERS != null) {
                        content += ConfigService.stringMessages.get("others");
                        Iterator<String> it = ConfigService.PERM_OTHERS.iterator();
                        while (it.hasNext()) {
                            String value = it.next();
                            String label = labels.get(value);
                            content += "<input type=\"checkbox\" name=\"fp_others\" value=\"" + value + "\">" + label + "";
                        }
                    }
                    
                    content += "; ";
                    
                    content += "<select name=\"fp_type\">";
                    ArrayList<String> fpType = new ArrayList<String>(Arrays.asList("a", "s", "r"));
                    HashMap<String, String> fpTypeLabels = new HashMap<String, String>();
                    fpTypeLabels.put("a", ConfigService.stringMessages.get("add"));
                    fpTypeLabels.put("s", ConfigService.stringMessages.get("set"));
                    fpTypeLabels.put("r", ConfigService.stringMessages.get("remove"));
                    Iterator<String> li = fpType.iterator();
                    while (li.hasNext()) {
                        String value = li.next();
                        String label = fpTypeLabels.get(value);
                        content += "<option value=\"" + value + "\">" + label + "</option>";
                    }
                    content += "</select>";
                    
                    if (ConfigService.ALLOW_CHANGEPERMRECURSIVE) {
                        content += "; ";
                        content += "<input type=\"checkbox\" name=\"fp_recursive\" value=\"recursive\">";
                        content += ConfigService.stringMessages.get("recursive");
                    }
                    
                    content += "; ";
                    content += "<input type=\"submit\" name=\"changeperm\" value=\"" + ConfigService.stringMessages.get("changepermissions") + "\" " +
                    		" onclick=\"return window.confirm('" + ConfigService.stringMessages.get("changepermconfirm") + "');\">";
                    
                    content += "<br>";
                    content += "&nbsp;&nbsp;";
                    content += ConfigService.stringMessages.get("changepermlegend");
                }
                
                if (ConfigService.ALLOW_ZIP_UPLOAD || ConfigService.ALLOW_ZIP_DOWNLOAD) {
                    content += "<hr>";
                    if (ConfigService.ALLOW_ZIP_DOWNLOAD && count > 0) {
                        content += "&bull; ";
                        content += "<input type=\"submit\" " +
                        		" name=\"zip\"" +
                        		" value=\"" + ConfigService.stringMessages.get("zipdownloadbutton") + "\">";
                        content += ConfigService.stringMessages.get("zipdownloadtext");
                        content += "<br>";
                    }
                    
                    if (ConfigService.ALLOW_ZIP_UPLOAD) {
                        content += "&bull; ";
                        content += ConfigService.stringMessages.get("zipuploadtext");
                        content += "<input type=\"file\" " +
                                        " name=\"zipfile_upload\"" +
                                        " multiple=\"multiple\">";
                        content += "<input type=\"submit\" " +
                                        " name=\"uncompress\"" +
                                        " value=\"" + ConfigService.stringMessages.get("zipuploadbutton") + "\" " +
                                   		" onclick=\"return window.confirm('" + ConfigService.stringMessages.get("zipuploadconfirm") + "');\">";
                    }
                }
            }
            
            if (ConfigService.ALLOW_FILE_MANAGEMENT) {
                content += "</form>";
            }
            
            if (ConfigService.ALLOW_POST_UPLOADS && FileOperationsService.is_file_writable(requestParams, fn)) {
                
                content += "<hr>";
                content += "<form " +
                		" enctype=\"multipart/form-data\"" +
                		" method=\"post\"" +
                		" onsubmit=\"return window.confirm('" + ConfigService.stringMessages.get("confirm") + "');\">";
                content += "<input type=\"hidden\" name=\"upload\" value=\"1\" >";
                content += "<span id=\"file_upload\">&bull; " + ConfigService.stringMessages.get("fileuploadtext");
                content += "<input type=\"file\" name=\"file_upload\" multiple=\"multiple\">";
                content += "</span>";

                content += "<span id=\"moreuploads\"></span>";
                content += "<input type=\"submit\" " +
                                " name=\"filesubmit\"" +
                                " value=\"" + ConfigService.stringMessages.get("fileuploadbutton") + "\" " +
                                " onclick=\"return window.confirm('" + ConfigService.stringMessages.get("fileuploadconfirm") + "');\">";
                content += " ";
                content += "<a href=\"#\"" +
                		" onclick=\"javascript:document.getElementById('moreuploads').innerHTML=document.getElementById('moreuploads').innerHTML+'<br/>'+document.getElementById('file_upload').innerHTML\">" + ConfigService.stringMessages.get("fileuploadmore") + "</a>";
                content += " (" + ConfigService.POST_MAX_SIZE / 1048576 + " MB max)";
                content += "</form>";
            }
        }
        
        if (ConfigService.SIGNATURE != null) {
            content += "<hr>" + ConfigService.SIGNATURE;
        }
        
        content += RenderingService.end_html();
        
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("Cache-Control", "no-cache, no-store");
        OutputService.printHeaderAndContent(requestParams, "200 OK", 
                "text/html", content, params);
        
    }
  
    private void doFilePropertiesRequest(RequestParams requestParams, String fn) {
        
        String content = "";
        content += RenderingService.start_html(requestParams.getRequestURI() + " properties");
        if (ConfigService.HEADER != null) {
            content += String.format(ConfigService.HEADER, requestParams.getUsername());
        }

        // PZ: original perl code
        // my $fullparent = dirname($REQUEST_URI) .'/';
        
        String ru = requestParams.getRequestURI();
        String[] sf = FileOperationsService.splitFilename(ru);
        
        String fullparent = sf[0];
        String basename = sf[1];
        
        if (fullparent == null || fullparent.isEmpty() || fullparent == "//") {
            fullparent = "/";
        }
        
        content += "<h1>";
        if (FileOperationsService.is_directory(requestParams, fn)) {
            content += RenderingService.getQuickNavPath(ru, QueryService.getQueryParams(requestParams));
        }
        else {
            content += RenderingService.getQuickNavPath(fullparent, QueryService.getQueryParams(requestParams));
            content += " ";
            content += "<a href=\"" + ru + "\">" + basename + "</a>";
            content += ConfigService.stringMessages.get("properties");
        }
        content += "</h1>";
        

        if (MIMETypesHelper.getMIMEType(fn).startsWith("image/")) {
            content += "<a href=\"" + ru + "\" title=\"" + ConfigService.stringMessages.get("clickforfullsize") + "\">";
            String thumbnailPath = "";
            int imageWidth = 200;
            if (ConfigService.ENABLE_THUMBNAIL) {
                thumbnailPath = "?action=thumb";
                imageWidth = ConfigService.THUMBNAIL_WIDTH;
            }
            content += "<img src=\"" + ru + thumbnailPath + "\" alt=\"image\" style=\"border: 0; width: " + String.valueOf(imageWidth) + "\">";
            content += "</a>";
        }
        
        content += "<table style=\"width: 100%; table-layout:fixed;\">";
        

        // original code: local(%NAMESPACEELEMENTS);
        HashMap<String, Integer> namespaceElements = new HashMap<String, Integer>();
        
        FileProperties dbprops = ConfigService.properties.getProperties(fn);
        ArrayList<String> bgcolors = new ArrayList<String>(Arrays.asList("#ffffff", "#eeeeee"));
        ArrayList<String> visited = new ArrayList<String>();
        String bgcolor = "";
        
        content += "<tr style=\"background-color:#dddddd;text-align:left\">";
        content += "<th style=\"width:25%\">" + ConfigService.stringMessages.get("propertyname") + "</th>";
        content += "<th style=\"width:75%\">" + ConfigService.stringMessages.get("propertyvalue") + "</th>";
        content += "</tr>";
        
        ArrayList<String> allprops = new ArrayList<String>();
        
        if (dbprops != null) {
            allprops.addAll(dbprops.getKeys());
        }
        allprops.addAll(ConfigService.KNOWN_FILE_PROPS);
        Collections.sort(allprops, new PropertyComparator());
        
        Iterator<String> it = allprops.iterator();
        while (it.hasNext()) {
            String prop = it.next();
            
            StatusResponse r200 = new StatusResponse();
            
            if (visited.contains(prop) || visited.contains("{" + NamespaceService.getNameSpaceUri(prop) + "}" + prop)) {
                continue;
            }
            
            // was if exists $visited{$prop}
            if (dbprops != null && dbprops.getProperty(prop) != null) {
                r200.putProp(prop, dbprops.getProperty(prop));
            }
            else {
                PropertiesActions.getProperty(requestParams, fn, requestParams.getRequestURI(), 
                        prop, null, r200, null);
            }
            
            visited.add(prop);
            
            namespaceElements.put(NamespaceService.nonamespace(prop), 1);
            
            String title = XMLService.createXML(namespaceElements, r200.getProps(), true);
            String value = XMLService.createXML(namespaceElements, r200.getProp(prop), true);
            String namespace = NamespaceService.getNameSpaceUri(prop);
            
            Pattern p = Pattern.compile("^\\{([^\\}]*)\\}", Pattern.DOTALL);
            Matcher m = p.matcher(prop);
            if (m.find()) {
                namespace = m.group(1);
            }
            
            // original perl code:
            // push @bgcolors, $bgcolor = shift @bgcolors;
            if (bgcolors.size() > 0) {
                bgcolor = bgcolors.get(0);
                bgcolors.remove(0);
                bgcolors.add(bgcolor);
            }
            
            content += "<tr style=\"background-color: + " + bgcolor + "; text-align:left\">";
            content += "<th title=\"" + namespace + "\" style=\"vertical-align:top;\">" + NamespaceService.nonamespace(prop) + "</th>";
            // note: HTMLEncode is requered here
            content += "<td title=\"" + RenderingHelper.escapeHTML(title) + "\" style=\"vertical-align:bottom;\">";
            content += "<pre style=\"margin:0px; overflow:auto;\">";
            content += RenderingHelper.escapeHTML(value);
            content += "</pre>";
            content += "</td>";
            content += "</tr>";
        }
        
        content += "</table>";
        if (ConfigService.SIGNATURE != null) {
            content += "<hr>" + ConfigService.SIGNATURE;
        }
        content += RenderingService.end_html();
        
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("Cache-Control", "no-cache, no-store");

        OutputService.printHeaderAndContent(requestParams, "200 OK", "text/html", content, params);
    }
    
    private void doFileExists(RequestParams requestParams, String fn) {
        Logger.debug("GET: DOWNLOAD");
        OutputService.printFileHeader(requestParams, fn);
        OutputService.printContentStream(requestParams, FileOperationsService.getFileContentStream(requestParams, fn));
    }
    
    public class PropertyComparator implements Comparator<String> {
        
        public int compare(String a, String b) {
            
            return NamespaceService.nonamespace(a.toLowerCase()).compareTo(NamespaceService.nonamespace(b.toLowerCase()));
        }
    }
    
}
