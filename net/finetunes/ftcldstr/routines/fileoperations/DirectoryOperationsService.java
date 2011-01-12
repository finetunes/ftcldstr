package net.finetunes.ftcldstr.routines.fileoperations;

import java.util.HashMap;

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
	
	public static Object[] getFolderList(String fn, String ru, String filter) {
		
		// TODO: implement
		return new Object[]{};
		
	}
	
    public static Object[] getFolderList(String fn, String ru) {
     
        return getFolderList(fn, ru, null);
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
