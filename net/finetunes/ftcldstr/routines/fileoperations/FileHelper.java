package net.finetunes.ftcldstr.routines.fileoperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.routines.webdav.LockingService;
import net.finetunes.ftcldstr.routines.webdav.QueryService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesHelper;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;


public class FileHelper {
	
	public static String getfancyfilename(
	        RequestParams requestParams,
			String full, String s,
			String m, String fn) {
		
	    if (s == null) {
	        s = "";
	    }
	    
	    String ret = s;
	    String q = QueryService.getQueryParams(requestParams);
	    
	    // fixes root folder navigation bug
	    if (full.equals("//")) {
	        full = "/";
	    }
	    
	    if (q != null && !q.isEmpty() && fn != null && !fn.isEmpty() && FileOperationsService.is_directory(requestParams, fn)) {
	        full += "?" + q;
	    }
	    
	    String fntext = s;
	    if (s.length() > ConfigService.MAXFILENAMESIZE) {
	        fntext = s.substring(0, ConfigService.MAXFILENAMESIZE - 3);
	    }
	    
	    if ((!FileOperationsService.is_directory(requestParams, fn) && FileOperationsService.is_file_readable(requestParams, fn)) || 
	            FileOperationsService.is_file_executable(requestParams, fn)) {
	        ret = "<a href=\"" + full + "\" title=\"" + s + "\" style=\"padding:1px\">";
	        ret += RenderingHelper.escapeHTML(fntext);
	        ret += "</a>";
	    }
	    else {
	        ret = RenderingHelper.escapeHTML(fntext);
	    }
	    
	    if (s.length() > ConfigService.MAXFILENAMESIZE) {
	        ret += "...";
	    }
	    else {
	        char[] spaces = new char[ConfigService.MAXFILENAMESIZE - s.length()];
	        Arrays.fill(spaces, ' ');
	        ret += new String(spaces);
	    }

	    // original code
	    // $full=~/([^\.]+)$/;
	    // my $suffix = $1 || $m;
	    
	    String suffix = null;
	    int li = full.lastIndexOf(".");
	    if (li >= 0) {
	        suffix = full.substring(li + 1);
	    }
	    
	    // String suffix = full.replaceFirst("^.*(\\.)?([^\\.]*)$", "$1");
	    if (suffix == null || suffix.isEmpty() || suffix.equals("/")) {
	        suffix = m;
	    }
	    
	    String icon = ConfigService.ICONS.get(m);
	    if (icon == null || icon.isEmpty()) {
	        icon = ConfigService.ICONS.get("default");
	    }
	    
	    int width = ConfigService.ICON_WIDTH;
	    if (width < 0) {
	        width = 22;
	    }
	    
	    String onmouseover = "";
	    String onmouseout = "";
	    String align = "";
	    String id = PropertiesHelper.getETag(requestParams, fn);
	    
	    id = id.replaceAll("\"", "");

	    if (ConfigService.ENABLE_THUMBNAIL && FileOperationsService.is_file_readable(requestParams, fn) &&
	            MIMETypesHelper.getMIMEType(fn).startsWith("image/")) {
	        
            // Original code: $icon=$full.($full=~/\?.*/?';':'?').'action=thumb';
	        icon = "?";
	        if (full.matches("(?s).*\\?.*")) {
	            icon = ConfigService.URL_PARAM_SEPARATOR;
	        }
	        
	        icon += "action=thumb";
	        icon = full + icon;
	        
	        if (ConfigService.THUMBNAIL_WIDTH > 0 && ConfigService.ICON_WIDTH < ConfigService.THUMBNAIL_WIDTH) {
	            align = "vertical-align:top;padding: 1px 0px 1px 0px;";
	            onmouseover = "javascript:this.intervalFunc=function() { if (this.width<" + ConfigService.THUMBNAIL_WIDTH + ") this.width+=" +
	                ((ConfigService.THUMBNAIL_WIDTH - ConfigService.ICON_WIDTH) / 15) +
	                "; else window.clearInterval(this.intervalObj);}; this.intervalObj = window.setInterval(\"document.getElementById('" + id + "').intervalFunc();\", 10);";
	            onmouseout = "javascript:window.clearInterval(this.intervalObj);this.width=" + ConfigService.ICON_WIDTH + ";";
	        }
	    }
	    
	    if (q != null && q.matches("(?s).*\\?.*")) {
	        full += ConfigService.URL_PARAM_SEPARATOR + "action=props";
	    }
	    else {
	        full += "?action=props";
	    }
	    
	    String r = "<a href=\"" + full + "\" title=\"" + ConfigService.stringMessages.get("showproperties") + "\">";
	    r += "<img id=\"" + id + "\" src=\"" + icon + "\"" +
	    		" alt=\"[" + suffix + "]\"" +
	    		" style=\"" + align + ";border:0;\"" +
	    		" width=\"" + width + "\"" +
	    		" onmouseover=\"" + onmouseover + "\" onmouseout=\"" + onmouseout + "\">";
	    r += "</a>";
	    ret = r + " " + ret;
	    
	    return ret;
	}
	
	public static boolean moveToTrash(RequestParams requestParams, String fn) {
	    
	    boolean ret = false;
	    String etag = PropertiesHelper.getETag(requestParams, fn);
	    etag = etag.replaceAll("\"", "");
	    String trash = ConfigService.TRASH_FOLDER + etag + "/";
	    
	    if (fn.startsWith(ConfigService.TRASH_FOLDER)) {
	        // delete within trash
	        ArrayList<String[]> err = new ArrayList<String[]>();
	        FileHelper.deltree(requestParams, fn, err);
	        if (err.size() == 0) {
	            ret = true;
	        }
	        
	        Logger.debug("moveToTrash(" + fn + ")->/dev/null = " + ret);
	    }
	    else if (FileOperationsService.file_exits(requestParams, ConfigService.TRASH_FOLDER) || FileOperationsService.mkdir(requestParams, ConfigService.TRASH_FOLDER)) {
	        if (FileOperationsService.file_exits(requestParams, trash)) {
	            int i = 0;
	            while (FileOperationsService.file_exits(requestParams, trash)) {
	                // find unused trash folder
	                trash = ConfigService.TRASH_FOLDER + etag + (i++) + "/"; 
	            }
	        }
	        
	        if (FileOperationsService.mkdir(requestParams, trash) && FileOperationsService.rmove(requestParams, fn, trash + FileOperationsService.basename(fn))) {
	            ret = true;
	        }
	        Logger.debug("moveToTrash(" + fn + ")->" + trash + " = " + ret);
	    }
	    
	    return ret;
	}
	
	public static int deltree(RequestParams requestParams, String f, ArrayList<String[]> errRef) {
	    
	    if (errRef == null) {
	        errRef = new ArrayList<String[]>();
	    }
	    
	    int count = 0;
	    String nf = new String(f);
	    nf = nf.replaceAll("/$", "");
	    
	    if (!LockingService.isAllowed(requestParams, f, true)) {
	        Logger.debug("Cannot delete '" + f + "': not allowed");
	        errRef.add(new String[] {f, "Cannot delete " + f});
	    }
	    else if (FileOperationsService.is_symbolic_link(requestParams, nf)) {
	        if (FileOperationsService.unlink(requestParams, nf)) {
	            count++;
	            ConfigService.properties.deleteProperties(f);
	            ConfigService.locks.deleteLock(f);
	        }
	        else {
	            errRef.add(new String[] {f, "Cannot delete '" + f + "': see log for details"});
	        }
	    }
	    else if (FileOperationsService.is_directory(requestParams, f)) {
	        ArrayList<String> files = WrappingUtilities.getFileList(requestParams, f);
	        if (files != null) {
    	        Iterator<String> it = files.iterator();
    	        while (it.hasNext()) {
    	            String sf = it.next();
    	            if (!sf.matches("(\\.|\\.\\.)")) {
    	                String full = f + sf;
    	                if (FileOperationsService.is_directory(requestParams, full) && !full.endsWith("/")) {
    	                    full += "/";
    	                }
    	                count += FileHelper.deltree(requestParams, full, errRef);
    	            }
    	        }
    	        
    	        if (FileOperationsService.rmdir(requestParams, f)) {
    	            count++;
    	            if (!f.endsWith("/")) {
    	                f += "/";
    	            }
    
    	            ConfigService.properties.deleteProperties(f);
    	            ConfigService.locks.deleteLock(f);
    	        }
    	        else {
    	            errRef.add(new String[] {f, "Cannot delete '" + f + "': see log for details"});
    	        }
	        }
	        else {
	            errRef.add(new String[] {f, "Cannot open '" + f + "': see log for details"});
	        }
	    }
	    else if (FileOperationsService.file_exits(requestParams, f)) {
	        if (FileOperationsService.unlink(requestParams, f)) {
	            count++;
	            ConfigService.properties.deleteProperties(f);
	            ConfigService.locks.deleteLock(f);
	        }
	        else {
	            errRef.add(new String[] {f, "Cannot delete '" + f + "': see log for details"});
	        }
	    }
	    else {
            errRef.add(new String[] {f, "File/Folder '" + f + "' not found"});
	    }
	    
	    return count;
	}
	
	public static Object[] getQuota() {

	    /*
	    // usage
        ql = (Integer)quota[0];
        qu = (Integer)quota[1];
        
        if (ql != null && qu != null) {
            
        }	  
        */
	    
	    // test data
	    Integer a = 237732444;
	    Integer b = 19212231;
	    
	    return new Object[] {a, b};
	    
		// TODO: implement
		// return new Object[]{};
		
	}

}
