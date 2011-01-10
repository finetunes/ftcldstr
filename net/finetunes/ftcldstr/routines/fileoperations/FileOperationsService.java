package net.finetunes.ftcldstr.routines.fileoperations;

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
        
        // TODO: implement
        return false;
        
    }	
    
    public static boolean file_exits(String filename) {
        
        // TODO: implement
        // should work both for file and directory
        return false;
        
    }       

    // determines whether the file is a plain file (not a link, directory, pipe, etc.)
    public static boolean is_plain_file(String filename) {
        
        // TODO: implement
        return false;
        
    }
    
    // determines whether the file is readable with the effective uid/gid
    public static boolean is_file_readable(String filename) {
        
        // TODO: implement
        return false;
        
    }       
    
}
