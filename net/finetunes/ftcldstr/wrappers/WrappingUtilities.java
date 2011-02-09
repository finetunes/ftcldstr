package net.finetunes.ftcldstr.wrappers;

import java.util.ArrayList;
import java.util.Arrays;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.Logger;

public class WrappingUtilities {

    public static ArrayList<String> getFileList(RequestParams requestParams, String fn) {
        
        ArrayList<String> files = new ArrayList<String>();
        CommonContentWrapper rdw = new CommonContentWrapper();
        CommonWrapperResult d = rdw.runCommand(requestParams, "list", fn);

        if (d == null || d.getExitCode() != 0) {
            Logger.log("Error reading directory content. Dir: " + fn + "; Error: " + d.getErrorMessage());
            return null;
        }
        else {
            String content = d.getContent(); 
            String[] list = content.split("\n");
            files = new ArrayList<String>(Arrays.asList(list));
        }
        
        return files;
    }
    
    public static boolean isPlainFile(RequestParams requestParams, String fn) {
        return false; // TODO

        
    }
    
}
