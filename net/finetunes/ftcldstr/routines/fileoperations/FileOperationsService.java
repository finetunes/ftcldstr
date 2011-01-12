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
    
}
