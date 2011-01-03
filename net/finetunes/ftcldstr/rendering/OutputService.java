package net.finetunes.ftcldstr.rendering;

public class OutputService {

	/**
	 * Sends the header and the content directly to the out
	 */
	public static void printHeaderAndContent(String status, 
			String type, String content, String addHeader) {
		
		// TODO: implement
		
	}
	
    public static void printHeaderAndContent(String status, 
            String type, String content) {
        printHeaderAndContent(status, type, content, null);
    }

    public static void printHeaderAndContent(String status, 
            String type) {
        printHeaderAndContent(status, type, "");
    }

    public static void printHeaderAndContent(String status) {
        printHeaderAndContent(status, "text/plain");
    }
    
    public static void printHeaderAndContent() {
        printHeaderAndContent("403 Forbidden");
    }	
    
	/**
	 * sends header directly to the out
	 */
	public static void printFileHeader(String filename) {
		
		// TODO: implement
		
	}
}
