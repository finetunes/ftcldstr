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
    
    protected String wrapperPath = null;
    protected String wrapperID = null;
    
    protected void setWrapperID(String wrapperID) {
        this.wrapperID = wrapperID;
    }
    
    
    private ExternalCallResult run(String wrapperPath, String[] args, boolean allowlog) {

        ProcessStreamReader outputStream;          
        ProcessStreamReader errorStream;
        ExternalCallResult result;
        
        ArrayList<String> precommands = new ArrayList<String>();
        precommands.add("sudo");
        precommands.add(wrapperPath);
        
        ArrayList<String> commands = new ArrayList<String>();
        commands.addAll(precommands);
        commands.addAll(Arrays.asList(args));
        
        if (allowlog) {
            String cl = RenderingHelper.joinArray(commands.toArray(new String[0]), " ");
            Logger.debug("Calling external command: " + cl);
        }
        else {
            String cl = RenderingHelper.joinArray(precommands.toArray(new String[0]), " ");
            Logger.debug("Calling external command (params hidden): " + cl);
        }
        
//        String[] c = new String[] {
//                "/var/cache/jetty/Jetty_0_0_0_0_8080_root.war____.cwywpb/webapp/wrappers/operation.sh",
//                "zeitgeist",
//                "list",
//                "/"
//        };
        
        try {
            Process p = Runtime.getRuntime().exec(commands.toArray(new String[0]));
            
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
            String username, String command, String[] arg) {
        return runCommand(requestParams, username, command, arg, false);
    }

    
    protected CommonWrapperResult runCommand(RequestParams requestParams,
            String username, String command, String[] arg, boolean allowlog) {
        
        if (wrapperID == null) {
            Logger.log("Unable to process the request: wrapper id is not set.");
            return null;
        }
        else {
            CommonWrapperResult result = new CommonWrapperResult();
            wrapperPath = WrapperPathProvider.getWrapperPath(requestParams, wrapperID);
            
            ArrayList<String> args = new ArrayList<String>();

            if (username != null && !command.isEmpty()) {
                args.add(username);
            }
            if (command != null && !command.isEmpty()) {
                args.add(command);
            }
            
            args.addAll(Arrays.asList(arg));
            
            ExternalCallResult externalCallResult = run(wrapperPath, args.toArray(new String[0]), allowlog);
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
