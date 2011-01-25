package net.finetunes.ftcldstr.serialization;


public class BasicRoutines {
	
    /**
     * @deprecated
     */
    public static void db_get(String filename, String token) {
		
	    // replaced with WebDAVLocks.getLocks();
	}
	
    /**
     * @deprecated
     */
	public static void db_insert(
			String baseFilename, String filename, 
			String type, String scope, String token, 
			int depth, int timeout, 
			String owner) {
	    
	    // replaced with WebDAVLocks.insertLock();
	}

	/**
	 * @deprecated
	 */
	public static void db_update(String baseFilename, String filename, int timeout) {
		
	     // replaced with WebDAVLocks.updateLock();
		
	}
	
	/**
	 * @deprecated
	 */
	public static void db_delete(String filename, String token) {
		
        // replaced with WebDAVLocks.deleteLock();
	}

    /**
     * @deprecated
     */	
    public static void db_delete(String filename) {
        
        // replaced with WebDAVLocks.deleteLock();
    }	
	
}
