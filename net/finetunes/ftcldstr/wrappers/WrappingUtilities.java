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
        CommonWrapperResult d = rdw.runCommand(requestParams, requestParams.getUsername(), "list", new String[]{fn});

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
        CommonWrapperResult d = rdw.runCommand(requestParams, requestParams.getUsername(), "stat", new String[]{fn});

        if (d == null || d.getExitCode() != 0) {
            Logger.log("Error getting stat info. File: " + fn + "; Error: " + d.getErrorMessage());
            return statData;
        }
        else {
            String content = d.getContent();
            if (content != null && !content.isEmpty()) {
                content = content.replaceAll("(\\n|\\r|\\t| )", "");
                String[] s = content.split(";");

                // data sample 
                // $dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks
                // 2049;302608;755;7;1000;1000;00;4096;1297443944;1297443836;1297443836;8;512
                
                statData.setDev(Integer.parseInt(s[0]));
                statData.setIno(Integer.parseInt(s[1]));
                statData.setMode(Integer.parseInt(s[2], 8));
                statData.setNlink(Integer.parseInt(s[3]));
                statData.setUid(Integer.parseInt(s[4]));
                statData.setGid(Integer.parseInt(s[5]));
                statData.setRdev(Integer.parseInt(s[6], 16));
                statData.setSize(Integer.parseInt(s[7]));
                statData.setAtime(Long.parseLong(s[8]));
                statData.setMtime(Long.parseLong(s[9]));
                statData.setCtime(Long.parseLong(s[10]));
                statData.setBlksize(Integer.parseInt(s[11]));
                statData.setBlocks(Integer.parseInt(s[12]));
            }
        }
        
        return statData;        
    }
    
    public static boolean isUserValid(RequestParams requestParams, String username, String password) {
        
        AuthenticationWrapper w = new AuthenticationWrapper();
        CommonWrapperResult d = w.runCommand(requestParams, null, null, new String[]{username, password});

        if (d != null && d.getExitCode() == 0) {
            return true;
        }
        
        return false;
    }    
    
}
