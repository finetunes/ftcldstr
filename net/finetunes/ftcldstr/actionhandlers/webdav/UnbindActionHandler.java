package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;

public class UnbindActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        // TODO: implement
/*        
    my ($status,$type,$content) = ('204 No Content', undef, undef);
    my $xml = join("",<>);
    my $xmldata = "";
    eval { $xmldata = simpleXMLParser($xml,0); };
    if ($@) {
        $status='400 Bad Request';
        $type='text/plain';
        $content='400 Bad Request';
    } else {
        my $segment = $$xmldata{'{DAV:}segment'};
        my $dst = $PATH_TRANSLATED.$segment;
        if (!-e $dst ) {
            $status = '404 Not Found';
        } elsif (!-l $dst) {
            $status = '403 Forbidden';
        } elsif (!unlink($dst)) {
            $status = '403 Forbidden';
        }
    }
    printHeaderAndContent($status, $type, $content);        
*/        
    }
}
