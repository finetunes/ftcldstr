package net.finetunes.ftcldstr.rendering;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import net.finetunes.ftcldstr.helper.ConfigService;

public class RenderingHelper {
	
	public static String getmodecolors(String filename, String mode) {
		
		// TODO: implement
		return null;
		
	}
	
	public static String mode2str(String filename, String mode) {
		
		// TODO: implement
		return null;
		
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
        String r;
        
        try {
            r = URLDecoder.decode(s, ConfigService.CHARSET);
        }
        catch (UnsupportedEncodingException e) {
            r = URLDecoder.decode(s);
        }
        
        return r;
    }
    
    public static String uri_escape(String s) {
        String r;
        
        try {
            r = URLEncoder.encode(s, ConfigService.CHARSET);
        }
        catch (UnsupportedEncodingException e) {
            r = URLEncoder.encode(s);
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
    

}
