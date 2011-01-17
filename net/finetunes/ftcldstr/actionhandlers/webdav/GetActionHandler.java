package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.MissingFormatArgumentException;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.rendering.RenderingService;
import net.finetunes.ftcldstr.routines.fileoperations.DirectoryOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.QueryService;
import net.finetunes.ftcldstr.routines.webdav.SearchService;
import net.finetunes.ftcldstr.serialization.DBPropertiesOperations;
import net.finetunes.ftcldstr.wrappers.FileOperationsWrapper;

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
        else if (FileOperationsService.is_directory(fn) && !ConfigService.FANCYINDEXING) {
            OutputService.printHeaderAndContent(requestParams, "404 Not Found", "text/plain", "404 - NOT FOUND");
        }
        else if (FileOperationsService.file_exits(fn) && 
                requestParams.getRequest().getParameter("action") != null && requestParams.getRequest().getParameter("action").equals("davmount")) {
            doDavmountRequest(requestParams, fn);
        }
        else if (ConfigService.ENABLE_THUMBNAIL && FileOperationsService.is_plain_file(fn) &&
                FileOperationsService.is_file_readable(fn) &&
                requestParams.getRequest().getParameter("action") != null && requestParams.getRequest().getParameter("action").equals("thumb")) {
            doThumbnailRequest(fn);
        }
        else if (FileOperationsService.file_exits(fn) &&
                requestParams.getRequest().getParameter("action") != null && requestParams.getRequest().getParameter("action").equals("props")) {
            doFilePropertiesRequest(requestParams, fn);
        }
        else if (FileOperationsService.is_directory(fn)) {
            doFileIsDirectory(requestParams, fn);
        }
        else if (FileOperationsService.file_exits(fn) &&
                !FileOperationsService.is_file_readable(fn)) {
            OutputService.printHeaderAndContent(requestParams, "403 Forbidden", "text/plain", "403 Forbidden");
        }
        else if (FileOperationsService.file_exits(fn)) {
            doFileExists(fn);
        }
        else {
            Logger.debug("GET: " + fn + " NOT FOUND!");
            OutputService.printHeaderAndContent(requestParams, "404 Not Found", "text/plain", "404 - FILE NOT FOUND");
        }
    }
    
    // TODO: check
    private void doDavmountRequest(RequestParams requestParams, String fn) {
        
        // my $su = $ENV{REDIRECT_SCRIPT_URI} || $ENV{SCRIPT_URI};
        String su = requestParams.getScriptURI();
        String bn = FileOperationsService.splitFilename(fn)[1];
        
        su = su.replaceFirst(Pattern.quote(bn) + "/?", ""); // $su =~ s/\Q$bn\E\/?//;
        if (FileOperationsService.is_directory(fn) && !bn.endsWith("/")) {
            bn += "/";
        }
        
        OutputService.printHeaderAndContent(requestParams, "200 OK", "application/davmount+xml", 
                "<dm:mount xmlns:dm=\"http://purl.org/NET/webdav/mount\"><dm:url>" + su +  "</dm:url><dm:open>" + bn + "</dm:open></dm:mount>");
    }
    
    // TODO
    private void doThumbnailRequest(String fn) {
        
//      my $image = Graphics::Magick->new;
        
        int width = 22;
        if (ConfigService.THUMBNAIL_WIDTH > 0) {
            width = ConfigService.THUMBNAIL_WIDTH;
        }
        else if (ConfigService.ICON_WIDTH > 0) {
            width = ConfigService.ICON_WIDTH;
        }
        
        if (ConfigService.ENABLE_THUMBNAIL_CACHE) {
            String uniqname = fn;
            uniqname = uniqname.replaceAll("/", "_");
            
            String cachefile = ConfigService.THUMBNAIL_CACHEDIR + "/" + uniqname + ".thumb";
            if (!FileOperationsService.file_exits(ConfigService.THUMBNAIL_CACHEDIR)) {
                // create directory using wrapper (THUMBNAIL_CACHEDIR) // TODO
            }
            
            if (!FileOperationsService.file_exits(cachefile) ||
                    FileOperationsWrapper.getFileModificationDate(fn).after(FileOperationsWrapper.getFileModificationDate(cachefile))) {
//              $image->Read($fn);
//              $image->Resize(geometry=>$width,filter=>'Gaussian');
//              $image->Write($cachefile);
            }
//          if (open(my $cf, "<$cachefile")) {
//              print $cgi->header(-status=>'200 OK',-type=>getMIMEType($fn), -ETag=>getETag($cachefile), -Content-length=>(stat($cachefile))[7]);
//              binmode $cf;
//              binmode STDOUT;
//              print while(<$cf>);
//              close($cf);
//          }
        }

//      } else {
//          print $cgi->header(-status=>'200 OK',-type=>getMIMEType($fn), -ETag=>getETag($fn));
//          $image->Read($fn);
//          $image->Resize(geometry=>$width,filter=>'Gaussian');
//          binmode STDOUT;
//          $image->Write('-');
//      }            
        
    }
    
    private void doFileIsDirectory(RequestParams requestParams, String fn) {
        
        String ru = requestParams.getRequestURI();
        String content = "";
        Logger.debug("GET: directory listing of " + fn);
        
        content += RenderingService.start_html(ru);
        if (ConfigService.HEADER != null) {
            content += ConfigService.HEADER;
        }
        
        if (ConfigService.ALLOW_SEARCH && FileOperationsService.is_file_readable(fn)) {
            String search = requestParams.getRequest().getParameter("search");
            
            String form = "";
            form += "<form method=\"get\">"; // PZ: TODO: form action missing?

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
                params.add(RenderingHelper.HTMLEncode(requestParams.getRequest().getParameter("p" + p)));
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
            
            content += SearchService.getSearchResult(requestParams.getRequest().getParameter("search"), 
                    fn, ru);
        }
        else {
            
            if (!FileOperationsService.is_file_writable(fn)) {
                content += "<div style=\"background-color:#ffeeee\">";
                content += ConfigService.stringMessages.get("foldernotwriteable");
                content += "</div>";
            }

            if (!FileOperationsService.is_file_readable(fn)) {
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
            
            if (ConfigService.ALLOW_FILE_MANAGEMENT && FileOperationsService.is_file_writable(fn)) {
                
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
                    content += "<input type=\"submit\" name=\"changeperm\" value=\"changepermissions\" " +
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
            
            if (ConfigService.ALLOW_POST_UPLOADS && FileOperationsService.is_file_writable(fn)) {
                
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
        
        if (ConfigService.SIGNATURE != null && !ConfigService.SIGNATURE.isEmpty()) {
            content += "<hr>" + ConfigService.SIGNATURE;
        }
        
        content += RenderingService.end_html();
        
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("Cache-Control", "no-cache, no-store");
        OutputService.printHeaderAndContent(requestParams, "200 OK", 
                "text/html", content, params);
        
    }
  
    // TODO
    private void doFilePropertiesRequest(RequestParams requestParams, String fn) {
        
        String content = "";
        content += RenderingService.start_html(requestParams.getRequestURI() + " properties");
        if (ConfigService.HEADER != null && !ConfigService.HEADER.isEmpty()) {
            content += ConfigService.HEADER;
        }

        // PZ: original perl code
        // my $fullparent = dirname($REQUEST_URI) .'/';
        // PZ: a strange trick, since dirname() may work wrong for some names
        
        String ru = requestParams.getRequestURI();
        String[] sf = FileOperationsService.splitFilename(ru);
        
        String fullparent = sf[0];
        String basename = sf[1];
        
        if (fullparent == null || fullparent.isEmpty() || fullparent == "//") {
            fullparent = "/";
        }
        
        content += "<h1>";
        if (FileOperationsService.is_directory(fn)) {
            content += RenderingService.getQuickNavPath(ru, QueryService.getQueryParams());
        }
        else {
            content += RenderingService.getQuickNavPath(fullparent, QueryService.getQueryParams());
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
        
/*
 * TODO: finish when properties are implemented        
        HashMap<String, String> namespaceElements = new HashMap<String, String>(ConfigService.NAMESPACEELEMENTS);
        List dbprops = DBPropertiesOperations.db_getProperties(fn);
        ArrayList<String> bgcolors = new ArrayList<String>(Arrays.asList("#ffffff", "#eeeeee"));
        HashMap<?> visited = new HashMap<?>();
        String bgcolor;
        
        my $dbprops = db_getProperties($fn);
        my @bgcolors = ( '#ffffff', '#eeeeee' );
        my (%visited);
        my $bgcolor;
*/
  
        content += "<tr style=\"background-color:#dddddd;text-align:left\">";
        content += "<th style=\"width:25%\">" + ConfigService.stringMessages.get("propertyname") + "</th>";
        content += "<th style=\"width:75%\">" + ConfigService.stringMessages.get("propertyvalue") + "</th>";
        content += "</tr>";

/*
 * TODO
 *         
        foreach my $prop (sort {nonamespace(lc($a)) cmp nonamespace(lc($b)) } keys %{$dbprops},-d $fn ? @KNOWN_COLL_PROPS : @KNOWN_FILE_PROPS ) {
            my (%r200);
            next if exists $visited{$prop} || exists $visited{'{'.getNameSpaceUri($prop).'}'.$prop};
            if (exists $$dbprops{$prop}) {
                $r200{prop}{$prop}=$$dbprops{$prop};
            } else {
                getProperty($fn, $REQUEST_URI, $prop, undef, \%r200, \my %r404);
            }
            $visited{$prop}=1;
            $NAMESPACEELEMENTS{nonamespace($prop)}=1;
            my $title = createXML($r200{prop},1);
            my $value = createXML($r200{prop}{$prop},1);
            my $namespace = getNameSpaceUri($prop);
            if ($prop =~ /^\{([^\}]*)\}/) {
                $namespace = $1;
            }
            push @bgcolors, $bgcolor = shift @bgcolors;
            $content.= $cgi->Tr( {-style=>"background-color:$bgcolor; text-align:left" },
                 $cgi->th({-title=>$namespace, -style=>'vertical-align:top;'},nonamespace($prop))
                .$cgi->td({-title=>$title, -style=>'vertical-align:bottom;' }, 
                        $cgi->pre({style=>'margin:0px; overflow:auto;'},$cgi->escapeHTML($value)))
                );
            
        }
*/
        
        content += "</table>";
        if (ConfigService.SIGNATURE != null && !ConfigService.SIGNATURE.isEmpty()) {
            content += "<hr>" + ConfigService.SIGNATURE;
        }
        content += RenderingService.end_html();
        
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("Cache-Control", "no-cache, no-store");

        OutputService.printHeaderAndContent(requestParams, "200 OK", "text/html", content, params);
    }
    

    // TODO
    private void doFileExists(String fn) {
        Logger.debug("GET: DOWNLOAD");
        OutputService.printFileHeader(fn);
//      if (open(F,"<$fn")) {
//          binmode(STDOUT);
//          while (read(F,my $buffer, $BUFSIZE)>0) {
//              print $buffer;
//          }
//          close(F);
//      }             
    }
    
}
