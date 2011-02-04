package net.finetunes.ftcldstr.routines.webdav;

public class SearchHandler {

	// TODO: params and return type
	public static void handleBasicSearch() {
	    
/*	    
        my ($xmldata, $resps, $error) = @_;
        # select > (allprop | prop)  
        my ($propsref,  $all, $noval) = handlePropFindElement($$xmldata{'{DAV:}select'});
        # where > op > (prop,literal) 
        my ($expr,$type) =  buildExprFromBasicSearchWhereClause(undef, $$xmldata{'{DAV:}where'});
        debug("_SEARCH: call buildExpr: expr=$expr");
        # from > scope+ > (href, depth, include-versions?)
        my @scopes;
        if (ref($$xmldata{'{DAV:}from'}{'{DAV:}scope'}) eq 'HASH') {
            push @scopes, $$xmldata{'{DAV:}from'}{'{DAV:}scope'}; 
        } elsif (ref($$xmldata{'{DAV:}from'}{'{DAV:}scope'}) eq 'ARRAY') {
            push @scopes, @{$$xmldata{'{DAV:}from'}{'{DAV:}scope'}};
        } else { 
            push @scopes, { '{DAV:}href'=>$REQUEST_URI, '{DAV:}depth'=>'infinity'};
        }
        # limit > nresults 
        my $limit = $$xmldata{'{DAV:}limit'}{'{DAV:}nresults'};
    
        my $host = $cgi->http('Host');
        my @matches;
        foreach my $scope (@scopes) {
            my $depth = $$scope{'{DAV:}depth'};
            my $href = $$scope{'{DAV:}href'};
            my $base = $href;
            $base =~ s@^(https?://([^\@]+\@)?\Q$host\E)?$VIRTUAL_BASE@@;
            $base = $DOCUMENT_ROOT.uri_unescape(uri_unescape($base));
            
            debug("handleBasicSearch: base=$base (href=$href), depth=$depth, limit=$limit\n");
    
            if (!-e $base) {
                push @{$error}, { 'search-scope-valid'=> { response=> { href=>$href, status=>'HTTP/1.1 404 Not Found' } } };
                return;
            }
            doBasicSearch($expr, $base, $href, $depth, $limit, \@matches);
        }
        # orderby > order+ (caseless=(yes|no))> (prop|score), (ascending|descending)? 
        my $sortfunc="";
        if (exists $$xmldata{'{DAV:}orderby'} && $#matches>0) {
            my @orders;
            if (ref($$xmldata{'{DAV:}orderby'}{'{DAV:}order'}) eq 'ARRAY') {
                push @orders, @{$$xmldata{'{DAV:}orderby'}{'{DAV:}order'}};
            } elsif (ref($$xmldata{'{DAV:}orderby'}{'{DAV:}order'}) eq 'HASH') {
                push @orders, $$xmldata{'{DAV:}orderby'}{'{DAV:}order'};
            }
            foreach my $order (@orders) {
                my @props = keys %{$$order{'{DAV:}prop'}};
                my $prop = $props[0] || '{DAV:}displayname';
                my $proptype = $SEARCH_PROPTYPES{$prop} || $SEARCH_PROPTYPES{default};
                my $type = $$order{'{DAV:}descending'} ?  'descending' : 'ascending';
                debug("orderby: prop=$prop, proptype=$proptype, type=$type");
                my($ta,$tb,$cmp);
                $ta = qq@getPropValue('$prop',\$\$a{fn},\$\$a{href})@;
                $tb = qq@getPropValue('$prop',\$\$b{fn},\$\$b{href})@;
                if ($SEARCH_SPECIALCONV{$proptype}) {
                    $ta = $SEARCH_SPECIALCONV{$proptype}."($ta)";
                    $tb = $SEARCH_SPECIALCONV{$proptype}."($tb)";
                }
                $cmp = $SEARCH_SPECIALOPS{$proptype}{cmp} || 'cmp';
                $sortfunc.=" || " if $sortfunc ne "";
                $sortfunc.="$ta $cmp $tb" if $type eq 'ascending';
                $sortfunc.="$tb $cmp $ta" if $type eq 'descending';
            }
    
            debug("orderby: sortfunc=$sortfunc");
        }
    
        debug("handleBasicSearch: matches=$#matches");
        foreach my $match ( sort { eval($sortfunc) } @matches ) {
            push @{$resps}, { href=> $$match{href}, propstat=>getPropStat($$match{fn},$$match{href},$propsref,$all,$noval) };
        }

	    
	    
*/	    
	    // TODO: implement
		
	}

}
