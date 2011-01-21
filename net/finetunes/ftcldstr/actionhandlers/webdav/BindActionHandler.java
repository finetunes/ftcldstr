package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.rendering.OutputService;

public class BindActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        String status = "200 OK";
        String type = null;
        String content = null;
        
        String overwrite = "T";
        if (requestParams.headerExists("Overwrite")) {
            overwrite = requestParams.getHeader("Overwrite");
        }
        
        String xml = requestParams.getRequestBody();
        String xmldata = "";
        String host = requestParams.getHeader("Host");
        
/*
 * TODO: try/except, etc.        
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

            my $ndst = $dst;
            $ndst=~s /\/$//;

            if (!-e $src) { 
                $status ='404 Not Found';
            } elsif ( -e $dst && ! -l $ndst) {
                $status = '403 Forbidden';
            } elsif (-e $dst && -l $ndst && $overwrite eq "F") {
                $status = '403 Forbidden';
            } else {
                $status = -l $ndst ? '204 No Content' : '201 Created';
                unlink($ndst) if -l $ndst;
                $status = '403 Forbidden' if (!symlink($src, $dst));
            }
        }
*/
        
        OutputService.printHeaderAndContent(requestParams, status, type, content);
        
    }    
    
}
