package net.finetunes.ftcldstr.helper;

import java.util.Date;
import java.util.UUID;

public class GeneratorService {
	
	public static String getuuid(String fn) {
		
	    // original perl code:
	    // my ($fn) = @_;
	    // my $uuid = new OSSP::uuid;
	    // my $uuid_ns = new OSSP::uuid;
	    // $uuid_ns->load("opaquelocktoken:$fn");
	    // $uuid->make("v3", $uuid_ns, "$fn".time());
	    // return $uuid->export("str");	    
	    
	    String namespace = UUID.nameUUIDFromBytes(("opaquelocktoken" + fn).getBytes()).toString(); 
	    String uuid = UUID.nameUUIDFromBytes((namespace + ":" + fn + ((int) (System.currentTimeMillis() / 1000L))).getBytes()).toString();
	    
		return uuid;
	}

}
