package net.finetunes.ftcldstr.rendering;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;

public class RenderingHelper {
	
	public static String getmodecolors(RequestParams requestParams, String fn, int m) {
		
	    String style = "";
	    
	    if ((m & 0020) == 0020) {
	        style = "color: darkred";
	    }
	    
	    if ((m & 0002) == 0002 && !FileOperationsService.file_has_sticky_bit_set(requestParams, fn)) {
	        style = "color: red";
	    }
	    
	    return style;
	}
	
	public static String mode2str(RequestParams requestParams, String fn, int m) {
		
	    if (FileOperationsService.is_symbolic_link(requestParams, fn)) {
	        m = Integer.parseInt((String)FileOperationsService.lstat(fn)[2]); 
	    }
	    
	    String[] ret = new String[10];
	    Arrays.fill(ret, "-");

        if (FileOperationsService.is_directory(fn)) { ret[0] = "d"; }
        if (FileOperationsService.is_block_special_file(requestParams, fn)) { ret[0] = "b"; }
        if (FileOperationsService.is_character_special_file(requestParams, fn)) { ret[0] = "c"; }
        if (FileOperationsService.is_symbolic_link(requestParams, fn)) { ret[0] = "l"; }
        
        if ((m & 0400) == 0400) { ret[1] = "r"; }
        if ((m & 0200) == 0200) { ret[2] = "w"; }
        if ((m & 0100) == 0100) { ret[3] = "x"; }
        if (FileOperationsService.file_has_setuid_bit_set(requestParams, fn)) { ret[3] = "s"; }
        
        if ((m & 0040) == 0040) { ret[4] = "r"; }
        if ((m & 0020) == 0020) { ret[5] = "w"; }
        if ((m & 0010) == 0010) { ret[6] = "x"; }
        if (FileOperationsService.file_has_setgid_bit_set(requestParams, fn)) { ret[6] = "s"; }
        
        if ((m & 0004) == 0004) { ret[7] = "r"; }
        if ((m & 0002) == 0002) { ret[8] = "w"; }
        if ((m & 0001) == 0001) { ret[9] = "x"; }
        if (FileOperationsService.file_has_sticky_bit_set(requestParams, fn)) { ret[9] = "t"; }
        
	    return joinArray(ret, ""); 
	}
	
    public static String HTMLEncode(String s) {
        StringBuffer raus = new StringBuffer();
        char c;

        for (int oo = 0; s != null && oo < s.length(); oo++) {
            c = s.charAt(oo);

            if (c < 65 || c > 122 || c == '\'' || c == '"') {
                raus.append("&#");
                raus.append((int) c);
                raus.append(';');
            } else {
                raus.append(c);
            }
        }

        return raus.toString();
    }
    
    public static String uri_unescape(String s) {
        String r = "";
        
        try {
            r = URLDecoder.decode(s, ConfigService.CHARSET);
        }
        catch (UnsupportedEncodingException e) {
            Logger.log("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        return r;
    }
    
    // $cgi->escape
    public static String uri_escape(String s) {
        String r = "";
        
        try {
            r = URLEncoder.encode(s, ConfigService.CHARSET);
        }
        catch (UnsupportedEncodingException e) {
            Logger.log("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        return r;
    }
    
    // additional routines
    
    public static String joinArray(Object[] strings, String glue) {
        
        String result = "";
        
        if (strings != null && strings.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append((String)(strings[0]));
             
            for (int i = 1; i < strings.length; i++) {
                sb.append(glue);
                sb.append((String)(strings[i]));
            }
             
            result = sb.toString();
        }    
        
        return result;
    }
    
    public static Date parseHTTPDate(String s) {
        
        // Three formats are possbile:
        // Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
        // Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
        // Sun Nov  6 08:49:37 1994       ; ANSI C's asctime() format        
        
        String[] dateFormats = {
            "EEE', 'dd-MMM-yyyy HH:mm:ss z",  // RFC 822, updated by RFC 1123
            "EEE', 'dd-MMM-yyyy HH:mm:ss",    // RFC 822, no timezone
            "EEEE', 'dd-MMM-yy HH:mm:ss z",   // RFC 850, obsoleted by RFC 1036
            "EEEE', 'dd-MMM-yy HH:mm:ss",     // RFC 850, no timezone
            "EEE MMM dd HH:mm:ss yyyy",       // ANSI C's asctime() format 
        };        
        
        Date d = null;
        if (s != null) {
            
            for (int i = 0; i < dateFormats.length; i++) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat(dateFormats[i]);
                    d = format.parse(s);
                }
                catch (ParseException e) {
                    // wrong format                    
                }
                
                if (d != null) {
                    return d;
                }
            }
        }
        
        return d;
    }
}
