package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;

/**
 * The MOVE operation on a non-collection resource is the logical
 * equivalent of a copy (COPY), followed by consistency maintenance
 * processing, followed by a delete of the source, where all three
 * actions are performed in a single operation.  The consistency
 * maintenance step allows the server to perform updates caused by the
 * move, such as updating all URLs, other than the Request-URI that
 * identifies the source resource, to point to the new destination
 * resource.
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class MoveActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
  
        String status = "201 Created";
        String host = requestParams.getHeader("Host");
        String destination = requestParams.getHeader("Destination");
        // TODO
        
/*        
        my $status = '201 Created';
        my $host = $cgi->http('Host');
        my $destination = $cgi->http('Destination');
        my $overwrite = defined $cgi->http('Overwrite')?$cgi->http('Overwrite') : "T";
        debug("_MOVE: $PATH_TRANSLATED => $destination");
        $destination=~s@^https?://([^\@]+\@)?\Q$host\E$VIRTUAL_BASE@@;
        $destination=uri_unescape($destination);
        $destination=uri_unescape($destination);
        $destination=$DOCUMENT_ROOT.$destination;

        if ( (!defined $destination) || ($destination eq "") || ($PATH_TRANSLATED eq $destination) ) {
            $status = '403 Forbidden';
        } elsif ( -e $destination && $overwrite eq "F") {
            $status = '412 Precondition Failed';
        } elsif ( ! -d dirname($destination)) {
            $status = "409 Conflict - ".dirname($destination);
        } elsif (!isAllowed($PATH_TRANSLATED,-d $PATH_TRANSLATED) || !isAllowed($destination, -d $destination)) {
            $status = '423 Locked';
        } else {
            unlink($destination) if -f $destination;
            $status = '204 No Content' if -e $destination;
            if (rmove($PATH_TRANSLATED, $destination)) {
                db_moveProperties($PATH_TRANSLATED, $destination);
                db_delete($PATH_TRANSLATED);
                inheritLock($destination,1);
                logger("MOVE($PATH_TRANSLATED, $destination)");
            } else {
                $status = '403 Forbidden';
            }
        }
        debug("_MOVE: status=$status");
        printHeaderAndContent($status);        
*/        
        
        
    }
    
}
