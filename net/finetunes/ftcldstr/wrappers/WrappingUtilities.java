package net.finetunes.ftcldstr.wrappers;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService.StatData;

public class WrappingUtilities {

    public static ArrayList<String> getFileList(RequestParams requestParams, String fn) {
        
        ArrayList<String> files = new ArrayList<String>();
        CommonContentWrapper rdw = new CommonContentWrapper(requestParams);
        CommonWrapperResult d = rdw.runCommand(requestParams, requestParams.getUsername(), "list", new String[]{fn});

        if (d == null || d.getExitCode() != 0) {
            Logger.log("Error reading directory content. Dir: " + fn + "; Error: " + d.getErrorMessage());
            return null;
        }
        else {
            String content = d.getContent();
            if (content != null && !content.isEmpty()) {
                String[] list = content.split("\n");
                files = new ArrayList<String>(Arrays.asList(list));
            }
        }
        
        return files;
    }
    
    public static StatData stat(RequestParams requestParams, String fn) {
        
        StatData statData = new FileOperationsService().new StatData();
        CommonContentWrapper rdw = new CommonContentWrapper(requestParams);
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
        
        AuthenticationWrapper w = new AuthenticationWrapper(requestParams);
        CommonWrapperResult d = w.runCommand(requestParams, null, null, new String[]{username, password}, false);

        if (d != null && d.getExitCode() == 0) {
            return true;
        }
        
        return false;
    }
    

    private static boolean runBooleanCommand(RequestParams requestParams, String fn, String command) {
        return runBooleanCommand(requestParams, fn, command, false, null);
    }

    private static boolean runBooleanCommand(RequestParams requestParams, String fn, String command,
            boolean logOnError) {
        return runBooleanCommand(requestParams, fn, command, logOnError, null);
    }
    
    private static boolean runBooleanCommand(RequestParams requestParams, String fn, String command,
            boolean logOnError, ArrayList<String> err) {
        
        CommonContentWrapper cw = new CommonContentWrapper(requestParams);
        CommonWrapperResult d = cw.runCommand(requestParams, requestParams.getUsername(), command, new String[]{fn});

        if (d != null && d.getExitCode() == 0) {
            return true;
        }
        else if (logOnError) {
            Logger.log("Error running external command: " + command + " on file " + fn + "; " + d.getErrorMessage());
            if (err == null) {
                err = new ArrayList<String>();
            }
            if (d.getErrorMessage() != null && !d.getErrorMessage().isEmpty()) {
                err.add(d.getErrorMessage());
            }
        }
        
        return false;
    }    
    
    public static boolean checkFileIsPlain(RequestParams requestParams, String fn) {
        return runBooleanCommand(requestParams, fn, "isPlain");
    }
    
    public static boolean checkFileIsSymbolicLink(RequestParams requestParams, String fn) {
        return runBooleanCommand(requestParams, fn, "isSymbolicLink");
    }
    
    public static boolean checkFileIsBlockSpecialFile(RequestParams requestParams, String fn) {
        return runBooleanCommand(requestParams, fn, "isBlockSpecial");
    }
    
    public static boolean checkFileIsCharacterSpecialFile(RequestParams requestParams, String fn) {
        return runBooleanCommand(requestParams, fn, "isCharacterSpecial");
    }
    
    public static boolean checkFileHasSetuidBitSet(RequestParams requestParams, String fn) {
        return runBooleanCommand(requestParams, fn, "hasUserIDbitSet");
    }
    
    public static boolean checkFileHasStickyBitSet(RequestParams requestParams, String fn) {
        return runBooleanCommand(requestParams, fn, "hasStickyBitSet");
    }
    
    public static boolean checkFileHasSetgidBitSet(RequestParams requestParams, String fn) {
        return runBooleanCommand(requestParams, fn, "hasGroupIDbitSet");
    }
    
    public static boolean checkFileIsReadable(RequestParams requestParams, String fn) {
        return runBooleanCommand(requestParams, fn, "isReadable");
    }
    
    public static boolean checkFileIsWritable(RequestParams requestParams, String fn) {
        return runBooleanCommand(requestParams, fn, "isWritable");
    }
    
    public static boolean checkFileIsExecutable(RequestParams requestParams, String fn) {
        return runBooleanCommand(requestParams, fn, "isExecutable");
    }
    
    public static String fullResolveSymbolicLink(RequestParams requestParams, String fn) {
        
        CommonContentWrapper cw = new CommonContentWrapper(requestParams);
        CommonWrapperResult d = cw.runCommand(requestParams, requestParams.getUsername(), "fullResolve", new String[]{fn});

        if (d == null || d.getExitCode() != 0) {
            Logger.log("Error getting resolved link. File: " + fn + "; Error: " + d.getErrorMessage());
        }
        else {
            String content = d.getContent();
            if (content != null && !content.isEmpty()) {
                content = content.replaceAll("^(\\n|\\r|\\t| )", "").replaceAll("(\\n|\\r|\\t| )$", "");
                return content;
            }
        }
        
        return "";           
    }
    
    public static boolean unlink(RequestParams requestParams, String fn) {
        return runBooleanCommand(requestParams, fn, "unlink", true);
    }
    
    public static boolean mkdir(RequestParams requestParams, String fn, ArrayList<String> err) {
        return runBooleanCommand(requestParams, fn, "mkdir", true, err);
    }
    
    public static String readFile(RequestParams requestParams, String fn) {
        
        CommonContentWrapper cw = new CommonContentWrapper(requestParams);
        CommonWrapperResult d = cw.runCommand(requestParams, requestParams.getUsername(), "read", new String[]{fn});

        if (d == null || d.getExitCode() != 0) {
            Logger.log("Unable to read file content. File: " + fn + "; Error: " + d.getErrorMessage());
        }
        else {
            String content = d.getContent();
            if (content != null && !content.isEmpty()) {
                return content;
            }
        }
        
        return "";           
    }    
    
    public static InputStream getFileContentReadStream(RequestParams requestParams, String fn) {
        
        CommonContentWrapper cw = new CommonContentWrapper(requestParams);
        AsyncCallResult d = cw.runAsyncCommand(requestParams, requestParams.getUsername(), "read", new String[]{fn});

        if (d == null || d.getInputStream() == null) {
            Logger.log("Unable to read file content. File: " + fn);
        }
        else {
            InputStream is = d.getInputStream();
            return is;
        }
        
        return null;           
    }
    
    public static OutputStream getFileContentWriteStream(RequestParams requestParams, String fn) {
        
        CommonContentWrapper cw = new CommonContentWrapper(requestParams);
        AsyncCallResult d = cw.runAsyncCommand(requestParams, requestParams.getUsername(), "write", new String[]{fn});

        if (d == null || d.getOutputStream() == null) {
            Logger.log("Unable to get file output stream. File: " + fn);
        }
        else {
            OutputStream os = d.getOutputStream();
            return os;
        }
        
        return null;           
    }    
    
}
