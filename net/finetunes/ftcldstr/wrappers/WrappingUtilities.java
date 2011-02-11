package net.finetunes.ftcldstr.wrappers;

import java.util.ArrayList;
import java.util.Arrays;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService.StatData;

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
    
    public static StatData stat(RequestParams requestParams, String fn) {
        
        StatData statData = new FileOperationsService().new StatData();
        CommonContentWrapper rdw = new CommonContentWrapper();
        CommonWrapperResult d = rdw.runCommand(requestParams, "stat", fn);

        if (d == null || d.getExitCode() != 0) {
            Logger.log("Error getting stat info. File: " + fn + "; Error: " + d.getErrorMessage());
            return null;
        }
        else {
            String content = d.getContent();
            System.err.println("STAT result for " + fn + ": " + content);
//            String[] list = content.split("\n");
//            files = new ArrayList<String>(Arrays.asList(list));
        }
        
        return statData;        
    }
    
}
