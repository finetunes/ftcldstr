package net.finetunes.ftcldstr.routines.webdav;

import java.util.ArrayList;
import java.util.Iterator;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.rendering.RenderingService;
import net.finetunes.ftcldstr.routines.fileoperations.DirectoryOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;

public class SearchService {
	
	public static String getSearchResult(RequestParams requestParams, String search,
			String fn, String ru,
			boolean isRecursive, IntegerRef fullcount,
			ArrayList<String> visited) {
	    
	    String content = "";
		ConfigService.ALLOW_FILE_MANAGEMENT = false;
		
		// link loop detection:
		String nfn = FileOperationsService.full_resolve(fn);
		
		if (visited == null) {
		    visited = new ArrayList<String>();
		}
		
		if (visited.contains(nfn)) {
		    return content;
		}
		
		visited.add(nfn);
		
        Object[] folderList = DirectoryOperationsService.getFolderList(requestParams, fn, ru, search);
        
        String list = "";
        int count = 0;
        
        
        if (folderList.length >= 2) {
            list = (String)folderList[0];
            count = ((Integer)folderList[1]).intValue();
        }

        if (count > 0 && isRecursive) {
            content += "<hr>";
            content += "<div style=\"font-size:0.8em;\">";
            content += count;
            if (count > 1) {
                content += ConfigService.stringMessages.get("searchresults");
            }
            else {
                content += ConfigService.stringMessages.get("searchresult"); 
            }
            content += "</div>";
            content += list;
        }
        
        if (fullcount == null) {
            fullcount = new SearchService().new IntegerRef(0);
        }
        
        fullcount.incValue(count);
        
        ArrayList<String> files = WrappingUtilities.getFileList(requestParams, fn);
        if (files != null) {
            Iterator<String> it = files.iterator();
            while (it.hasNext()) {
                String filename = it.next();
                
                if (!filename.matches("(\\.|\\.\\.)")) {
                    String full = fn + filename;
                    if (FileOperationsService.is_hidden(full)) {
                        continue;
                    }
                    
                    String nru = ru + RenderingHelper.uri_escape(filename);
                    if (FileOperationsService.is_directory(full)) {
                        full += "/";
                        nru += "/";
                        content += getSearchResult(requestParams, search, full, nru, true, fullcount, visited);
                    }
                }
            }
        }
        
        if (!isRecursive) {
            if (fullcount.getValue() == 0) {
                content += "<h2>";
                content += ConfigService.stringMessages.get("searchnothingfound") + "'" + RenderingHelper.HTMLEncode(search) + "'" + 
                    ConfigService.stringMessages.get("searchgoback") + RenderingService.getQuickNavPath(ru);
                content += "</h2>";
            }
            else {
                String cc = "<h2>";
                String pkey = "searchresultfor";
                if (fullcount.getValue() > 1) {
                    pkey = "searchresultsfor"; 
                }
                cc += fullcount.getValue() + " " + ConfigService.stringMessages.get(pkey) + "'" + RenderingHelper.HTMLEncode(search) + "'" +
                    ConfigService.stringMessages.get("searchgoback") + RenderingService.getQuickNavPath(ru);
                cc += "</h2>";
                
                if (count > 0) {
                    cc += "<hr>";
                    cc += "<div style=\"font-size:0.8em\">";
                    cc += count;
                    String ckey = "searchresult";
                    if (count > 1) {
                        ckey = "searchresults";
                    }
                    cc += ConfigService.stringMessages.get(ckey);
                    cc += list;
                    cc += "</div>";
                }
                
                content = cc;
            }
        }
        
        return content;
	}
	
    public static String getSearchResult(RequestParams requestParams, String search,
            String fn, String ru) {
        
        return getSearchResult(requestParams, search, fn, ru, false, null, null);
    }
	
	
	// TODO: define xmlref type and return type
	public static void buildExprFromBasicSearchWhereClause(
			String operator, /*xmlref, */ String superOperator) {

    	    
//        my ($op, $xmlref, $superop) = @_;
//        my ($expr,$type) = ( '', '', undef);
//        my $ns = '{DAV:}';
//        if (!defined $op) {
//            my @ops = keys %{$xmlref};
//            return buildExprFromBasicSearchWhereClause($ops[0], $$xmlref{$ops[0]}); 
//        }
//    
//        $op=~s/\Q$ns\E//;
//        $type='bool';
//    
//        if (ref($xmlref) eq 'ARRAY') {  
//            foreach my $oo (@{$xmlref}) {
//                my ($ne,$nt) = buildExprFromBasicSearchWhereClause($op, $oo, $superop);
//                my ($nes,$nts) = buildExprFromBasicSearchWhereClause($superop, undef, $superop);
//                $expr.= $nes if $expr ne "";
//                $expr.= "($ne)";
//            }
//            return $expr;
//        }
//    
//        study $op;
//        if ($op =~ /^(and|or)$/) {
//            if (ref($xmlref) eq 'HASH') {
//                foreach my $o (keys %{$xmlref}) {
//                    $expr .= $op eq 'and' ? ' && ' : ' || ' if $expr ne "";
//                    my ($ne, $nt) =  buildExprFromBasicSearchWhereClause($o, $$xmlref{$o}, $op);
//                    $expr .= "($ne)";
//                }
//            } else {
//                return $op eq 'and' ? ' && ' : ' || ';
//            }
//        } elsif ($op eq 'not') {
//            my @k = keys %{$xmlref};
//            my ($ne,$nt) = buildExprFromBasicSearchWhereClause($k[0], $$xmlref{$k[0]});
//            $expr="!($ne)";
//        } elsif ($op eq 'is-collection') {
//            $expr="getPropValue('{DAV:}iscollection',\$filename,\$request_uri)==1";
//        } elsif ($op eq 'is-defined') {
//            my ($ne,$nt)=buildExprFromBasicSearchWhereClause('{DAV:}prop',$$xmlref{'{DAV:}prop'});
//            $expr="$ne ne '__undef__'";
//        } elsif ($op =~ /^(language-defined|language-matches)$/) {
//            $expr='0!=0';
//        } elsif ($op =~ /^(eq|lt|gt|lte|gte)$/) {
//            my $o = $op;
//            my ($ne1,$nt1) = buildExprFromBasicSearchWhereClause('{DAV:}prop',$$xmlref{'{DAV:}prop'});
//            my ($ne2,$nt2) = buildExprFromBasicSearchWhereClause('{DAV:}literal', $$xmlref{'{DAV:}literal'});
//            $ne2 =~ s/'/\\'/sg;
//            $ne2 = $SEARCH_SPECIALCONV{$nt1} ? $SEARCH_SPECIALCONV{$nt1}."('$ne2')" : "'$ne2'";
//            my $cl= $$xmlref{'caseless'} || $$xmlref{'{DAV:}caseless'} || 'yes';
//            $expr = (($nt1 =~ /(string|xml)/ && $cl ne 'no')?"lc($ne1)":$ne1)
//                          . ' '.($SEARCH_SPECIALOPS{$nt1}{$o} || $o).' '
//                  . (($nt1 =~ /(string|xml)/ && $cl ne 'no')?"lc($ne2)":$ne2);
//        } elsif ($op eq 'like') {
//            my ($ne1,$nt1) = buildExprFromBasicSearchWhereClause('{DAV:}prop',$$xmlref{'{DAV:}prop'});
//            my ($ne2,$nt2) = buildExprFromBasicSearchWhereClause('{DAV:}literal', $$xmlref{'{DAV:}literal'});
//            $ne2=~s/\//\\\//gs;     ## quote slashes 
//            $ne2=~s/(?<!\\)_/./gs;  ## handle unescaped wildcard _ -> .
//            $ne2=~s/(?<!\\)%/.*/gs; ## handle unescaped wildcard % -> .*
//            my $cl= $$xmlref{'caseless'} || $$xmlref{'{DAV:}caseless'} || 'yes';
//            $expr = "$ne1 =~ /$ne2/s" . ($cl eq 'no'?'':'i');
//        } elsif ($op eq 'contains') {
//            my $content = ref($xmlref) eq "" ? $xmlref : $$xmlref{content};
//            my $cl = ref($xmlref) eq "" ? 'yes' : ($$xmlref{caseless} || $$xmlref{'{DAV:}caseless'} || 'yes');
//            $content=~s/\//\\\//g;
//            $expr="getFileContent(\$filename) =~ /\\Q$content\\E/s".($cl eq 'no'?'':'i');
//        } elsif ($op eq 'prop') {
//            my @props = keys %{$xmlref};
//            $props[0] =~ s/'/\\'/sg;
//            $expr = "getPropValue('$props[0]',\$filename,\$request_uri)";
//            $type = $SEARCH_PROPTYPES{$props[0]} || $SEARCH_PROPTYPES{default};
//            $expr = $SEARCH_SPECIALCONV{$type}."($expr)" if exists $SEARCH_SPECIALCONV{$type};
//        } elsif ($op eq 'literal') {
//            $expr = ref($xmlref) ne "" ? convXML2Str($xmlref) : $xmlref;
//            $type = $op;
//        } else {
//            $expr= $xmlref;
//            $type= $op;
//        }
//    
//        return ($expr, $type);
	    
	    // TODO: implement
		
	}
	
	public class IntegerRef {
	    int value;

	    public IntegerRef(int value) {
	        this.value = value;
	    }
	    
        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
        
        public void incValue(int value) {
            this.value += value;
        }        
	}

}
