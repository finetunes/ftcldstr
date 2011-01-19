package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;

/**
 * The COPY method creates a duplicate of the source resource identified
 * by the Request-URI, in the destination resource identified by the URI
 * in the Destination header.  The Destination header MUST be present.
 * The exact behavior of the COPY method depends on the type of the
 * source resource.
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class CopyActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        String status = "201 Created";
        String depth = requestParams.getHeader("Depth");
        String host = requestParams.getHeader("Host");
        String destination = requestParams.getHeader("Destination");
        
/*
TODO
  
    my $overwrite = defined $cgi->http('Overwrite')?$cgi->http('Overwrite') : "T";
    $destination=~s@^https?://([^\@]+\@)?\Q$host\E$VIRTUAL_BASE@@;
    $destination=uri_unescape($destination);
    $destination=uri_unescape($destination);
    $destination=$DOCUMENT_ROOT.$destination;

    debug("_COPY: $PATH_TRANSLATED => $destination");

    if ( (!defined $destination) || ($destination eq "") || ($PATH_TRANSLATED eq $destination) ) {
        $status = '403 Forbidden';
    } elsif ( -e $destination && $overwrite eq "F") {
        $status = '412 Precondition Failed';
    } elsif ( ! -d dirname($destination)) {
        $status = "409 Conflict - $destination";
    } elsif ( !isAllowed($destination,-d $PATH_TRANSLATED) ) {
        $status = '423 Locked';
    } elsif ( -d $PATH_TRANSLATED && $depth == 0 ) {
        if (-e $destination) {
            $status = '204 No Content' ;
        } else {
            if (mkdir $destination) {
                inheritLock($destination);
            } else {
                $status = '403 Forbidden';
            }
        }
    } else {
        $status = '204 No Content' if -e $destination;
        if (rcopy($PATH_TRANSLATED, $destination)) {
            inheritLock($destination,1);
            logger("COPY($PATH_TRANSLATED, $destination)");
        } else {
            $status = '403 Forbidden - copy failed';
        }
    }

    printHeaderAndContent($status);
        
        
        
*/        
    }
    
}
