package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;

public class SearchActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {
        // TODO: implement
/*
    my @resps;
    my $status = 'HTTP/1.1 207 Multistatus';
    my $content = "";
    my $type='application/xml';
    my @errors;

    my $xml = join("",<>);
    my $xmldata = "";
    eval { $xmldata = simpleXMLParser($xml,1); };
    if ($@) {
        debug("_SEARCH: invalid XML request: $@");
        debug("_SEARCH: xml-request=$xml");
        $status='400 Bad Request';
        $type='text/plain';
        $content='400 Bad Request';
    } elsif (exists $$xmldata{'{DAV:}query-schema-discovery'}) {
        debug("_SEARCH: found query-schema-discovery");
        push @resps, { href=>$REQUEST_URI, status=>$status, 
                'query-schema'=> { basicsearchschema=> { properties => { 
                    propdesc => [
                        { 'any-other-property'=>undef, searchable=>undef, selectable=>undef, caseless=>undef, sortable=>undef }
                    ]
                }, operators => { 'opdesc allow-pcdata="yes"' => 
                                [ 
                                    { like => undef, 'operand-property'=>undef, 'operand-literal'=>undef },
                                    { contains => undef }
                                ] 
                }}}};
    } elsif (exists $$xmldata{'{DAV:}searchrequest'}) {
        foreach my $s (keys %{$$xmldata{'{DAV:}searchrequest'}}) {
            if ($s =~ /{DAV:}basicsearch/) {
                handleBasicSearch($$xmldata{'{DAV:}searchrequest'}{$s}, \@resps,\@errors);
            }
        }
    }
    if ($#errors>-1) {
        $content = createXML({error=>\@errors});
        $status='409 Conflict';
    } elsif ($#resps > -1) {
        $content = createXML({multistatus=>{ response=>\@resps }});
    } else {
        $content = createXML({multistatus=>{ response=> { href=>$REQUEST_URI, status=>'404 Not Found' }}});
    }
    printHeaderAndContent($status, $type, $content);
        
*/        
    }
}
