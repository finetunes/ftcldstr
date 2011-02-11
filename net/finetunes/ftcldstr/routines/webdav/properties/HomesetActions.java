package net.finetunes.ftcldstr.routines.webdav.properties;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;

public class HomesetActions {

    public static String getAddressbookHomeSet(RequestParams requestParams, String uri) {
        
        if (ConfigService.ADDRESSBOOK_HOME_SET == null) {
            return uri;
        }
        
        String rmuser = requestParams.getUsername();
        
        if (!ConfigService.ADDRESSBOOK_HOME_SET.containsKey(rmuser)) {
            rmuser = ""; // TODO: $rmuser = $< --- real uid of the current process
        }
        
        if (ConfigService.ADDRESSBOOK_HOME_SET.containsKey(rmuser)) {
            return ConfigService.ADDRESSBOOK_HOME_SET.get(rmuser);
        }
        else {
            return ConfigService.ADDRESSBOOK_HOME_SET.get("default");
        }
    }
    
    public static String getCalendarHomeSet(String uri) {
        
        // TODO: implement after HomesetActions.getAddressbookHomeSet() is ready
        return null;
    }
    
    
}
