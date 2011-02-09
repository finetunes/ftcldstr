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
import net.finetunes.ftcldstr.routines.fileoperations.DirectoryOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesActions;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesHelper;
import net.finetunes.ftcldstr.routines.webdav.properties.StatusResponse;
import net.finetunes.ftcldstr.routines.xml.XMLParser;
import net.finetunes.ftcldstr.routines.xml.XMLService;

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
        
        String fn = requestParams.getPathTranslated();
        String ru = requestParams.getRequestURI();
        
        String depths = requestParams.getHeader("Depth");
        if (depths == null || depths.isEmpty()) {
            depths = "0";
        }
        
        if (depths.matches("(?i).*infinity.*")) {
            depths = "-1";
        }
        
        int depth = 0;
        if (depths != null) {
            try {
                depth = Integer.valueOf(depths);
            }
            catch (NumberFormatException e) {
                Logger.log("Exception: Invaliv depth value: " + depths);
            }
        }
        
        Logger.debug("REPORT(" + fn + "," + ru + ")");
        String status = "200 OK";
        String content = "";
        String type = "";
        HashMap error;
        String xml = requestParams.getRequestBody();
        HashMap<String, Object> xmldata = null;
        XMLParser xmlParser = new XMLParser();
        xmldata = xmlParser.simpleXMLParser(xml, ConfigService.CHARSET, true);
        if (xmldata == null || xmldata.size() == 0) {
            Logger.debug("REPORT: invalid XML request: " + xml);
            // Logger.debug("REPORT: xml-request: " + xml);
            status = "400 Bad Request";
            type = "text/plain";
            content = "400 Bad Request";
        }
        else if (!FileOperationsService.file_exits(fn)) {
            status = "404 Not Found";
            type = "text/plain";
            content = "404 Not Found";
        }
        else {
            // MUST CalDAV: DAV:expand-property
            status = "207 Multi-Status";
            type = "application/xml";
            ArrayList<HashMap<String, Object>> resps = new ArrayList<HashMap<String,Object>>();
            ArrayList<String> hrefs = new ArrayList<String>();
            String rn = null;
            
            Set<String> reports = xmldata.keySet();
            if (reports.size() > 0) {
                Logger.debug("REPORT: report=" + reports.toArray()[0]);
            }
            
            if (xmldata.containsKey("{DAV:}acl-principal-prop-set")) {
                ArrayList<String> props = new ArrayList<String>();
                PropertiesActions.handlePropElement(requestParams, 
                        (HashMap<String, Object>)((HashMap<String, Object>)xmldata.get("{DAV:}acl-principal-prop-set")).get("{DAV:}prop"), props);
                HashMap<String, Object> resp = new HashMap<String, Object>();
                resp.put("href", ru);
                resp.put("propstat", StatusResponse.statusResponseListToHashMap(PropertiesHelper.getPropStat(requestParams, fn, ru, props)));
                resps.add(resp);
            }
            else if (xmldata.containsKey("{DAV:}principal-match")) {
                if (depth != 0) {
                    OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
                    return;
                }
                
                // response, href
                ArrayList<String> props = new ArrayList<String>();
                if (xmldata.get("{DAV:}principal-match") != null && ((HashMap<String, Object>)xmldata.get("{DAV:}principal-match")).containsKey("{DAV:}prop")) {
                    PropertiesActions.handlePropElement(requestParams, 
                            (HashMap<String, Object>)((HashMap<String, Object>)xmldata.get("{DAV:}principal-match")).get("{DAV:}prop"), props);
                }
                
                ArrayList<StatusResponse> respsRef = new ArrayList<StatusResponse>();
                DirectoryOperationsService.readDirRecursive(requestParams, fn, ru, respsRef, props, false, false, 1, true);
                if (respsRef != null && !respsRef.isEmpty()) {
                    resps.addAll(StatusResponse.statusResponseListToHashMap(respsRef));
                }
            }
            else if (xmldata.containsKey("{DAV:}principal-property-search")) {
                if (depth != 0) {
                    OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
                    return;
                }
                
                ArrayList<String> props = new ArrayList<String>();
                
                if (xmldata.get("{DAV:}principal-property-search") != null && 
                        ((HashMap<String, Object>)xmldata.get("{DAV:}principal-property-search")).get("{DAV:}prop") != null) {
                    PropertiesActions.handlePropElement(requestParams, (HashMap<String, Object>)((HashMap<String, Object>)xmldata.get("{DAV:}principal-property-search")).get("{DAV:}prop"), props);
                }
                
                ArrayList<StatusResponse> respsRef = new ArrayList<StatusResponse>();
                DirectoryOperationsService.readDirRecursive(requestParams, fn, ru, respsRef, props, false, false, 1, true);
                if (respsRef != null && !respsRef.isEmpty()) {
                    resps.addAll(StatusResponse.statusResponseListToHashMap(respsRef));
                }   
                
                // XX filter data
                
                ArrayList<HashMap<String, Object>> propertysearch = new ArrayList<HashMap<String,Object>>();
                if (((HashMap<String, Object>)xmldata.get("{DAV:}principal-property-search")).get("{DAV:}property-search") instanceof HashMap<?, ?>) {
                    propertysearch.add((HashMap<String, Object>)((HashMap<String, Object>)xmldata.get("{DAV:}principal-property-search")).get("{DAV:}property-search"));
                }
                else if (((HashMap<String, Object>)xmldata.get("{DAV:}principal-property-search")).get("{DAV:}property-search") instanceof ArrayList<?>) {
                    propertysearch.addAll((ArrayList<HashMap<String, Object>>)((HashMap<String, Object>)xmldata.get("{DAV:}principal-property-search")).get("{DAV:}property-search"));
                }
            }
            else if (xmldata.containsKey("{DAV:}principal-property-search")) {
                HashMap<String, Object> resp = new HashMap<String, Object>();
                HashMap<String, Object> prop = new HashMap<String, Object>();
                prop.put("displayname", null);
                HashMap<String, Object> principalsearchitem = new HashMap<String, Object>();
                principalsearchitem.put("prop", prop);
                principalsearchitem.put("description", "Full name");
                
                ArrayList<HashMap<String, Object>> principalsearch = new ArrayList<HashMap<String,Object>>();
                principalsearch.add(principalsearchitem);
                resp.put("principal-search-property-set", principalsearch);
                
                content = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, resp);
                status = "200 OK";
                type = "text/xml";
            }
            else if (xmldata.containsKey("{urn:ietf:params:xml:ns:caldav}free-busy-query")) {
                status = "200 OK";
                type = "text/calendar";
                content = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Example Corp.//CalDAV Server//EN\r\nBEGIN:VFREEBUSY\r\nEND:VFREEBUSY\r\nEND:VCALENDAR";
            }
            else if (xmldata.containsKey("{urn:ietf:params:xml:ns:caldav}calendar-query")) { // ## missing filter
                rn = "{urn:ietf:params:xml:ns:caldav}calendar-query";
                DirectoryOperationsService.readDirBySuffix(requestParams, fn, ru, hrefs, "ics", depth);
            }
            else if (xmldata.containsKey("{urn:ietf:params:xml:ns:caldav}calendar-multiget")) { // ## OK - complete
                rn = "{urn:ietf:params:xml:ns:caldav}calendar-multiget";
                
                if (!(xmldata.get(rn) != null && ((HashMap<String, Object>)xmldata.get(rn)).containsKey("{DAV:}href")) ||
                        !(xmldata.get(rn) != null && ((HashMap<String, Object>)xmldata.get(rn)).containsKey("{DAV:}prop"))) {
                    OutputService.printHeaderAndContent(requestParams, "404 Bad Request");
                    return;
                }
                if (((HashMap<String, Object>)xmldata.get(rn)).get("{DAV:}href") instanceof ArrayList<?>) {
                    hrefs.addAll((ArrayList<String>)((HashMap<String, Object>)xmldata.get(rn)).get("{DAV:}href"));
                }
                else {
                    hrefs.add((String)((HashMap<String, Object>)xmldata.get(rn)).get("{DAV:}href"));
                }
            }
            else if (xmldata.containsKey("{urn:ietf:params:xml:ns:carddav}addressbook-query")) {
                rn = "{urn:ietf:params:xml:ns:carddav}addressbook-query";
                DirectoryOperationsService.readDirBySuffix(requestParams, fn, ru, hrefs, "vcf", depth);
            }
            else if (xmldata.containsKey("{urn:ietf:params:xml:ns:carddav}addressbook-multiget")) {
                rn = "{urn:ietf:params:xml:ns:carddav}addressbook-multiget";
                
                if (!(xmldata.get(rn) != null && ((HashMap<String, Object>)xmldata.get(rn)).containsKey("{DAV:}href")) ||
                        !(xmldata.get(rn) != null && ((HashMap<String, Object>)xmldata.get(rn)).containsKey("{DAV:}prop"))) {
                    OutputService.printHeaderAndContent(requestParams, "404 Bad Request");
                    return;
                }
                if (((HashMap<String, Object>)xmldata.get(rn)).get("{DAV:}href") instanceof ArrayList<?>) {
                    hrefs.addAll((ArrayList<String>)((HashMap<String, Object>)xmldata.get(rn)).get("{DAV:}href"));
                }
                else {
                    hrefs.add((String)((HashMap<String, Object>)xmldata.get(rn)).get("{DAV:}href"));
                }                
            }
            else {
                status = "400 Bad Request";
                type = "text/plain";
                content = "400 Bad Request";
            }
            
            if (rn != null && !rn.isEmpty()) {
                Iterator<String> it = hrefs.iterator();
                while (it.hasNext()) {
                    String href = it.next();
                    StatusResponse resp_200 = new StatusResponse();
                    StatusResponse resp_404 = new StatusResponse();
                    
                    resp_200.setStatus("HTTP/1.1 200 OK");
                    resp_404.setStatus("HTTP/1.1 404 Not Found");
                    String nhref = new String(href);
                    nhref = nhref.replaceFirst(ConfigService.VIRTUAL_BASE, "");
                    String nfn = ConfigService.DOCUMENT_ROOT + nhref;
                    Logger.debug("REPORT: nfn=" + nfn + ", href=" + href);
                    
                    if (!FileOperationsService.file_exits(nfn)) {
                        HashMap<String, Object> resp = new HashMap<String, Object>();
                        resp.put("href", href);
                        resp.put("status", "HTTP/1.1 404 Not Found");
                        resps.add(resp);
                        continue;
                    }
                    else if (FileOperationsService.is_directory(nfn)) {
                        HashMap<String, Object> resp = new HashMap<String, Object>();
                        resp.put("href", href);
                        resp.put("status", "HTTP/1.1 403 Forbidden");
                        resps.add(resp);                        
                        continue;
                    }
                    
                    ArrayList<String> props = new ArrayList<String>();
                    if (xmldata.get(rn) != null && ((HashMap<String, Object>)xmldata.get(rn)).get("{DAV:}prop") != null) {
                        PropertiesActions.handlePropElement(requestParams, 
                                (HashMap<String, Object>)((HashMap<String, Object>)xmldata.get(rn)).get("{DAV:}prop"), props);
                    }
                    HashMap<String, Object> resp = new HashMap<String, Object>();
                    resp.put("href", href);
                    resp.put("propstat", StatusResponse.statusResponseListToHashMap(PropertiesHelper.getPropStat(requestParams, nfn, nhref, props)));
                    resps.add(resp);
                }
                // ### push @resps, { } if ($#hrefs==-1);  ## empty multistatus response not supported
                
            }
            
            if (resps.size() > 0) {
                HashMap<String, Object> multistatus = new HashMap<String, Object>();
                
                if (resps.size() > 0) {
                    HashMap<String, Object> response = new HashMap<String, Object>();
                    response.put("response", resps);
                    multistatus.put("multistatus", response);
                }
                else {
                    multistatus.put("multistatus", null);
                }
                content = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, multistatus);
            }
        }
    
        Logger.debug("REPORT: REQUEST: " + xml);
        Logger.debug("REPORT: RESPONSE: " + content);
        OutputService.printHeaderAndContent(requestParams, status, type, content);
    }
}
