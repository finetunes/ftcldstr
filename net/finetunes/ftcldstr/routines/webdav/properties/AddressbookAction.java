package net.finetunes.ftcldstr.routines.webdav.properties;

import net.finetunes.ftcldstr.helper.ConfigService;

public class AddressbookAction {
	
	public static String getAddressbookHomeSet(String uri) {
	
	    if (ConfigService.ADDRESSBOOK_HOME_SET == null) {
	        return uri;
	    }
	    
        String rmuser = "";
	    // TODO: my $rmuser = $ENV{REDIRECT_REMOTE_USER} || $ENV{REMOTE_USER};
        
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

}
