package net.finetunes.ftcldstr.serialization;

public class DBPropertiesOperations {
	
    /**
     * @deprecated
     */
	public static void db_insertProperty(String filename, 
			String propertyName, String propertyValue) {
		
        // replaced with PropertiesService.setProperty
	}
	
    /**
     * @deprecated
     */
	public static void db_updateProperty(String filename, 
			String propertyName, String propertyValue) {
		
        // replaced with PropertiesService.setProperty
	}
	
    /**
     * @deprecated
     */
	public static void db_moveProperties(String source, String destination) {
		
        // replaced with PropertiesService.moveProperties
	}
	
    /**
     * @deprecated
     */
	public static void db_copyProperties(String source, String destination) {
		
        // replaced with PropertiesService.copyProperties
	}
	
    /**
     * @deprecated
     */
	public static void db_deleteProperties(String filename) {
		
	    // replaced with PropertiesService.deleteProperties
	}
	
    /**
     * @deprecated
     */
	public static void db_getProperties(String filename) {
		
	    // replaced with PropertiesService.getProperties
	}
	
    /**
     * @deprecated
     */
	public static void db_getProperty(String filename, String propertyName) {
		
	    // replaced with PropertiesService.getProperty
	}
	
    /**
     * @deprecated
     */
	public static void db_removeProperty(String filename, String propertyName) {
		
	    // replaced with Properties.removeProperty
	}

}
