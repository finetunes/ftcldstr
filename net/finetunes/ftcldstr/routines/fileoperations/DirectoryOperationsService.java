package net.finetunes.ftcldstr.routines.fileoperations;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.helper.SystemCalls;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.rendering.RenderingService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService.StatData;
import net.finetunes.ftcldstr.routines.webdav.QueryService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesHelper;
import net.finetunes.ftcldstr.routines.webdav.properties.StatusResponse;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;

public class DirectoryOperationsService {
	
	public static void readDirBySuffix(RequestParams requestParams, String fn, String base, 
			ArrayList<String> hrefs, String suffix, 
			int depth, ArrayList<String> visited) {
	    
	    Logger.debug("readDirBySuffix(" + fn + ", ..., " + suffix + ", " + depth + ")");
	    
	    String nfn = FileOperationsService.full_resolve(fn);
	    
	    if (visited == null) {
	        visited = new ArrayList<String>();
	    }
	    
	    if (hrefs == null) {
	        hrefs = new ArrayList<String>();
	    }
	    
	    
	    // was "if exists $$visited{$nfn} && ($depth eq 'infinity' || $depth < 0)"
	    if (visited.contains(nfn) && (depth == Integer.MAX_VALUE || depth < 0)) {
	        return;
	    }
	    
	    visited.add(nfn);
	    
        List<String> files = WrappingUtilities.getFileList(requestParams, fn);
        if (files != null) {
    	    Iterator<String> it = files.iterator();
    	    while (it.hasNext()) {
    	        String sf = it.next();
    	        
    	        if (!sf.matches("(\\.|\\.\\.)")) {
    	            if (FileOperationsService.is_directory(fn + sf)) {
    	                sf += "/";
    	            }
    	            
    	            String nbase = base + sf;
    	            if (FileOperationsService.is_plain_file(fn + sf) && sf.matches(".*\\." + Pattern.quote(suffix) + ".*")) {
    	                hrefs.add(nbase);
    	            }
    	            
    	            if (depth != 0 && FileOperationsService.is_directory(fn + sf)) {
    	                readDirBySuffix(requestParams, fn + sf, nbase, hrefs, suffix, depth - 1, visited);
    	            }
    	            // ## add only files with requested components 
    	            // ## filter (comp-filter > comp-filter >)	            
    	        }
    	    }
        }
	}
	
    public static void readDirBySuffix(RequestParams requestParams, String fn, String base, 
            ArrayList<String> hrefs, String suffix, int depth) {
        readDirBySuffix(requestParams, fn, base, hrefs, suffix, depth, null);
    }
	
	public static Object[] getFolderList(RequestParams requestParams, String fn, String ru, String filter) {
	    
	    String content = "";
	    String list;
	    int count = 0;
	    int filecount = 0;
	    int foldercount = 0;
	    int filesizes = 0;
	    
        content += "<h2 style=\"border:0; padding:0; margin:0\">";
        content += "<a href=\"" + ru + "?action=props\">";
        
        String iconPath = ConfigService.ICONS.get("< folder >");
        if (iconPath == null || iconPath.isEmpty()) {
            iconPath = ConfigService.ICONS.get("default");
        }
        
        content += "<img src=\"" + iconPath + "\" style=\"border:0\" title=\"" + ConfigService.stringMessages.get("showproperties") + "\" alt=\"folder\">";
        content += "</a>";
        content += "&nbsp;";
        content += "<a href=\"?action=davmount\" style=\"font-size:0.8em;color:black\" title=\"" + ConfigService.stringMessages.get("mounttooltip") + "\">";
        content += ConfigService.stringMessages.get("mount");
        content += "</a>";
        content += " ";
        content += RenderingService.getQuickNavPath(ru, QueryService.getQueryParams(requestParams));
        content += "</h2>";
        
        if (ConfigService.SHOW_QUOTA) {
            
            Integer ql;
            Integer qu;
            Object[] quota = FileHelper.getQuota();
            ql = (Integer)quota[0];
            qu = (Integer)quota[1];
            
            if (ql != null && qu != null) {
                ql = ql / 1048576;
                qu = qu / 1048576;
                content += "<div style=\"padding-left:30px;font-size:0.8em;\">";
                content += ConfigService.stringMessages.get("quotalimit") + ql.intValue() + " MB,";
                content += ConfigService.stringMessages.get("quotaused") + qu.intValue() + " MB,";
                content += ConfigService.stringMessages.get("quotaavailable") + (ql.intValue() - qu.intValue()) + " MB,";
                content += "</div>";
            }
            
        }
        
        list = "\n";
        
        if (ConfigService.ALLOW_FILE_MANAGEMENT) {
            list += "<input type=\"checkbox\"" +
            		    " onclick=\"javascript:this.checked=false; var ea = document.getElementsByName('file'); for (var i=0; i<ea.length; i++) ea[i].checked=!ea[i].checked;\"" +
            		    " name=\"selectall\" value=\"\"" +
            		    " title=\"" + ConfigService.stringMessages.get("togglealltooltip") + "\" >";
        }
        
        String order = requestParams.getRequest().getParameter("order");
        if (order == null || order.isEmpty()) {
            order = "name";
        }
        
        String dir = "_desc";
        if (order.endsWith("_desc")) {
            dir = "";
        }
        
        String query = "";
        String showAllParam = requestParams.getRequest().getParameter("showall");
        if (showAllParam != null && !showAllParam.isEmpty()) {
            query = "showall=" + showAllParam;
        }

        char[] spacesName = new char[ConfigService.MAXFILENAMESIZE - ConfigService.stringMessages.get("names").length() + 1];
        char[] spacesMod = new char[ConfigService.MAXLASTMODIFIEDSIZE - ConfigService.stringMessages.get("lastmodified").length() + 1];
        char[] spacesSize = new char[ConfigService.MAXSIZESIZE - ConfigService.stringMessages.get("size").length()];
        Arrays.fill(spacesName, ' ');
        Arrays.fill(spacesMod, ' ');
        Arrays.fill(spacesSize, ' ');
        
        list += "<span style=\"font-weight:bold;padding-left:20px\">";
        list += "<a href=\"" + ru + "?order=name" + dir + ConfigService.URL_PARAM_SEPARATOR + query + "\" style=\"color:black\">" + ConfigService.stringMessages.get("names") + "</a>";
        list += new String(spacesName);
        list += "<a href=\"" + ru + "?order=lastmodified" + dir + ConfigService.URL_PARAM_SEPARATOR + query + "\" style=\"color:black\">" + ConfigService.stringMessages.get("lastmodified") + "</a>";
        list += new String(spacesMod);
        list += new String(spacesSize);
        list += "<a href=\"" + ru + "?order=size" + dir + ConfigService.URL_PARAM_SEPARATOR + query + "\" style=\"color:black\">" + ConfigService.stringMessages.get("size") + "</a>";
        
        if (ConfigService.SHOW_PERM) {
            list += " ";
            list += "<a href=\"" + ru + "?order=mode" + dir + ConfigService.URL_PARAM_SEPARATOR + query + "\" style=\"color:black\">";
            list += String.format("%-11s", ConfigService.stringMessages.get("permissions")); 
            list += "</a>";
        }
        
        list += " ";
        list += "<a href=\"" + ru + "?order=mime" + dir + ConfigService.URL_PARAM_SEPARATOR + query + "\" style=\"color:black\">" + ConfigService.stringMessages.get("mimetype") + "</a>";
        list += "\n";
        list += "</span>";
        
	    if (!(fn.equals(ConfigService.DOCUMENT_ROOT) || ru.equals("/") || (filter != null && !filter.isEmpty()))) {
            list += "<input type=\"checkbox\" name=\"hidden\" value=\"\" disabled=\"disabled\" style=\"visibility:hidden\">";
            list += FileHelper.getfancyfilename(requestParams, FileOperationsService.splitFilename(ru)[0] + "/", "..", "< .. >", FileOperationsService.splitFilename(fn)[0]) + "\n";
	    }
	    
        List<String> files = WrappingUtilities.getFileList(requestParams, fn);
        if (files == null) {
            files = new ArrayList<String>();
        }
        Collections.sort(files, new FilenameComparator(requestParams.getPathTranslated(), order));
        
	    String pagenum = requestParams.getRequest().getParameter("page");
	    int page = 0;
	    try {
	        page = Integer.valueOf(pagenum).intValue() - 1;
	    }
	    catch (NumberFormatException e) {
	        // do nothing
	    }
	    catch (NullPointerException e) {
	        // do nothing
	    }
	    
	    int fullcount = files.size();
	    if ((filter == null || filter.isEmpty()) && ConfigService.PAGE_LIMIT > 0 && (showAllParam == null || showAllParam.isEmpty())) {
	        int startIndex = 0;
	        int endIndex = ConfigService.PAGE_LIMIT * (page + 1);
	        if (page > 0) {
	            startIndex = ConfigService.PAGE_LIMIT * page;
	        }
	        
	        if (files.size() < endIndex) {
	            endIndex = files.size();
	        }
	        
	        files = files.subList(startIndex, endIndex);
	    }

	    // original perl code:
	    // eval qq@/$filter/;@;
	    // $filter="\Q$filter\E" if ($@);
	    
	    if (filter != null && !filter.isEmpty()) {
    	    try {
    	        Pattern pattern = Pattern.compile(filter);
    	    }
    	    catch (PatternSyntaxException e) {
    	        filter = Pattern.quote(filter);
    	    }
	    }
	    
        Iterator<String> it = files.iterator();
        while (it.hasNext()) {
            String filename = it.next();
            String full = fn + filename;
            
            if (FileOperationsService.is_hidden(full)) {
                continue;
            }
            
            String mimetype;
            if (FileOperationsService.is_directory(full)) {
                mimetype = "< folder >";
            }
            else {
                mimetype = MIMETypesHelper.getMIMEType(filename);
            }
            
            String nru = "";
            nru = ru + RenderingHelper.HTMLEncode(filename);
            
            if (FileOperationsService.is_directory(full)) {
                filename += "/";
                nru += "/";
            }
            
            if (filter != null && !filter.isEmpty() && !filename.matches("(?i).*" + filter + ".*")) {
                continue;
            }
            
            StatData stat = FileOperationsService.stat(full);
            int mode = stat.getMode(); 

            int uid = stat.getUid(); 
            int gid = stat.getGid(); 
            int size = stat.getSize(); 
            Date mtime = stat.getMtimeDate(); 

            if (ConfigService.ALLOW_FILE_MANAGEMENT) {
                list += "<input type=\"checkbox\" name=\"file\" value=\"" + filename + "\">";
            }

            DateFormat dateFormat = new SimpleDateFormat(ConfigService.stringMessages.get("lastmodifiedformat"));
            String lmf = dateFormat.format(mtime);
            
            list += FileHelper.getfancyfilename(requestParams, nru, filename, mimetype, full);
            list += String.format(" %-" + ConfigService.MAXLASTMODIFIEDSIZE + "s %" + ConfigService.MAXSIZESIZE + "d", lmf, size);
            if (ConfigService.SHOW_PERM) {
                list += "<span " +
                      " style=\"" + RenderingHelper.getmodecolors(full, mode) + "\"" +
                      " title=\"" + String.format("mode: %04o, uid: %s (%s), gid: %s (%s)", mode & 07777, SystemCalls.getpwuid(uid), uid, SystemCalls.getgrgid(gid), gid) + "\">";
                list += String.format("%-11s", RenderingHelper.mode2str(full, mode));
                list += "</span>";
            }
            list += " " + RenderingHelper.HTMLEncode(mimetype);
            list += "\n";
            
            count++;
            if (FileOperationsService.is_directory(full)) {
                foldercount++;
            }
            
            if (FileOperationsService.is_plain_file(full)) {
                filecount++;
                filesizes += size;
            }
        }

        String pagenav = "";
        if (filter == null || filter.isEmpty()) {
            pagenav = RenderingService.getPageNavBar(requestParams, ru, fullcount);
        }
        
        if (ConfigService.ALLOW_FILE_MANAGEMENT) {
            content += "<form " +
                            " enctype=\"multipart/form-data\"" +
                            " method=\"post\"" +
                            " onsubmit=\"return window.confirm('" + ConfigService.stringMessages.get("confirm") + "');\">";
        }
        
        content += pagenav;
        content += "<pre style=\"overflow:auto;\">" + list + "</pre>";
        
        if (ConfigService.SHOW_STAT) {
            content += "<div style=\"font-size:0.8em\">";
            content += String.format("%s %d, %s %d, %s %d, %s %d Bytes (= %.2f KB = %.2f MB = %.2f GB)", 
                    ConfigService.stringMessages.get("statfiles"), filecount,
                    ConfigService.stringMessages.get("statfolders"), foldercount,
                    ConfigService.stringMessages.get("statsum"), count,
                    ConfigService.stringMessages.get("statsize"), filesizes, ((float)filesizes)/1024, ((float)filesizes)/1048576, ((float)filesizes)/1073741824);
            content += "</div>";
        }
        
        content += pagenav;
        return new Object[] {content, count};
	}
	
    public static Object[] getFolderList(RequestParams requestParams, String fn, String ru) {
     
        return getFolderList(requestParams, fn, ru, null);
    }	
	
    // PZ: original perl code cached values and returned them
    // later from cache
    // though such an approach seems not to take into account
    // the situation when the directory contents is changed;
    // implemented without the cache here
	public static int getDirInfo(RequestParams requestParams, String fn, String prop) {
	    
        int childcount = 0;
        int visiblecount = 0;
        int objectcount = 0;
        int hassubs = 0;
        int realchildcount = 0;
	    
        List<String> files = WrappingUtilities.getFileList(requestParams, fn);
        if (files != null) {
            Iterator<String> it = files.iterator();
            while (it.hasNext()) {
                String f = it.next();
                
                if (!f.matches("(\\.|\\.\\.)")) {
                    realchildcount++;
                    if (!FileOperationsService.is_hidden(fn + "/" + f)) {
                        childcount++;
                        if (!FileOperationsService.is_directory(fn + "/" + f) && !f.startsWith(".")) {
                            visiblecount++;
                        }
                        
                        if (!FileOperationsService.is_directory(fn + "/" + f)) {
                            objectcount++;
                        }
                    }
                }
            }	
        }
        if (childcount - objectcount > 0) {
            hassubs = 1;
        }
        
        HashMap<String, Integer> counter = new HashMap<String, Integer>();
        counter.put("childcount", childcount);
        counter.put("visiblecount", visiblecount);
        counter.put("objectcount", objectcount);
        counter.put("hassubs", hassubs);
        counter.put("realchildcount", realchildcount);
        
        return counter.get(prop);
	}
	
	public static void readDirRecursive(RequestParams requestParams,
	        String fn, String ru,
	        ArrayList<StatusResponse> respsRef,
	        ArrayList<String> props,
	        boolean all, boolean noval, int depth,
	        boolean noroot, ArrayList<String> visited) {
	    
	    if (FileOperationsService.is_hidden(fn)) {
	        return;
	    }
	    
	    if (visited == null) {
	        visited = new ArrayList<String>();
	    }
	    
	    String nfn = FileOperationsService.full_resolve(fn);
	    if (!noroot) {
	        StatusResponse response = new StatusResponse();
	        response.setHref(ru);
	        
	        // original perl code is dealing with two returned objects of StatusResponse
	        // here, but it seems there is no need in the second object
	        
	        ArrayList<StatusResponse> r = PropertiesHelper.getPropStat(requestParams, nfn, ru, props, all, noval);
	        if (r.size() == 0) {
	            response.setStatus("HTTP/1.1 200 OK");
	            response.setPropstat(null);
	        }
	        else {
	            if (ConfigService.ENABLE_BIND && depth < 0 && visited.contains(nfn)) {
	                response.setPropstatStatus("HTTP/1.1 208 Already Reported");
	            }
	        }
	        
	        respsRef.add(response);
	    }
	    
	    if (visited.contains(nfn) && !noroot && (depth == Integer.MAX_VALUE || depth < 0)) {
	        return;
	    }
	    
	    visited.add(nfn);
	    if (depth != 0 && FileOperationsService.is_directory(nfn)) {
	        List<String> files = WrappingUtilities.getFileList(requestParams, nfn);
	        if (files != null) {
    	        String order = requestParams.getRequest().getParameter("order");
    	        if (order == null || order.isEmpty()) {
    	            order = "name";
    	        }
    	        
    	        Collections.sort(files, new FilenameComparator(requestParams.getPathTranslated(), order));
                Iterator<String> it = files.iterator();
                
                while (it.hasNext()) {
                    String f = it.next();
                    if (FileOperationsService.is_hidden(nfn + "/" + f)) {
                        continue;
                    }
                    
                    String fru = ru + RenderingHelper.uri_escape(f);
                    if (FileOperationsService.is_directory(nfn + "/" + f) && !fru.endsWith("/")) {
                        fru += "/";
                    }
                    
                    String nnfn = FileOperationsService.full_resolve(nfn + "/" + f);
                    int nd = depth; 
                    if (depth > 0) {
                        nd = depth - 1;
                    }
                    readDirRecursive(requestParams, nnfn, fru, respsRef, props, all, noval, nd, false, visited);
                }
	        }
	    }
	}
	
    public static void readDirRecursive(RequestParams requestParams,
            String fn, String ru,
            ArrayList<StatusResponse> respsRef,
            ArrayList<String> props,
            boolean all, boolean noval, int depth,
            boolean noroot) {
        
        readDirRecursive(requestParams, fn, ru, respsRef, props, all, noval, depth, noroot, null);
    }
}
