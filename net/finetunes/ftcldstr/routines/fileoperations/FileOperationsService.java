package net.finetunes.ftcldstr.routines.fileoperations;

import java.io.File;
import java.util.HashMap;

public class FileOperationsService {
	
	public static boolean rcopy(String sourcePath, String destinationPath, 
			boolean move) {
		
		// TODO: implement
		return false;
		
	}
	
	public static boolean rmove(String sourcePath, String destinationPath) {
		
		// TODO: implement
		return false;
		
	}
	
	public static boolean changeFilePermissions(String filename, 
			String mode, String type, boolean recurse, 
			HashMap visited) {

		// TODO: implement
		return false;
		
	}

	public static boolean is_hidden(String filename) {
		
		// TODO: implement
		return false;
		
	}
	
	public static String getFileContent(String filename) {
		
		// TODO: implement
		return null;
		
	}
	
	// additional methods
	
    public static boolean is_directory(String filename) {
        
        File file = new File(filename);
        return file.isDirectory();
    }	
    
    public static boolean file_exits(String filename) {
        
        File file = new File(filename);
        return file.exists();
    }       

    // determines whether the file is a plain file (not a link, directory, pipe, etc.)
    public static boolean is_plain_file(String filename) {
        
        // TODO: implement !
        // return false;
        // File.isFile() ?
        
        return !is_directory(filename);
        
    }
    
    // determines whether the file or directory is readable with the effective uid/gid
    public static boolean is_file_readable(String filename) {
        
        // TODO: implement
        return true;
        
    }       
    
    // determines whether the file or directory is writable with the effective uid/gid
    public static boolean is_file_writable(String filename) {
        
        // TODO: implement
        return true;
        
    }
    
    // determines whether the file or directory is executable with the effective uid/gid
    public static boolean is_file_executable(String filename) {
        
        // TODO: implement
        return true;
        
    }
    
    public static String[] splitFilename(String filename) {
        
        String fullparent = "";
        String basename = "";
        String fn = filename;
        
        if (fn != null && !fn.isEmpty()) {
            
            File f = new File(filename);
            if (f.isFile() || f.isDirectory()) {
                if (f.isFile()) {
                    fullparent = f.getParent();
                    basename = f.getName();
                }
                else {
                    fullparent = f.getPath();
                    basename = "";
                }
                
            }
            else {
                if (fn.equals("/") || fn.equals("//")) {
                    fullparent = "";
                    basename = "";
                }
                else {
                    int index = fn.lastIndexOf("/");
                    if (fn.length() == index + 1) {
                        fn = fn.substring(0, index);
                        index = fn.lastIndexOf("/");
                    }
                    
                    if (index > 0) {
                        fullparent = fn.substring(0, index);
                    }
                    
                    if (index + 1 < fn.length()) {
                        basename = fn.substring(index + 1);
                    }
                }
                
            }
        }
        
        return new String[] {fullparent, basename};
        
    }
    
    // returns the result of stat unix funtion
    public static Object[] stat(String filename) {
        
        // ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, 
        // $atime,$mtime,$ctime,$blksize,$blocks) = stat($filename);
        
        // TODO: implement
        // return true;
        
        return new Object[] {0, 0, "0888", 0, 0, 0, 0, 0, 0, "", 0, 0, 0};
        
    }    
    
}
