package net.finetunes.ftcldstr.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.RenderingHelper;


public class AbstractWrapper {
    
    private String username;
    private String password;
    
    protected String wrapperPath = null;
    protected String wrapperID = null;
    
    /**
     * Constructor for the access using no privilege change.
     */
    public AbstractWrapper() {
        
        // this("jetty", null);
        this("zeitgeist", null); // TODO: set real username
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
    
    protected void setWrapperID(String wrapperID) {
        this.wrapperID = wrapperID;
    }
    
    
    private ExternalCallResult run(String wrapperPath, String[] args) {

        ProcessStreamReader outputStream;          
        ProcessStreamReader errorStream;
        ExternalCallResult result;
        
        String[] precommands = new String[]{
            // "sudo",
            wrapperPath,
            username
        };
        String[] command = new String[args.length + precommands.length];
        
        System.arraycopy(args, 0, command, precommands.length, args.length);
        System.arraycopy(precommands, 0, command, 0, precommands.length);

        String cl = RenderingHelper.joinArray(command, " ");
        Logger.debug("Calling external command: " + cl);
        
//        String[] c = new String[] {
//                "/var/cache/jetty/Jetty_0_0_0_0_8080_root.war____.cwywpb/webapp/wrappers/operation.sh",
//                "zeitgeist",
//                "list",
//                "/"
//        };
        
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
                Logger.log("Exception: " + e.getMessage());
            }
            
            result = new ExternalCallResult(outputStream.getOutput(), errorStream.getOutput(), p.exitValue());
        }
        catch (IOException e) {
            result = new ExternalCallResult(null, e.getMessage(), 1);
        }
        
        return result;
        
    }
    
    protected CommonWrapperResult runCommand(RequestParams requestParams,
            String command, String arg) {
        
        if (wrapperID == null) {
            Logger.log("Unable to process the request: wrapper id is not set.");
            return null;
        }
        else {
            CommonWrapperResult result = new CommonWrapperResult();
            wrapperPath = WrapperPathProvider.getWrapperPath(requestParams, wrapperID);
            
            String[] args = new String[]{command, arg};
            ExternalCallResult externalCallResult = run(wrapperPath, args);
            
            result.setExitCode(externalCallResult.getExitCode());
    
            if (externalCallResult.getExitCode() != 0) {
                result.setErrorMessage(externalCallResult.getErrOutput());
            }
            else {
                String externalCallOutput = externalCallResult.getStdOutput();
                result.setContent(externalCallOutput);
            }
            
            return result;
        }
    }    
    
    

}
