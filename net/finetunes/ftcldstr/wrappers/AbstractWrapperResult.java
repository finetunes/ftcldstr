package net.finetunes.ftcldstr.wrappers;

public class AbstractWrapperResult {

    // return code from the external script
    private int exitCode = 0;
    
    // error message if an error occured
    private String errorMessage = null;
    
    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    
    
}
