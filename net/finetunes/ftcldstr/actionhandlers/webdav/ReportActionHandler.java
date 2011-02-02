package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;

/**
 * A REPORT request is an extensible mechanism for obtaining information
 * about a resource.  Unlike a resource property, which has a single
 * value, the value of a report can depend on additional information
 * specified in the REPORT request body and in the REPORT request
 * headers.
 * 
 * Description from RFC 3253 (C) The Internet Society (2002).
 * http://www.ietf.org/rfc/rfc3253.txt
 * 
 */

public class ReportActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        // TODO: implement
/*        
    my $fn = $PATH_TRANSLATED;
    my $ru = $REQUEST_URI;
    my $depth = defined $cgi->http('Depth')? $cgi->http('Depth') : 0;
    $depth=-1 if $depth =~ /infinity/i;
    debug("_REPORT($fn,$ru)");
    my $status = '200 OK';
    my $content = "";
    my $type;
    my %error;
    my $xml = join("",<>);
    my $xmldata = "";
    eval { $xmldata = simpleXMLParser($xml,1); };
    if ($@) {
        debug("_REPORT: invalid XML request: $@");
        debug("_REPORT: xml-request=$xml");
        $status='400 Bad Request';
        $type='text/plain';
        $content='400 Bad Request';
    } elsif (!-e $fn) {
        $status = '404 Not Found';
        $type = 'text/plain';
        $content='404 Not Found';
    } else {
        # MUST CalDAV: DAV:expand-property
        $status='207 Multi-Status';
        $type='application/xml';
        my @resps;
        my @hrefs;
        my $rn;
        my @reports = keys %{$xmldata};
        debug("_REPORT: report=".$reports[0]) if $#reports >-1;
        if (defined $$xmldata{'{DAV:}acl-principal-prop-set'}) {
            my @props;
            handlePropElement($$xmldata{'{DAV:}acl-principal-prop-set'}{'{DAV:}prop'}, \@props);
            push @resps, { href=>$ru, propstat=> getPropStat($fn,$ru,\@props) };
        } elsif (defined $$xmldata{'{DAV:}principal-match'}) {
            if ($depth!=0) {
                printHeaderAndStatus('400 Bad Request');
                return;
            }
            # response, href
            my @props;
            handlePropElement($$xmldata{'{DAV:}principal-match'}{'{DAV:}prop'}, \@props) if (exists $$xmldata{'{DAV:}principal-match'}{'{DAV:}prop'});
            readDirRecursive($fn, $ru, \@resps, \@props, 0, 0, 1, 1);
        } elsif (defined $$xmldata{'{DAV:}principal-property-search'}) {
            if ($depth!=0) {
                printHeaderAndStatus('400 Bad Request');
                return;
            }

            my @props;
            handlePropElement($$xmldata{'{DAV:}principal-property-search'}{'{DAV:}prop'}, \@props) if exists $$xmldata{'{DAV:}principal-property-search'}{'{DAV:}prop'};
            readDirRecursive($fn, $ru, \@resps, \@props, 0, 0, 1, 1);
            ### XXX filter data
            my @propertysearch;
            if (ref($$xmldata{'{DAV:}principal-property-search'}{'{DAV:}property-search'}) eq 'HASH') {
                push @propertysearch, $$xmldata{'{DAV:}principal-property-search'}{'{DAV:}property-search'};
            } elsif (ref($$xmldata{'{DAV:}principal-property-search'}{'{DAV:}property-search'}) eq 'ARRAY') {
                push @propertysearch, @{$$xmldata{'{DAV:}principal-property-search'}{'{DAV:}property-search'}};
            }
        } elsif (defined $$xmldata{'{DAV:}principal-search-property-set'}) {
            my %resp;
            $resp{'principal-search-property-set'} = { 
                'principal-search-property' =>
                    [
                        { prop => { displayname=>undef }, description => 'Full name' },
                    ] 
            };
            $content = createXML(\%resp);
            $status = '200 OK';
            $type = 'text/xml';
        } elsif (defined $$xmldata{'{urn:ietf:params:xml:ns:caldav}free-busy-query'}) {
            ($status,$type) = ('200 OK', 'text/calendar');
            $content="BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Example Corp.//CalDAV Server//EN\r\nBEGIN:VFREEBUSY\r\nEND:VFREEBUSY\r\nEND:VCALENDAR";
        } elsif (defined $$xmldata{'{urn:ietf:params:xml:ns:caldav}calendar-query'}) { ## missing filter
            $rn = '{urn:ietf:params:xml:ns:caldav}calendar-query';
            readDirBySuffix($fn, $ru, \@hrefs, 'ics', $depth);
        } elsif (defined $$xmldata{'{urn:ietf:params:xml:ns:caldav}calendar-multiget'}) { ## OK - complete
            $rn = '{urn:ietf:params:xml:ns:caldav}calendar-multiget';
            if (!defined $$xmldata{$rn}{'{DAV:}href'} || !defined $$xmldata{$rn}{'{DAV:}prop'}) {
                printHeaderAndContent('404 Bad Request');
                return;
            }
            if (ref($$xmldata{$rn}{'{DAV:}href'}) eq 'ARRAY') {
                @hrefs = @{$$xmldata{$rn}{'{DAV:}href'}};
            } else {
                push @hrefs,  $$xmldata{$rn}{'{DAV:}href'};
            }
                        
        } elsif (defined $$xmldata{'{urn:ietf:params:xml:ns:carddav}addressbook-query'}) {
            $rn = '{urn:ietf:params:xml:ns:carddav}addressbook-query';
            readDirBySuffix($fn, $ru, \@hrefs, 'vcf', $depth);
        } elsif (defined $$xmldata{'{urn:ietf:params:xml:ns:carddav}addressbook-multiget'}) {
            $rn = '{urn:ietf:params:xml:ns:carddav}addressbook-multiget';
            if (!defined $$xmldata{$rn}{'{DAV:}href'} || !defined $$xmldata{$rn}{'{DAV:}prop'}) {
                printHeaderAndContent('404 Bad Request');
                return;
            }
            if (ref($$xmldata{$rn}{'{DAV:}href'}) eq 'ARRAY') {
                @hrefs = @{$$xmldata{$rn}{'{DAV:}href'}};
            } else {
                push @hrefs,  $$xmldata{$rn}{'{DAV:}href'};
            }
        } else {
            $status ='400 Bad Request';
            $type = 'text/plain';
            $content = '400 Bad Request';
        }
        if ($rn) {
            foreach my $href (@hrefs) {
                my(%resp_200, %resp_404);
                $resp_200{status}='HTTP/1.1 200 OK';
                $resp_404{status}='HTTP/1.1 404 Not Found';
                my $nhref = $href;
                $nhref=~s/$VIRTUAL_BASE//;
                my $nfn.=$DOCUMENT_ROOT.$nhref;
                debug("_REPORT: nfn=$nfn, href=$href");
                if (!-e $nfn) {
                    push @resps, { href=>$href, status=>'HTTP/1.1 404 Not Found' };
                    next;
                } elsif (-d $nfn) {
                    push @resps, { href=>$href, status=>'HTTP/1.1 403 Forbidden' };
                    next;
                }
                my @props;
                handlePropElement($$xmldata{$rn}{'{DAV:}prop'}, \@props) if exists $$xmldata{$rn}{'{DAV:}prop'};
                push @resps, { href=>$href, propstat=> getPropStat($nfn,$nhref,\@props) };
            }
            ### push @resps, { } if ($#hrefs==-1);  ## empty multistatus response not supported
        }
        $content=createXML({multistatus => $#resps>-1 ? { response => \@resps } : undef }) if $#resps>-1;

    }
    debug("_REPORT: REQUEST: $xml");
    debug("_REPORT: RESPONSE: $content");
    printHeaderAndContent($status, $type, $content);
        
*/        
    }
}
