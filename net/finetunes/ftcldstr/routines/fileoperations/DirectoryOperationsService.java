package net.finetunes.ftcldstr.routines.fileoperations;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import net.finetunes.ftcldstr.routines.webdav.QueryService;
import net.finetunes.ftcldstr.wrappers.ReadDirectoryContentWrapper;
import net.finetunes.ftcldstr.wrappers.ReadDirectoryResult;

public class DirectoryOperationsService {
	
	// TODO: hrefs array of strings (filenames) recursively passed by reference
	public static void readDirBySuffix(String filename, 
			String baseName, 
			// $hrefs, 
			String suffix, 
			int depth, 
			HashMap visited) {
		
		// TODO: implement
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
        content += RenderingService.getQuickNavPath(ru, QueryService.getQueryParams());
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
	    
        List<String> files = new ArrayList<String>();
        ReadDirectoryContentWrapper rdw = new ReadDirectoryContentWrapper();
        ReadDirectoryResult d = rdw.readDirectory(fn);
        if (d.getExitCode() != 0) {
            Logger.log("Error reading directory content. Dir: " + fn + "; Error: " + d.getErrorMessage());
        }
        else {
            files = d.getContent();
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
            
            System.out.println("MIMETYPE: " + mimetype + " (" + filename + ")");
            
            
            String nru = "";
            try {
                nru = ru + URLEncoder.encode(filename, ConfigService.CHARSET);
            }
            catch (UnsupportedEncodingException e) {
                Logger.log("Exception: " + e.getMessage());
                nru = ru + filename;
            }
            
            if (FileOperationsService.is_directory(full)) {
                filename += "/";
                nru += "/";
            }
            
            if (filter != null && !filter.isEmpty() && !filename.matches("(?i).*" + filter + ".*")) {
                continue;
            }
            
            // TODO
            // my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks) = stat($full);
            Object[] stat = FileOperationsService.stat(full);
            String modestr = (String)stat[2];

            int mode = 0;
            try {
                mode = Integer.parseInt(modestr);
            }
            catch (NumberFormatException e) {
                // do nothing
            }
            
            int uid = ((Integer)stat[4]).intValue();
            int gid = ((Integer)stat[4]).intValue();
            int size = ((Integer)stat[7]).intValue();
            String mtime = ((String)stat[9]);

            if (ConfigService.ALLOW_FILE_MANAGEMENT) {
                list += "<input type=\"checkbox\" name=\"file\" value=\"" + filename + "\">";
            }

            // TODO: take string date from mtime
            // convert it to java.util.Date
            // use the value in dateFormat.format(date)
            //
            // current date to show something
            Date date = new Date();
            
            DateFormat dateFormat = new SimpleDateFormat(ConfigService.stringMessages.get("lastmodifiedformat"));
            String lmf = dateFormat.format(date) + " --TODO";
            
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
                    ConfigService.stringMessages.get("confstatsizeirm"), filesizes, ((float)filesizes)/1024, ((float)filesizes)/1048576, ((float)filesizes)/1073741824);
            content += "</div>";
        }
        
        content += pagenav;
        return new Object[] {content, count};
	}
	
    public static Object[] getFolderList(RequestParams requestParams, String fn, String ru) {
     
        return getFolderList(requestParams, fn, ru, null);
    }	
	
	public static int getDirInfo(String filename, String propertyName) {
		
		// TODO: implement
		return -1;
		
	}
	
	// TODO: params:
	// $fn, $ru, $respsRef, $props, $all, $noval, $depth, $noroot, $visited
	public static void readDirRecursive() {
		
		// TODO: implement
		
	}

}
