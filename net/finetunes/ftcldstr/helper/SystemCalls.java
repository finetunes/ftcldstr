package net.finetunes.ftcldstr.helper;

import java.lang.management.ManagementFactory;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;


public class SystemCalls {
    
    public static String getpwuid(RequestParams requestParams, int uid) {
        
        String username = WrappingUtilities.getUserNameByUID(requestParams, String.valueOf(uid));
        if (username != null) {
            return username;
        }
        
        return "";
    }

    public static String getgrgid(RequestParams requestParams, int gid) {
        
        String groupname = WrappingUtilities.getGroupNameByGID(requestParams, String.valueOf(gid));
        if (groupname != null) {
            return groupname;
        }
        
        return "";
    }
    
    // returns real uid of the current process 
    public static String getCurrentProcessUid(RequestParams requestParams) {
        
        String name = ManagementFactory.getRuntimeMXBean().getName();
        
        // The easiest way to get uid of the current process
        // Name is something like uid@hostname
        if (name != null) {
            String[] u = name.split("@");
            if (u.length > 0) {
                return u[0];
            }
        }

        return "-1";
    }
    
}
