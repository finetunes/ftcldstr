package net.finetunes.ftcldstr.wrappers.common;

import java.io.IOException;

import net.finetunes.ftcldstr.wrappers.types.WrapperResult;

public class AbstractWrapper {
    
    private String username;
    private String password;
    
    protected String wrapperPath = null;
    
    /**
     * Constructor for the access using no privilege change.
     */
    public AbstractWrapper() {
        
        this(null, null);
    }


    /**
     * Constructor for the wrapper.
     * @param username username to access the filesystem
     * @param password password for the user
     */
    public AbstractWrapper(String username, String password) {
        
        this.username = username;
        this.password = password;
    }
    
    
    protected WrapperResult run(String wrapperPath, String[] args) {

        ProcessStreamReader outputStream;          
        ProcessStreamReader errorStream;
        WrapperResult result;
        
        // TODO: check whether all the parameters are passed correctly
        // check for not logged-in usage as well
        
        String[] command = new String[args.length + 5];
        command[0] = wrapperPath;
        command[1] = "-u";
        command[2] = username;
        command[3] = "-p";
        command[4] = password;
        
        System.arraycopy(args, 0, command, 5, args.length);

        try {
            Process p = Runtime.getRuntime().exec(command);
            
            outputStream = new ProcessStreamReader(p.getInputStream()); // output stream of a process          
            errorStream = new ProcessStreamReader(p.getErrorStream()); // error stream of a process
            
            outputStream.start();
            errorStream.start();        
            
            try {
                p.waitFor();
            }
            catch (InterruptedException e) {
            }
            
            result = new WrapperResult(outputStream.getOutput(), errorStream.getOutput(), p.exitValue());
        }
        catch (IOException e) {
            result = new WrapperResult(null, e.getMessage(), 1);
        }
        
        return result;
        
    }
    
    

}
