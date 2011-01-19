package net.finetunes.ftcldstr.routines.fileoperations;

import java.util.ArrayList;
import java.util.Arrays;

import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.routines.webdav.QueryService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesHelper;


public class FileHelper {
	
	// TODO: find out what all these filename parameters mean
	public static String getfancyfilename(
			String full,
			String s,
			String m,
			String fn) {
		
	    if (s == null) {
	        s = "";
	    }
	    
	    String ret = s;
	    String q = QueryService.getQueryParams();
	    
	    // fixes root folder navigation bug
	    if (full.equals("//")) {
	        full = "/";
	    }
	    
	    if (q != null && !q.isEmpty() && fn != null && !fn.isEmpty() && FileOperationsService.is_directory(fn)) {
	        full += "?" + q;
	    }
	    
	    String fntext = s;
	    if (s.length() > ConfigService.MAXFILENAMESIZE) {
	        fntext = s.substring(0, ConfigService.MAXFILENAMESIZE - 3);
	    }
	    
	    if ((!FileOperationsService.is_directory(fn) && FileOperationsService.is_file_readable(fn)) || 
	            FileOperationsService.is_file_executable(fn)) {
	        ret = "<a href=\"" + full + "\" title=\"" + s + "\" style=\"padding:1px\">";
	        ret += RenderingHelper.HTMLEncode(fntext);
	        ret += "</a>";
	    }
	    else {
	        ret = RenderingHelper.HTMLEncode(fntext);
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
	    String id = PropertiesHelper.getETag(fn);
	    
	    // TODO
//	    $id=~s/\"//g;

	    if (ConfigService.ENABLE_THUMBNAIL && FileOperationsService.is_file_readable(fn) &&
	            MIMETypesHelper.getMIMEType(fn).startsWith("image/")) {
	        
            // Original code: $icon=$full.($full=~/\?.*/?';':'?').'action=thumb';
	        icon = "?";
	        if (full.matches(".*\\?.*")) {
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
	    
	    if (q.matches(".*\\?.*")) {
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
	
	public static boolean moveToTrash(String filename) {
		
		// TODO: implement
		return false;
		
	}
	
	// TODO: parameters
	public static int deltree() {
		
		// TODO: implement
		return -1;

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
