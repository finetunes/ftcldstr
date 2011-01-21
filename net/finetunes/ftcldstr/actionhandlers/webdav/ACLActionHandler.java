package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;

public class ACLActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        // TODO: implement
/*        
    my $fn = $PATH_TRANSLATED;
    my $status = '200 OK';
    my $content = "";
    my $type;
    my %error;
    debug("_ACL($fn)");
    my $xml = join("",<>);
    my $xmldata = "";
    eval { $xmldata = simpleXMLParser($xml,1); };
    if ($@) {
        debug("_ACL: invalid XML request: $@");
        $status='400 Bad Request';
        $type='text/plain';
        $content='400 Bad Request';
    } elsif (!-e $fn) {
        $status = '404 Not Found';
        $type = 'text/plain';
        $content='404 Not Found';
    } elsif (!isAllowed($fn)) {
        $status = '423 Locked';
        $type = 'text/plain';
        $content='423 Locked';
    } elsif (!exists $$xmldata{'{DAV:}acl'}) {
        $status='400 Bad Request';
        $type='text/plain';
        $content='400 Bad Request';
    } else {
        my @ace;
        if (ref($$xmldata{'{DAV:}acl'}{'{DAV:}ace'}) eq 'HASH') {
            push @ace, $$xmldata{'{DAV:}acl'}{'{DAV:}ace'};
        } elsif (ref($$xmldata{'{DAV:}acl'}{'{DAV:}ace'}) eq 'ARRAY') {
            push @ace, @{$$xmldata{'{DAV:}acl'}{'{DAV:}ace'}};
        } else {
            printHeaderAndContent('400 Bad Request');
            return;
        }
        foreach my $ace (@ace) {
            my $p;
            my ($user,$group,$other) = (0,0,0);
            if (defined ($p = $$ace{'{DAV:}principal'})) {
                if (exists $$p{'{DAV:}property'}{'{DAV:}owner'}) { 
                    $user=1;
                } elsif (exists $$p{'{DAV:}property'}{'{DAV:}group'}) {
                    $group=1;
                } elsif (exists $$p{'{DAV:}all'}) {
                    $other=1;
                } else {
                    printHeaderAndContent('400 Bad Request');
                    return;
                }
            } else {
                printHeaderAndContent('400 Bad Request');
                return;
            }
            my ($read,$write) = (0,0);
            if (exists $$ace{'{DAV:}grant'}) {
                $read=1 if exists $$ace{'{DAV:}grant'}{'{DAV:}privilege'}{'{DAV:}read'};
                $write=1 if exists $$ace{'{DAV:}grant'}{'{DAV:}privilege'}{'{DAV:}write'};
            } elsif (exists $$ace{'{DAV:}deny'}) {
                $read=-1 if exists $$ace{'{DAV:}deny'}{'{DAV:}privilege'}{'{DAV:}read'};
                $write=-1 if exists $$ace{'{DAV:}deny'}{'{DAV:}privilege'}{'{DAV:}write'};
            } else {
                printHeaderAndContent('400 Bad Request');
                return;
                
            }
            if ($read==0 && $write==0) {
                printHeaderAndContent('400 Bad Request');
                return;
            }
            my @stat = stat($fn);
            my $mode = $stat[2];
            $mode = $mode & 07777;
            
            my $newperm = $mode;
            if ($read!=0) {
                my $mask = $user? 0400 : $group ? 0040 : 0004;
                $newperm = ($read>0) ? $newperm | $mask : $newperm & ~$mask
            } 
            if ($write!=0) {
                my $mask = $user? 0200 : $group ? 0020 : 0002;
                $newperm = ($write>0) ? $newperm | $mask : $newperm & ~$mask;
            }
            debug("_ACL: old perm=".sprintf('%4o',$mode).", new perm=".sprintf('%4o',$newperm));
            if (!chmod($newperm, $fn)) {
                $status='403 Forbidden';
                $type='text/plain';
                $content='403 Forbidden';
            }

        }
        
    }
    printHeaderAndContent($status, $type, $content);
        
*/        
    }
    
}
