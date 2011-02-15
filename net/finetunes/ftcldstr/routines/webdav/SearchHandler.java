package net.finetunes.ftcldstr.routines.webdav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.routines.fileoperations.BasicSearch;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesActions;

public class SearchHandler {

	// TODO: params and return type
	public static void handleBasicSearch(RequestParams requestParams,
	        HashMap<String, Object> xmldata,
	        ArrayList<HashMap<String, Object>> resps,
	        ArrayList<HashMap<String, Object>> error) {

	    if (xmldata == null) {
	        xmldata = new HashMap<String, Object>();
	    }
	    
	    // select > (allprop | prop)  
        Object[] propFindElement = PropertiesActions.handlePropFindElement(requestParams, (HashMap<String, Object>)xmldata.get("{DAV:}select"));
        ArrayList<String> props = (ArrayList<String>)propFindElement[0];
        boolean all = ((Boolean)propFindElement[1]).booleanValue();
        boolean noval = ((Boolean)propFindElement[2]).booleanValue();
        
        // TODO: implement
        // where > op > (prop,literal) 
	    // my ($expr,$type) =  buildExprFromBasicSearchWhereClause(undef, $$xmldata{'{DAV:}where'});
        String expr = "";
        Logger.debug("SEARCH: call buildExpr: expr=" + expr);
	    
        // from > scope+ > (href, depth, include-versions?)
        ArrayList<HashMap<String, Object>> scopes = new ArrayList<HashMap<String,Object>>();
        if (xmldata.get("{DAV:}from") != null && ((HashMap<String, Object>)xmldata.get("{DAV:}from")).get("{DAV:}scope") != null &&
                ((HashMap<String, Object>)xmldata.get("{DAV:}from")).get("{DAV:}scope") instanceof HashMap<?, ?>) {
            scopes.add((HashMap<String, Object>)((HashMap<String, Object>)xmldata.get("{DAV:}from")).get("{DAV:}scope"));
        }
        else if (xmldata.get("{DAV:}from") != null && ((HashMap<String, Object>)xmldata.get("{DAV:}from")).get("{DAV:}scope") != null &&
                ((HashMap<String, Object>)xmldata.get("{DAV:}from")).get("{DAV:}scope") instanceof ArrayList<?>) {
            scopes.addAll((ArrayList<HashMap<String, Object>>)((HashMap<String, Object>)xmldata.get("{DAV:}from")).get("{DAV:}scope"));
        }
        else {
            HashMap<String, Object> scope = new HashMap<String, Object>();
            scope.put("{DAV:}href", requestParams.getRequestURI());
            scope.put("{DAV:}depth", "infinity");
            scopes.add(scope);
        }
        
        // limit > nresults
        int limit = Integer.MAX_VALUE;
        try {
            limit = Integer.valueOf((String)((HashMap<String, Object>)xmldata.get("{DAV:}from")).get("{DAV:}scope"));
        }
        catch (NullPointerException e) {
            // no limit; ignire
        }
        catch (NumberFormatException e) {
            // invalid limit; ignore
        }
        
        String host = requestParams.getHeader("Host");
        ArrayList<String[]> matches = new ArrayList<String[]>();
        Iterator<HashMap<String, Object>> it = scopes.iterator();
        while (it.hasNext()) {
            HashMap<String, Object> scope = it.next();
            int depth = 0;
            if ("infinity".equals(scope.get("{DAV:}depth"))) {
                depth = Integer.MAX_VALUE;
            }
            
            try {
                depth = Integer.valueOf((String)scope.get("{DAV:}depth"));
            }
            catch (NumberFormatException e) {
                // ignore
            }
            
            String href = (String)scope.get("{DAV:}href");
            String base = new String(href);
            // original code: $base =~ s@^(https?://([^\@]+\@)?\Q$host\E)?$VIRTUAL_BASE@@;
            base = base.replaceFirst("^(https?://([^\\@]+\\@)?\\Q" + host + "\\E)?" + ConfigService.VIRTUAL_BASE, "");
            base = ConfigService.DOCUMENT_ROOT + RenderingHelper.uri_unescape(RenderingHelper.uri_unescape(base));
            
            Logger.debug("handleBasicSearch: base=" + base + " (href=" + href + "), depth=" + depth + ", limit=" + limit + "\n");
            
            if (!FileOperationsService.file_exits(base)) {
                HashMap<String, Object> response = new HashMap<String, Object>();
                response.put("href", href);
                response.put("status", "HTTP/1.1 404 Not Found");
                HashMap<String, Object> e = new HashMap<String, Object>();
                e.put("search-scope-valid", response);
                error.add(e);
                return;
            }
            
            BasicSearch.doBasicSearch(requestParams, expr, base, href, depth, limit, matches);
        }
        
        // orderby > order+ (caseless=(yes|no))> (prop|score), (ascending|descending)?
        
/*	    
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
