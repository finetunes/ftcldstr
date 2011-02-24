package net.finetunes.ftcldstr.routines.webdav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.routines.fileoperations.BasicSearch;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService.StatData;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesActions;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesHelper;
import net.finetunes.ftcldstr.routines.webdav.properties.StatusResponse;
import net.finetunes.ftcldstr.routines.xml.XMLService;

public class SearchHandler {

	public static void handleBasicSearch(RequestParams requestParams,
	        HashMap<String, Object> xmldata,
	        ArrayList<HashMap<String, Object>> resps,
	        ArrayList<HashMap<String, Object>> error) {

	    if (xmldata == null) {
	        xmldata = new HashMap<String, Object>();
	    }
	    
	    // select > (allprop | prop)  
        Object[] propFindElement = PropertiesActions.handlePropFindElement(requestParams, (HashMap<String, Object>)xmldata.get("{DAV:}select"));
        ArrayList<String> propsref = (ArrayList<String>)propFindElement[0];
        boolean all = ((Boolean)propFindElement[1]).booleanValue();
        boolean noval = ((Boolean)propFindElement[2]).booleanValue();
        
        // where > op > (prop,literal) 
        // Logger.debug("SEARCH: call buildExpr: expr=");
	    
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
        ArrayList<HashMap<String, Object>> matches = new ArrayList<HashMap<String,Object>>();
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
            
            BasicSearch.doBasicSearch(requestParams, xmldata.get("{DAV:}where"), base, href, depth, limit, matches, null,
                    null, xmldata.get("{DAV:}where"), null);
        }
        
        // orderby > order+ (caseless=(yes|no))> (prop|score), (ascending|descending)?
        
        MatchComparator comp = new MatchComparator(requestParams);
        if (xmldata.containsKey("{DAV:}orderby") && matches != null && matches.size() > 1) {
            ArrayList<HashMap<String, Object>> orders = new ArrayList<HashMap<String,Object>>();
            
            HashMap<String, Object> orderby = null;
            if (xmldata.get("{DAV:}orderby") instanceof HashMap<?, ?>) {
                orderby = (HashMap<String, Object>)xmldata.get("{DAV:}orderby");
                if (orderby != null) {
                    Object ord = orderby.get("{DAV:}order");
                    if (ord != null) {
                        if (ord instanceof ArrayList<?>) {
                            orders.addAll((ArrayList<HashMap<String, Object>>)ord);
                        }
                        else if (ord instanceof HashMap<?, ?>) {
                            orders.add((HashMap<String, Object>)ord);
                        }
                    }
                    
                }
            }
            
            Iterator<HashMap<String, Object>> itr = orders.iterator();
            while (itr.hasNext()) {
                HashMap<String, Object> order = itr.next();
                
                HashMap<String, Object> p = (HashMap<String, Object>)order.get("{DAV:}prop");
                if (p != null) {
                    Set<String> keys = p.keySet();
                    ArrayList<String> props = new ArrayList<String>(keys);
                    String prop = null;
                    if (props.size() > 0) {
                        prop = props.get(0);
                    }
                    
                    if (prop == null) {
                        prop = "{DAV:}displayname";
                    }
                    
                    String proptype = ConfigService.SEARCH_PROPTYPES.get(prop);
                    if (proptype == null) {
                        proptype = ConfigService.SEARCH_PROPTYPES.get("default");
                    }
                    
                    String type;
                    if (order.get("{DAV:}descending") != null) {
                        type = "descending";
                    }
                    else {
                        type = "ascending";
                    }
                    
                    Logger.debug("orderby: prop=" + prop + ", proptype=" + proptype + ", type=" + type);
                    comp.addCondition(prop, proptype, type);
                }
            }
            Logger.debug("orderby: sortfunc=$sortfunc");
        }
        
        int matchesCount = 0;
        if (matches != null) {
            matchesCount = matches.size();
        }
        Logger.debug("handleBasicSearch: matches=" + matchesCount);

        if (comp != null) {
            Collections.sort(matches, comp);
        }
        Iterator<HashMap<String, Object>> itm = matches.iterator();
        while (itm.hasNext()) {
            HashMap<String, Object> match = itm.next();
            HashMap<String, Object> resp = new HashMap<String, Object>();
            resp.put("href", match.get("href"));
            resp.put("propstat", StatusResponse.statusResponseListToHashMap(PropertiesHelper.getPropStat(requestParams, (String)match.get("fn"), (String)match.get("href"), propsref, all, noval)));
            resps.add(resp);
        }
	}

}

class MatchComparator implements Comparator<HashMap<String, Object>> {
    
    private RequestParams requestParams;
    private ArrayList<HashMap<String, String>> cond = new ArrayList<HashMap<String,String>>(); 
    
    public MatchComparator(RequestParams requestParams) {
        this.requestParams = requestParams;
    }
    
    public void addCondition(String prop, String proptype, String type) {
        HashMap<String, String> c = new HashMap<String, String>();
        c.put("prop", prop);
        c.put("proptype", proptype);
        c.put("type", type);
        cond.add(c);
    }
    
    public int compare(HashMap<String, Object> a, HashMap<String, Object> b) {
        
        if (cond.size() == 0) {
            return 0;
        }
        
        Iterator<HashMap<String, String>> it = cond.iterator();
        while (it.hasNext()) {
            HashMap<String, String> c = it.next();
            int cc = checkCondition(a, b, c);
            if (cc != 0) {
                return cc;
            }
        }
        
        return 0;
    }
    
    private int checkCondition(HashMap<String, Object> a, HashMap<String, Object> b,
            HashMap<String, String> condition) {
        
        String prop = condition.get("prop");
        String proptype = condition.get("proptype");
        if (proptype == null) {
            proptype = "";
        }
        String type = condition.get("type");
        Object propa = PropertiesHelper.getPropValue(requestParams, prop, (String)a.get("fn"), (String)a.get("href"));
        Object propb = PropertiesHelper.getPropValue(requestParams, prop, (String)b.get("fn"), (String)b.get("href"));
        String pca;
        String pcb;
        Date pda;
        Date pdb;
        
        int asc = 1; // asc by default
        if (type.equals("ascending")) {
            asc = 1;
        }
        if (type.equals("descending")) {
            asc = -1;
        }        
        
        if (proptype.equals("dateTime")) {
            pda = RenderingHelper.parseHTTPDate((String)propa);
            pdb = RenderingHelper.parseHTTPDate((String)propb);
            return asc * pda.compareTo(pdb);
        }
        else if (proptype.equals("xml")) {
            pca = XMLService.convXML2Str((HashMap<String, Object>)propa);
            pcb = XMLService.convXML2Str((HashMap<String, Object>)propb);
            return asc * pca.compareTo(pcb);
        }
        else if (proptype.equals("int")) {
            return asc * (Integer.parseInt((String)propa) - Integer.parseInt((String)propb));
        }
        else {
            return asc * propa.toString().compareTo(propb.toString());
        }
    }
}

