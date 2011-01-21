package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;

public class RebindActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        // TODO: implement
/*        
    my ($status,$type,$content) = ('200 OK', undef, undef);
    my $overwrite = defined $cgi->http('Overwrite')?$cgi->http('Overwrite') : "T";
    my $xml = join("",<>);
    my $xmldata = "";
    my $host = $cgi->http('Host');
    eval { $xmldata = simpleXMLParser($xml,0); };
    if ($@) {
        $status='400 Bad Request';
        $type='text/plain';
        $content='400 Bad Request';
    } else {
        my $segment = $$xmldata{'{DAV:}segment'};
        my $href = $$xmldata{'{DAV:}href'};
        $href=~s/^https?:\/\/\Q$host\E+$VIRTUAL_BASE//;
        $href=uri_unescape(uri_unescape($href));
        my $src = $DOCUMENT_ROOT.$href;
        my $dst = $PATH_TRANSLATED.$segment;

        my $nsrc = $src; $nsrc =~ s/\/$//;
        my $ndst = $dst; $ndst =~ s/\/$//;

        if (!-e $src) {
            $status = '404 Not Found';
        } elsif (!-l $nsrc) { 
            $status = '403 Forbidden';
        } elsif (-e $dst && $overwrite ne 'T') {
            $status = '403 Forbidden';
        } elsif (-e $dst && !-l $ndst) {
            $status = '403 Forbidden';
        } else {
            $status = -l $ndst ? '204 No Content' : '201 Created';
            unlink($ndst) if -l $ndst;
            if (!rename($nsrc, $ndst)) {
                my $orig = readlink($nsrc);
                $status = '403 Forbidden' unless symlink($orig, $dst) && unlink($nsrc);
            }
        }
    }
    printHeaderAndContent($status, $type, $content);
        
*/        
    }
    
}
