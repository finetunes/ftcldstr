package net.finetunes.ftcldstr.routines.webdav.properties;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.SystemCalls;

public class HomesetActions {

    public static String getAddressbookHomeSet(RequestParams requestParams, String uri) {
        
        if (ConfigService.ADDRESSBOOK_HOME_SET == null) {
            return uri;
        }
        
        String rmuser = requestParams.getUsername();
        
        if (!ConfigService.ADDRESSBOOK_HOME_SET.containsKey(rmuser)) {
            rmuser = SystemCalls.getCurrentProcessUid();
        }
        
        if (ConfigService.ADDRESSBOOK_HOME_SET.containsKey(rmuser)) {
            return ConfigService.ADDRESSBOOK_HOME_SET.get(rmuser);
        }
        else {
            return ConfigService.ADDRESSBOOK_HOME_SET.get("default");
        }
    }
    
    public static String getCalendarHomeSet(RequestParams requestParams, String uri) {
        
        if (ConfigService.CALENDAR_HOME_SET == null) {
            return uri;
        }
        
        String rmuser = requestParams.getUsername();
        
        if (!ConfigService.CALENDAR_HOME_SET.containsKey(rmuser)) {
            rmuser = SystemCalls.getCurrentProcessUid();
        }
        
        if (ConfigService.CALENDAR_HOME_SET.containsKey(rmuser)) {
            return ConfigService.CALENDAR_HOME_SET.get(rmuser);
        }
        else {
            return ConfigService.CALENDAR_HOME_SET.get("default");
        }        
    }
    
    
}
