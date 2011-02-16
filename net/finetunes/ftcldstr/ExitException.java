package net.finetunes.ftcldstr;

/**
 * Exception class to handle the situation when the script wants 
 * to exit in the middle of runtime.
 * 
 */
public class ExitException extends RuntimeException {
    
    public ExitException(String message) {
        super(message);
    }

}
