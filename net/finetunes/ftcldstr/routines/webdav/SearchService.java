package net.finetunes.ftcldstr.routines.webdav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.rendering.RenderingService;
import net.finetunes.ftcldstr.routines.fileoperations.DirectoryOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.wrappers.ReadDirectoryContentWrapper;

import org.w3c.dom.Element;

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
        
        ArrayList<String> files = ReadDirectoryContentWrapper.getFileList(fn);
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
