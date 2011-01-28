package net.finetunes.ftcldstr.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.finetunes.ftcldstr.helper.Logger;


public class ReadDirectoryContentWrapper extends AbstractWrapper {
    
    public static final String WRAPPER_ID = "READ_DIRECTORY_WRAPPER";
    
    public ReadDirectoryResult readDirectory(String directoryName) {
        
        ReadDirectoryResult result = new ReadDirectoryResult();
        wrapperPath = WrapperPathProvider.getWrapperPath(WRAPPER_ID);
        
        String[] args = new String[]{directoryName};
        WrapperResult externalCallResult = run(wrapperPath, args);
        
        result.setExitCode(externalCallResult.getExitCode());

        if (externalCallResult.getExitCode() != 0) {
            result.setErrorMessage(externalCallResult.getErrOutput());
        }
        else {
            String externalCallOutput = externalCallResult.getStdOutput();
            String[] content = externalCallOutput.split("\n");
            
            // ArrayList<String> directoryContent = new ArrayList<String>(Arrays.asList(content));
            // TODO: returning sublist for windows; check for ubuntu
            ArrayList<String> directoryContent = new ArrayList<String>(Arrays.asList(content).subList(2, content.length));
            
//            for (int i = directoryContent.size() - 1; i >= 0; i--) {
//                if (directoryContent.get(i).equals(".") || directoryContent.get(i).equals("..")) {
//                    directoryContent.remove(i);
//                }
//            }
            
            result.setContent(directoryContent);
        }
        
        return result;
    }
    
    
    public static ArrayList<String> getFileList(String fn) {
        ArrayList<String> files = new ArrayList<String>();
        ReadDirectoryContentWrapper rdw = new ReadDirectoryContentWrapper();
        ReadDirectoryResult d = rdw.readDirectory(fn);
        if (d.getExitCode() != 0) {
            Logger.log("Error reading directory content. Dir: " + fn + "; Error: " + d.getErrorMessage());
            return null;
        }
        else {
            files = d.getContent();
        }
        
        return files;
    }
    

}