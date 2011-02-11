package net.finetunes.ftcldstr.actionhandlers.webdav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.DirectoryOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesActions;
import net.finetunes.ftcldstr.routines.webdav.properties.StatusResponse;
import net.finetunes.ftcldstr.routines.xml.XMLParser;
import net.finetunes.ftcldstr.routines.xml.XMLService;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;

/**
 * The PROPFIND method retrieves properties defined on the resource
 * identified by the Request-URI, if the resource does not have any
 * internal members, or on the resource identified by the Request-URI
 * and potentially its member resources, if the resource is a collection
 * that has internal member URLs.  All DAV-compliant resources MUST
 * support the PROPFIND method and the propfind XML element
 * (Section 14.20) along with all XML elements defined for use with that
 * element.
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */


public class PropfindActionHandler extends AbstractActionHandler {
    
    public void handle(final RequestParams requestParams) {
        
        String fn = requestParams.getPathTranslated();
        String status = "207 Multi-Status";
        String type = "text/xml";
        boolean noroot = false;
        
        String depthstr = requestParams.getRequest().getHeader("Depth");
        if (depthstr == null) {
            depthstr = "-1";
        }
        
        if (depthstr.matches(".*,noroot.*")) {
            depthstr = depthstr.replaceFirst(",noroot", ""); // $noroot=1 if $depth =~ s/,noroot//;
            noroot = true;
        }
        
        if (depthstr.matches("(?i).*infinity.*")) {
            depthstr = "-1";
        }
        
        if (depthstr.equals("-1") && !ConfigService.ALLOW_INFINITE_PROPFIND) {
            depthstr = "0";
        }
        
        int depth = 0;
        try {
            depth = Integer.valueOf(depthstr).intValue();
        }
        catch (NumberFormatException e) {
            Logger.log("Exception: invalid depth value: " + depthstr);
        }
        
        String xml = requestParams.getRequestBody();
        
        if (xml == null || xml.matches("\\s*")) {
            xml = "<?xml version=\"1.0\" encoding=\"" + ConfigService.CHARSET + "\" ?>\n<D:propfind xmlns:D=\"DAV:\"><D:allprop/></D:propfind>";
        }


        XMLParser xmlParser = new XMLParser();
        HashMap<String, Object> xmldata = xmlParser.simpleXMLParser(xml, ConfigService.CHARSET);
        if (xmldata == null || xmldata.size() == 0) {
            Logger.debug("PROPFIND: invalid XML request: " + xml);
            OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
            return;
        }
        
        String ru = requestParams.getRequestURI();
        ru = ru.replaceAll(" ", "%20");
        Logger.debug("PROPFIND: depth=" + depthstr + ", fn=" + fn + ", ru=" + ru + "");

        ArrayList<StatusResponse> resps = new ArrayList<StatusResponse>();
        
        // ACL, CalDAV, CardDAV, ...:
        if (ConfigService.PRINCIPAL_COLLECTION_SET != null && ConfigService.PRINCIPAL_COLLECTION_SET.length() > 1 &&
                ru.endsWith(ConfigService.PRINCIPAL_COLLECTION_SET)) {
            fn = fn.replaceAll(Pattern.quote(ConfigService.PRINCIPAL_COLLECTION_SET) + "$", "");
            depthstr = "0";
        }
        else if (ConfigService.CURRENT_USER_PRINCIPAL != null && ConfigService.CURRENT_USER_PRINCIPAL.length() > 1 &&
                ru.matches(".*" + Pattern.quote(String.format(ConfigService.CURRENT_USER_PRINCIPAL, requestParams.getUsername())) + "/?")) {
            fn = fn.replaceAll(Pattern.quote(String.format(ConfigService.CURRENT_USER_PRINCIPAL, requestParams.getUsername())) + "/?$", "");
            depthstr = "0";
        }
        
        if (FileOperationsService.is_hidden(fn)) {
            // do nothing
        }
        else if (FileOperationsService.file_exits(fn)) {
            Object[] propFindElement = PropertiesActions.handlePropFindElement(requestParams, xmldata);
            ArrayList<String> props = (ArrayList<String>)propFindElement[0];
            boolean all = ((Boolean)propFindElement[1]).booleanValue();
            boolean noval = ((Boolean)propFindElement[2]).booleanValue();
            
            if (props != null) {
                DirectoryOperationsService.readDirRecursive(
                        requestParams,
                        fn, ru, resps, props, all, noval, depth, noroot);
            }
            else {
                status = "400 Bad Request";
                type = "text/plain";
            }
        }
        else {
            status = "404 Not Found";
            type = "text/plain";
        }
        
        String content = "";
        if (resps.size() > 0) {
            HashMap<String, Object> response = new HashMap<String, Object>();
            response.put("response", StatusResponse.statusResponseListToHashMap(resps));
            HashMap<String, Object> propfind = new HashMap<String, Object>();
            propfind.put("multistatus", response);
            content = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, propfind, false);
        }
        
        Logger.debug("PROPFIND: status=" + status + ", type=" + type + "");
        Logger.debug("PROPFIND: REQUEST:\n" + xml + "\nEND-REQUEST");
        Logger.debug("PROPFIND: RESPONSE:\n" + content + "\nEND-RESPONSE");
        
        OutputService.printHeaderAndContent(requestParams, status, type, content);

    }

}
