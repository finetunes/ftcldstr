package net.finetunes.ftcldstr.routines.webdav;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.rendering.RenderingService;
import net.finetunes.ftcldstr.routines.fileoperations.DirectoryOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesHelper;
import net.finetunes.ftcldstr.routines.xml.XMLService;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;

public class SearchService {
	
	public static String getSearchResult(RequestParams requestParams, String search,
			String fn, String ru,
			boolean isRecursive, IntegerRef fullcount,
			ArrayList<String> visited) {
	    
	    String content = "";
		ConfigService.ALLOW_FILE_MANAGEMENT = false;
		
		// link loop detection:
		String nfn = FileOperationsService.full_resolve(requestParams, fn);
		
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
                content += ConfigService.stringMessages.get("searchnothingfound") + "'" + RenderingHelper.escapeHTML(search) + "'" + 
                    ConfigService.stringMessages.get("searchgoback") + RenderingService.getQuickNavPath(ru);
                content += "</h2>";
            }
            else {
                String cc = "<h2>";
                String pkey = "searchresultfor";
                if (fullcount.getValue() > 1) {
                    pkey = "searchresultsfor"; 
                }
                cc += fullcount.getValue() + " " + ConfigService.stringMessages.get(pkey) + "'" + RenderingHelper.escapeHTML(search) + "'" +
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
    public static Object[] buildExprFromBasicSearchWhereClause(
            String op, Object xmlref, String superop,
            RequestParams requestParams, String filename, String request_uri) {

        if (xmlref == null) {
            xmlref = new HashMap<String, Object>();
        }
        
        // FIXME
        // expr
        String type = null; // TODO: type?
        
        String ns = "{DAV:}";
        if (op == null) {
            ArrayList<String> ops;
            ops = new ArrayList<String>(((HashMap<String, Object>)xmlref).keySet());
            return buildExprFromBasicSearchWhereClause(ops.get(0), ((HashMap<String, Object>)xmlref).get(ops.get(0)), null,
                    null, null, null);
        }
        
        op = op.replaceFirst(Pattern.quote(ns), "");
        // type = Boolean.class; // "bool"
        type = "bool";
        
        if (xmlref instanceof ArrayList<?>) {
            Boolean result = null; 
            Iterator<Object> it = ((ArrayList)xmlref).iterator();
            while (it.hasNext()) {
                Object oo = it.next();
                Object[] r1 = buildExprFromBasicSearchWhereClause(op, oo, superop, null, null, null);
                Object[] r2 = buildExprFromBasicSearchWhereClause(superop, null, superop, null, null, null);
                
                if (result == null) {
                    result = (Boolean)r1[0];
                }
                else {
                    String opr = r2[0].toString();
                    if (opr.equalsIgnoreCase("and")) {
                        result = result && (Boolean)r1[0];
                    }
                    else {
                        result = result && (Boolean)r1[0];
                    }
                }
            }
            return new Object[]{result, null};
        }
        
        if (op.matches("(and|or)")) {
            if (xmlref instanceof HashMap<?, ?>) {
                Boolean result = null;
                ArrayList<String> keys = new ArrayList<String>(((HashMap) xmlref).keySet());
                Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String o = it.next();
                    Object[] r1 = buildExprFromBasicSearchWhereClause(o, ((HashMap)xmlref).get(o), op, null, null, null);
                    
                    if (result == null) {
                        result = (Boolean)r1[0];
                    }
                    else {
                        if (op.equalsIgnoreCase("and")) {
                            result = result && (Boolean)r1[0];
                        }
                        else {
                            result = result && (Boolean)r1[0];
                        }
                    }                        
                }
                return new Object[]{result, null};
            }
            else {
                if (op.equalsIgnoreCase("and")) {
                    return new Object[]{"and", null};
                }
                return new Object[]{"or", null};
            }
        }
        else if (op.equals("not")) {
            ArrayList<String> k = new ArrayList<String>(((HashMap) xmlref).keySet());
            Object[] r1 = buildExprFromBasicSearchWhereClause(k.get(0), ((HashMap)xmlref).get(k.get(0)), null, null, null, null);
            return new Object[]{!((Boolean)r1[0]), null};
        }
        else if (op.equals("is-collection")) {
            Object p = PropertiesHelper.getPropValue(requestParams, "{DAV:}iscollection", filename, request_uri);
            Boolean result = (((Integer)(p)) == 1);
            return new Object[]{result, null};
        }
        else if (op.equals("is-defined")) {
            Object[] r1 = buildExprFromBasicSearchWhereClause("{DAV:}prop", ((HashMap)xmlref).get("{DAV:}prop"), null, null, null, null);
            Boolean result = null;
            if (r1[0] != null) {
                result = !r1[0].toString().equals("__undef__");
            }
            return new Object[]{result, null};
        }
        else if (op.matches("(language-defined|language-matches)")) {
            return new Object[]{false, null};
        }
        else if (op.matches("(eq|lt|gt|lte|gte)")) {
            String o = op;
            Object[] r1 = buildExprFromBasicSearchWhereClause("{DAV:}prop", ((HashMap)xmlref).get("{DAV:}prop"), null, null, null, null);
            Object[] r2 = buildExprFromBasicSearchWhereClause("{DAV:}literal", ((HashMap)xmlref).get("{DAV:}literal"), null, null, null, null);
            Object ne2 = (String)r2[0];
            Object nt1 = (String)r1[1];
            // ne2 = ne2.replaceAll("'", "\\'"); // not needed for java
            if (nt1.equals("dateTime")) {
                ne2 = RenderingHelper.parseHTTPDate((String)ne2);
            }
            else if (nt1.equals("xml")) {
                ne2 = XMLService.convXML2Str((HashMap<String, Object>)ne2);
            }
            else {
                // don't change anything
            }
            // $ne2 = $SEARCH_SPECIALCONV{$nt1} ? $SEARCH_SPECIALCONV{$nt1}."('$ne2')" : "'$ne2'"; // not needed for java
            
            boolean caseless = false;
            if (((HashMap)xmlref).get("caseless") != null &&
                    ((HashMap)xmlref).get("caseless").toString().equalsIgnoreCase("yes")) {
                caseless = true;
            }
            else if (((HashMap)xmlref).get("{DAV:}caseless") != null &&
                    ((HashMap)xmlref).get("{DAV:}caseless").toString().equalsIgnoreCase("yes")) {
                caseless = true;
            }
            
            Object arg1 = r1[0];
            Object arg2 = r2[0];
            if (r1[1].toString().matches("(?s).*(string|xml).*") && caseless) {
                arg1 = arg1.toString().toLowerCase();
                arg2 = arg1.toString().toLowerCase();
            }
            
            if (nt1.equals("int")) {
                Boolean result = null;
                if (op.equals("eq")) {
                    result = ((Integer)arg1) == ((Integer)arg2); 
                }
                else if (op.equals("lt")) {
                    result = ((Integer)arg1) < ((Integer)arg2); 
                }
                else if (op.equals("gt")) {
                    result = ((Integer)arg1) > ((Integer)arg2); 
                }
                else if (op.equals("lte")) {
                    result = ((Integer)arg1) <= ((Integer)arg2); 
                }
                else if (op.equals("gte")) {
                    result = ((Integer)arg1) >= ((Integer)arg2); 
                }
                return new Object[]{result, null};
            }
            else if (nt1.equals("date")) {
                Boolean result = null;
                if (op.equals("eq")) {
                    result = ((Date)arg1).equals((Date)arg2); 
                }
                else if (op.equals("lt")) {
                    result = ((Date)arg1).before((Date)arg2); 
                }
                else if (op.equals("gt")) {
                    result = ((Date)arg1).after((Date)arg2); 
                }
                else if (op.equals("lte")) {
                    result = !((Date)arg1).after((Date)arg2); 
                }
                else if (op.equals("gte")) {
                    result = !((Date)arg1).before((Date)arg2); 
                }
                return new Object[]{result, null};
            }
            else {
                // strings
                Boolean result = null;
                if (op.equals("eq")) {
                    result = (arg1.toString().equals(arg2));
                }
                else if (op.equals("lt")) {
                    result = (arg1.toString().compareTo(arg2.toString()) < 0);
                }
                else if (op.equals("gt")) {
                    result = (arg1.toString().compareTo(arg2.toString()) > 0);
                }                
                else if (op.equals("lte")) {
                    result = !(arg1.toString().compareTo(arg2.toString()) > 0);
                }
                else if (op.equals("gte")) {
                    result = !(arg1.toString().compareTo(arg2.toString()) < 0);
                }                
                return new Object[]{result, null};
            }
        }
        else if (op.equals("like")) {
            Object[] r1 = buildExprFromBasicSearchWhereClause("{DAV:}prop", ((HashMap)xmlref).get("{DAV:}prop"), null, null, null, null);
            Object[] r2 = buildExprFromBasicSearchWhereClause("{DAV:}literal", ((HashMap)xmlref).get("{DAV:}literal"), null, null, null, null);
            String ne1 = r1[0].toString();
            String ne2 = r2[0].toString();
//          $ne2=~s/\//\\\//gs;     ## quote slashes 
//          $ne2=~s/(?<!\\)_/./gs;  ## handle unescaped wildcard _ -> .
//          $ne2=~s/(?<!\\)%/.*/gs; ## handle unescaped wildcard % -> .*
            
            ne2 = ne2.replaceAll("(?<!\\)_", "."); // ## handle unescaped wildcard _ -> .
            ne2 = ne2.replaceAll("(?<!\\)%", ".*"); // ## handle unescaped wildcard % -> .*
            
            boolean caseless = false;
            if (((HashMap)xmlref).get("caseless") != null &&
                    ((HashMap)xmlref).get("caseless").toString().equalsIgnoreCase("yes")) {
                caseless = true;
            }
            else if (((HashMap)xmlref).get("{DAV:}caseless") != null &&
                    ((HashMap)xmlref).get("{DAV:}caseless").toString().equalsIgnoreCase("yes")) {
                caseless = true;
            }
            
            String flags = "s";
            if (caseless) {
                flags += "i";
            }
            Boolean result = ne1.matches("(?" + flags + ")" + ne2);
            return new Object[]{result, null};
        }
        else if (op.matches("contains")) {
            String content;
            if (xmlref != null && ((HashMap<String, Object>)xmlref).get("content") != null) {
                content = ((HashMap<String, Object>)xmlref).get("content").toString();
            }
            else {
                content = xmlref.toString();
            }
            
            boolean caseless = false;
            if (((HashMap)xmlref).get("caseless") != null &&
                    ((HashMap)xmlref).get("caseless").toString().equalsIgnoreCase("yes")) {
                caseless = true;
            }
            else if (((HashMap)xmlref).get("{DAV:}caseless") != null &&
                    ((HashMap)xmlref).get("{DAV:}caseless").toString().equalsIgnoreCase("yes")) {
                caseless = true;
            }          
            
            String fileContent = FileOperationsService.getFileContent(requestParams, filename);
            String flags = "s";
            if (caseless) {
                flags += "i";
            }
            
            Boolean result = fileContent.matches("(?" + flags + ").*" + Pattern.quote(content) + ".*");
            return new Object[]{result, null};
        }
        // TODO finish and check
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
        
        return null;
        
    }    
    
	
//	// TODO: define xmlref type and return type
//	public static void buildExprFromBasicSearchWhereClause(
//			String operator, /*xmlref, */ String superOperator) {
//
//    	    
////        my ($op, $xmlref, $superop) = @_;
////        my ($expr,$type) = ( '', '', undef);
////        my $ns = '{DAV:}';
////        if (!defined $op) {
////            my @ops = keys %{$xmlref};
////            return buildExprFromBasicSearchWhereClause($ops[0], $$xmlref{$ops[0]}); 
////        }
////    
////        $op=~s/\Q$ns\E//;
////        $type='bool';
////    
////        if (ref($xmlref) eq 'ARRAY') {  
////            foreach my $oo (@{$xmlref}) {
////                my ($ne,$nt) = buildExprFromBasicSearchWhereClause($op, $oo, $superop);
////                my ($nes,$nts) = buildExprFromBasicSearchWhereClause($superop, undef, $superop);
////                $expr.= $nes if $expr ne "";
////                $expr.= "($ne)";
////            }
////            return $expr;
////        }
////    
////        study $op;
////        if ($op =~ /^(and|or)$/) {
////            if (ref($xmlref) eq 'HASH') {
////                foreach my $o (keys %{$xmlref}) {
////                    $expr .= $op eq 'and' ? ' && ' : ' || ' if $expr ne "";
////                    my ($ne, $nt) =  buildExprFromBasicSearchWhereClause($o, $$xmlref{$o}, $op);
////                    $expr .= "($ne)";
////                }
////            } else {
////                return $op eq 'and' ? ' && ' : ' || ';
////            }
////        } elsif ($op eq 'not') {
////            my @k = keys %{$xmlref};
////            my ($ne,$nt) = buildExprFromBasicSearchWhereClause($k[0], $$xmlref{$k[0]});
////            $expr="!($ne)";
////        } elsif ($op eq 'is-collection') {
////            $expr="getPropValue('{DAV:}iscollection',\$filename,\$request_uri)==1";
////        } elsif ($op eq 'is-defined') {
////            my ($ne,$nt)=buildExprFromBasicSearchWhereClause('{DAV:}prop',$$xmlref{'{DAV:}prop'});
////            $expr="$ne ne '__undef__'";
////        } elsif ($op =~ /^(language-defined|language-matches)$/) {
////            $expr='0!=0';
////        } elsif ($op =~ /^(eq|lt|gt|lte|gte)$/) {
////            my $o = $op;
////            my ($ne1,$nt1) = buildExprFromBasicSearchWhereClause('{DAV:}prop',$$xmlref{'{DAV:}prop'});
////            my ($ne2,$nt2) = buildExprFromBasicSearchWhereClause('{DAV:}literal', $$xmlref{'{DAV:}literal'});
////            $ne2 =~ s/'/\\'/sg;
////            $ne2 = $SEARCH_SPECIALCONV{$nt1} ? $SEARCH_SPECIALCONV{$nt1}."('$ne2')" : "'$ne2'";
////            my $cl= $$xmlref{'caseless'} || $$xmlref{'{DAV:}caseless'} || 'yes';
////            $expr = (($nt1 =~ /(string|xml)/ && $cl ne 'no')?"lc($ne1)":$ne1)
////                          . ' '.($SEARCH_SPECIALOPS{$nt1}{$o} || $o).' '
////                  . (($nt1 =~ /(string|xml)/ && $cl ne 'no')?"lc($ne2)":$ne2);
////        } elsif ($op eq 'like') {
////            my ($ne1,$nt1) = buildExprFromBasicSearchWhereClause('{DAV:}prop',$$xmlref{'{DAV:}prop'});
////            my ($ne2,$nt2) = buildExprFromBasicSearchWhereClause('{DAV:}literal', $$xmlref{'{DAV:}literal'});
////            $ne2=~s/\//\\\//gs;     ## quote slashes 
////            $ne2=~s/(?<!\\)_/./gs;  ## handle unescaped wildcard _ -> .
////            $ne2=~s/(?<!\\)%/.*/gs; ## handle unescaped wildcard % -> .*
////            my $cl= $$xmlref{'caseless'} || $$xmlref{'{DAV:}caseless'} || 'yes';
////            $expr = "$ne1 =~ /$ne2/s" . ($cl eq 'no'?'':'i');
////        } elsif ($op eq 'contains') {
////            my $content = ref($xmlref) eq "" ? $xmlref : $$xmlref{content};
////            my $cl = ref($xmlref) eq "" ? 'yes' : ($$xmlref{caseless} || $$xmlref{'{DAV:}caseless'} || 'yes');
////            $content=~s/\//\\\//g;
////            $expr="getFileContent(\$filename) =~ /\\Q$content\\E/s".($cl eq 'no'?'':'i');
////        } elsif ($op eq 'prop') {
////            my @props = keys %{$xmlref};
////            $props[0] =~ s/'/\\'/sg;
////            $expr = "getPropValue('$props[0]',\$filename,\$request_uri)";
////            $type = $SEARCH_PROPTYPES{$props[0]} || $SEARCH_PROPTYPES{default};
////            $expr = $SEARCH_SPECIALCONV{$type}."($expr)" if exists $SEARCH_SPECIALCONV{$type};
////        } elsif ($op eq 'literal') {
////            $expr = ref($xmlref) ne "" ? convXML2Str($xmlref) : $xmlref;
////            $type = $op;
////        } else {
////            $expr= $xmlref;
////            $type= $op;
////        }
////    
////        return ($expr, $type);
//	    
//	    // TODO: implement
//		
//	}
	
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
