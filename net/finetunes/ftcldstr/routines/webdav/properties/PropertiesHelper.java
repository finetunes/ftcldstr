package net.finetunes.ftcldstr.routines.webdav.properties;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.routines.NamespaceService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService.StatData;

public class PropertiesHelper {
	
	public static String getPropValue(RequestParams requestParams, String prop, String fn, String uri) {
	    
	    
        StatusResponse r200 = new StatusResponse();
        StatusResponse r404 = new StatusResponse();
        
        String propname = new String(prop);
        propname = propname.replaceFirst("^\\{[^\\}]*\\}", "");
        
        String propval = null;
        if (!ConfigService.PROTECTED_PROPS.contains(propname)) {
            propval = ConfigService.properties.getProperty(fn, prop);
        }
        
        if (propval == null) {
            PropertiesActions.getProperty(requestParams, fn, uri, propname, null, r200, r404);
            propval = (String)r200.getProp("propname");
        }
	    
        if (propval == null) {
            propval = "__undef__"; 
        }
        
        Logger.debug("getPropValue: " + prop + " = " + propval);
        
        return propval;
	}
	
	public static ArrayList<StatusResponse> getPropStat(RequestParams requestParams, 
	        String fn, String uri, ArrayList<String> props, boolean all, boolean noval) {
	    
	    ArrayList<StatusResponse> propstat = new ArrayList<StatusResponse>();
	    String nfn = FileOperationsService.full_resolve(requestParams, fn);
	    
	    StatData stat = FileOperationsService.stat(requestParams, fn);
	    StatusResponse resp_200 = new StatusResponse();
	    StatusResponse resp_404 = new StatusResponse();
	    
	    resp_200.setStatus("HTTP/1.1 200 OK");
	    resp_404.setStatus("HTTP/1.1 404 Not Found");
	    
	    if (props == null) {
	        props = new ArrayList<String>();
	    }
	    Iterator<String> it = props.iterator();
	    
	    while (it.hasNext()) {
	        String prop = it.next();
	        String xmlnsuri = "DAV:";
	        String propname = new String(prop);
	        
            Pattern p = Pattern.compile("^\\{([^\\}]*)\\}(.*)$");
            Matcher m = p.matcher(prop);
            if (m.find()) {
                xmlnsuri = m.group(1);
                propname = m.group(2);
            }
            
            ArrayList<String> liveSource;
            if (FileOperationsService.is_directory(fn)) {
                liveSource = ConfigService.KNOWN_COLL_LIVE_PROPS; 
            }
            else {
                liveSource = ConfigService.KNOWN_FILE_LIVE_PROPS;
            }
            
            ArrayList<String> liveSourceN;
            if (FileOperationsService.is_directory(nfn)) {
                liveSourceN = ConfigService.KNOWN_COLL_LIVE_PROPS; 
            }
            else {
                liveSourceN = ConfigService.KNOWN_FILE_LIVE_PROPS;
            }
            
            
            if (ConfigService.UNSUPPORTED_PROPS.contains(propname)) {
                Logger.debug("getPropStat: UNSUPPORTED: " + propname);
                resp_404.putProp(prop, null);
                continue;
            }
            else if ((ConfigService.NAMESPACES.get(xmlnsuri) == null || liveSource.contains(propname)) && !ConfigService.PROTECTED_PROPS.contains(propname)) {
                
                String propparam;
                if (prop.matches(".*\\{[^\\}]*\\}.*")) {
                    propparam = prop;
                }
                else {
                    propparam = "{" + NamespaceService.getNameSpaceUri(prop) + "}" + prop;
                }
                String dbval = ConfigService.properties.getProperty(fn, propparam);
                if (dbval != null) {
                    String pv = null;
                    if (!noval) {
                        pv = dbval;
                    }
                    
                    resp_200.putProp(prop, pv);
                    continue;
                }
                else if (!liveSourceN.contains(propname)) {
                    Logger.debug("getPropStat: #1 NOT FOUND: " + prop + " (" + propname + ", " + xmlnsuri + ")");
                    resp_404.putProp(prop, null);
                }
            }
            
            ArrayList<String> liveSourceNP;
            if (FileOperationsService.is_directory(nfn)) {
                liveSourceNP = ConfigService.KNOWN_COLL_PROPS; 
            }
            else {
                liveSourceNP = ConfigService.KNOWN_FILE_PROPS;
            }            
            
            if (liveSourceNP.contains(propname)) {
                if (noval) {
                    resp_200.putProp(prop, null);
                }
                else {
                    PropertiesActions.getProperty(requestParams, fn, uri, prop, stat, resp_200, resp_404);
                }
            }
            else if (!all) {
                Logger.debug("getPropStat: #2 NOT FOUND: " + prop + " (" + propname + ", " + xmlnsuri + ")");
                resp_404.putProp(prop, null);
            }
	    }
	    
	    if (resp_200.propsExist()) {
	        propstat.add(resp_200);
	    }
	    
        if (resp_404.propsExist()) {
            propstat.add(resp_404);
        }
        
	    return propstat;
	}
	
    public static ArrayList<StatusResponse> getPropStat(RequestParams requestParams, 
            String fn, String uri, ArrayList<String> props) {
        return getPropStat(requestParams, fn, uri, props, false, false);
    }
	
	public static String getETag(RequestParams requestParams, String file) {
		
	    if (file == null) {
	        file = requestParams.getPathTranslated();
	    }
	    
        // my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks) = stat($file);
	    StatData stat = FileOperationsService.stat(requestParams, file);
        int size = stat.getSize();
        long mtime = stat.getMtime(); 
	    
	    String digest = file + String.valueOf(size) + String.valueOf(mtime);
	    
	    try {
    	    MessageDigest md = MessageDigest.getInstance("MD5");
    	    md.reset();
    	    md.update(digest.getBytes());
    	    byte[] bd = md.digest();
    	    
    	    StringBuffer hexString = new StringBuffer();
    	    for (int i=0; i < bd.length; i++) {
    	        hexString.append(Integer.toHexString(0xFF & bd[i]));
    	    }
    	    
    	    return "\"" + hexString.toString() + "\"";
	    }
	    catch (NoSuchAlgorithmException e) {
	        Logger.log("Exception: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return null;
	}

}
