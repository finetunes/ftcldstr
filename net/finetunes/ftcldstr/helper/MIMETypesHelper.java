package net.finetunes.ftcldstr.helper;

public class MIMETypesHelper {
	
//	public static void readMIMETypes() {
//		
//	}
	
	public static String getMIMEType(String filename) {
	    
	    // TODO
	    // check filename is null
	    // filename!=null

	    String extension = "default";
	    String ext = filename.replaceFirst("^.*\\.([^\\.]+)$", "$1");
	    
	    if (ext != null && !ext.isEmpty()) {
	        extension = ext;
	    }
	    
/*	    
	    my ($filename) = @_;
	    my $extension= "default";
	    if ($filename=~/\.([^\.]+)$/) {
	        $extension=$1;
	    }
	    my @t = grep /\b\Q$extension\E\b/i, keys %MIMETYPES;
	    return $#t>-1 ? $MIMETYPES{$t[0]} : $MIMETYPES{default};	    
*/	    
		// TODO: implement
        return null;
		
	}

}
