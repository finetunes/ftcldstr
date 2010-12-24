package net.finetunes.ftcldstr.wrappers.types;

/**
 * Class to store the raw result from a wrapper.
 *
 */
public class WrapperResult {
    
    private String stdOutput = null;
    private String errOutput = null;
    private int exitCode = 0;
    
    public WrapperResult(String stdOutput, String errOutput, int errorCode) {
        super();
        this.stdOutput = stdOutput;
        this.errOutput = errOutput;
        this.exitCode = errorCode;
    }
    
    public String getStdOutput() {
        return stdOutput;
    }
    
    public void setStdOutput(String stdOutput) {
        this.stdOutput = stdOutput;
    }
    
    public String getErrOutput() {
        return errOutput;
    }
    
    public void setErrOutput(String errOutput) {
        this.errOutput = errOutput;
    }
    
    public int getExitCode() {
        return exitCode;
    }
    
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }
    

    
}
