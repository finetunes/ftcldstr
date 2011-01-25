package net.finetunes.ftcldstr.routines.webdav.properties;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;

public class PropertiesHelper {
	
	public static String getPropValue(String propertyName, String filename, String uri) {

		// TODO: implement
		return null;
	}
	
	// TODO: params
	public static void getPropStat() {
		
	}
	
	public static String getETag(RequestParams requestParams, String file) {
		
	    if (file == null) {
	        file = requestParams.getPathTranslated();
	    }
	    
        // my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks) = stat($file);
	    Object[] stat = FileOperationsService.stat(file);
        int size = ((Integer)stat[7]).intValue();
        long mtime = ((Long)stat[9]).longValue();
	    
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
