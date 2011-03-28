package net.finetunes.ftcldstr.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.RenderingHelper;


public class AbstractWrapper {
    
    protected String wrapperPath = null;
    protected String wrapperID = null;
    
    protected void setWrapperID(RequestParams requestParams, String wrapperID) {
        this.wrapperID = wrapperID;
        wrapperPath = WrapperPathProvider.getWrapperPath(requestParams, wrapperID);
    }
    
    private String[] buildCommandList(String[] args, boolean allowlog) {
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
        
        return commands.toArray(new String[0]);
    }
    
    private ExternalCallResult run(String wrapperPath, String[] commands) {

        ProcessStreamReader outputStream;          
        ProcessStreamReader errorStream;
        ExternalCallResult result;

        try {
            Process p = Runtime.getRuntime().exec(commands);
            
            outputStream = new ProcessStreamReader(p.getInputStream()); // output stream of the process          
            errorStream = new ProcessStreamReader(p.getErrorStream()); // error stream of the process
            
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
    
    private AsyncCallResult asyncrun(String wrapperPath, String[] commands) {

        AsyncCallResult result;

        try {
            Process p = Runtime.getRuntime().exec(commands);
            
            InputStream is = p.getInputStream(); // output stream of the process
            InputStream es = p.getErrorStream(); // error stream of the process
            OutputStream os = p.getOutputStream(); // input stream for writing to
            
            result = new AsyncCallResult(is, es, os);
        }
        catch (IOException e) {
            result = new AsyncCallResult(null, null, null);
        }

        return result;
    }    
    
    protected CommonWrapperResult runCommand(RequestParams requestParams,
            String username, String command, String[] arg) {
        return runCommand(requestParams, username, command, arg, true);
    }

    private String[] getArgs(RequestParams requestParams,
            String username, String command, String[] arg, boolean allowlog) {

        if (wrapperID == null) {
            Logger.log("Unable to process the request: wrapper id is not set.");
            return null;
        }
        else {
            ArrayList<String> args = new ArrayList<String>();

            if (username != null && !command.isEmpty()) {
                args.add(username);
            }
            if (command != null && !command.isEmpty()) {
                args.add(command);
            }
            
            if (arg != null) {
                args.addAll(Arrays.asList(arg));
            }
            String[] commands = buildCommandList(args.toArray(new String[0]), allowlog);
            return commands;
        }        
    }
    
    protected CommonWrapperResult runCommand(RequestParams requestParams,
            String username, String command, String[] arg, boolean allowlog) {
        
        String[] commands = getArgs(requestParams, username, command, arg, allowlog);
        if (commands != null && wrapperPath != null) {
            CommonWrapperResult result = new CommonWrapperResult();
            ExternalCallResult externalCallResult = run(wrapperPath, commands);
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
        
        return null;
    }    
    
    protected AsyncCallResult runAsyncCommand(RequestParams requestParams,
            String username, String command, String[] arg) {
        return runAsyncCommand(requestParams, username, command, arg, true);
    }
    
    protected AsyncCallResult runAsyncCommand(RequestParams requestParams,
            String username, String command, String[] arg, boolean allowlog) {
        
        String[] commands = getArgs(requestParams, username, command, arg, allowlog);
        if (commands != null && wrapperPath != null) {
            AsyncCallResult asyncCallResult = asyncrun(wrapperPath, commands);
            return asyncCallResult;
        }
        
        return null;
    }        
}
