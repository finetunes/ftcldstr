package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.webdav.SearchHandler;
import net.finetunes.ftcldstr.routines.xml.XMLParser;
import net.finetunes.ftcldstr.routines.xml.XMLService;

/**
 * The client invokes the SEARCH method to initiate a server-side
 * search.  The body of the request defines the query.  The server MUST
 * emit an entity matching the WebDAV multistatus format ([RFC4918],
 * Section 13).
 * 
 * The SEARCH method plays the role of transport mechanism for the query
 * and the result set.  It does not define the semantics of the query.
 * The type of the query defines the semantics.
 * 
 * Description from RFC 5323 (C) 2008 IETF Trust and the persons 
 * identified as the document authors.
 * http://www.ietf.org/rfc/rfc5323.txt
 * 
 */

public class SearchActionHandler extends AbstractActionHandler {
    
    public void handle(final RequestParams requestParams) {
        
        ArrayList<HashMap<String, Object>> resps = new ArrayList<HashMap<String, Object>>();
        String status = "HTTP/1.1 207 Multistatus";
        String content = "";
        String type = "application/xml";
        ArrayList<HashMap<String, Object>> errors = new ArrayList<HashMap<String, Object>>();
        
        String xml = requestParams.getRequestBody();
        XMLParser xmlParser = new XMLParser();
        HashMap<String, Object> xmldata = xmlParser.simpleXMLParser(xml, ConfigService.CHARSET, true);
        if (xmldata == null || xmldata.size() == 0) {
            Logger.debug("SEARCH: invalid XML request: " + xml);
            status = "400 Bad Request";
            type = "text/plain";
            content = "400 Bad Request";
        }
        else if (xmldata.containsKey("{DAV:}query-schema-discovery")) {
            Logger.debug("SEARCH: found query-schema-discovery");
            HashMap<String, Object> resp = new HashMap<String, Object>();
            resp.put("href", requestParams.getRequestURI());
            resp.put("status", status);
            ArrayList<HashMap<String, Object>> properties = new ArrayList<HashMap<String,Object>>();
            HashMap<String, Object> propdesc1 = new HashMap<String, Object>();
            propdesc1.put("any-other-property", null);
            propdesc1.put("searchable", null);
            propdesc1.put("selectable", null);
            propdesc1.put("caseless", null);
            propdesc1.put("sortable", null);
            properties.add(propdesc1);
            
            ArrayList<HashMap<String, Object>> opdesc = new ArrayList<HashMap<String,Object>>();
            HashMap<String, Object> opdesc1 = new HashMap<String, Object>();
            opdesc1.put("like", null);
            opdesc1.put("operand-property", null);
            opdesc1.put("operand-literal", null);
            opdesc.add(opdesc1);
            HashMap<String, Object> opdesc2 = new HashMap<String, Object>();
            opdesc2.put("contains", null);
            opdesc.add(opdesc2);            
  
            HashMap<String, Object> operators = new HashMap<String, Object>();
            operators.put("opdesc allow-pcdata=\"yes\"", opdesc);

            HashMap<String, Object> basicsearchschema = new HashMap<String, Object>();
            basicsearchschema.put("properties", properties);
            basicsearchschema.put("operators", operators);

            resp.put("query-schema", basicsearchschema);
        }
        else if (xmldata.containsKey("{DAV:}searchrequest")) {
            if (xmldata.get("{DAV:}searchrequest") != null && xmldata.get("{DAV:}searchrequest") instanceof HashMap<?, ?>) {
                HashMap<String, Object> searchrequest = (HashMap<String, Object>)xmldata.get("{DAV:}searchrequest");
                Set<String> keys = searchrequest.keySet();
                Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String s = it.next();
                    if (s.matches("(?s).*{DAV:}basicsearch.*")) {
                        HashMap<String, Object> sr = (HashMap<String, Object>)searchrequest.get(s);
                        SearchHandler.handleBasicSearch(requestParams, sr, resps, errors);
                    }
                }
            }
        }
        
        if (errors.size() > 0) {
            HashMap<String, Object> e = new HashMap<String, Object>();
            e.put("error", errors);
            content = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, e);
            status = "409 Conflict";
        }
        else if (resps.size() > 0) {
            HashMap<String, Object> response = new HashMap<String, Object>();
            response.put("response", resps);
            HashMap<String, Object> m = new HashMap<String, Object>();
            m.put("multistatus", response);
            content = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, m); 
        }
        else {
            HashMap<String, Object> response = new HashMap<String, Object>();
            response.put("href", requestParams.getRequestURI());
            response.put("status", "404 Not Found");
            HashMap<String, Object> m = new HashMap<String, Object>();
            m.put("multistatus", response);
            content = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, m);
        }
        
        OutputService.printHeaderAndContent(requestParams, status, type, content);
    }
}
