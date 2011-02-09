package net.finetunes.ftcldstr.routines.webdav.properties;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.xml.internal.ws.encoding.XMLHTTPBindingCodec;

import sun.nio.cs.Surrogate.Generator;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.GeneratorService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.routines.fileoperations.DirectoryOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileHelper;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService.StatData;
import net.finetunes.ftcldstr.routines.handlers.SupportedMethodsHandler;
import net.finetunes.ftcldstr.routines.webdav.LockingService;
import net.finetunes.ftcldstr.routines.xml.XMLService;

public class PropertiesActions {
	
	public static void removeProperty(RequestParams requestParams,
	        String propname, Object elementParentRef,
	        StatusResponse resp_200, StatusResponse resp_403) {
	    
	    String fn = requestParams.getPathTranslated();
	    Logger.debug("removeProperty: " + fn + ": " + propname);
	    
	    ConfigService.properties.removeProperty(fn, propname);
	    
	    resp_200.setHref(requestParams.getRequestURI());
	    resp_200.setPropstatStatus("HTTP/1.1 200 OK");
        resp_200.putPropstatProp(propname, null);
	}
	
	public static Object[] handlePropFindElement(RequestParams requestParams, HashMap<String, Object> xmldata) {
	    
	    ArrayList<String> props = new ArrayList<String>();
	    boolean all = false;
	    boolean noval = false;
	    
	    Set<String> keys = xmldata.keySet();
	    Iterator<String> it = keys.iterator();
	    
	    while (it.hasNext()) {
	        String propfind = it.next();
	        String nons = new String(propfind);
	        String ns = "";
	        String nonsv = new String(nons);
	        
            nons = nons.replaceFirst("\\{([^\\}]*)\\}", "");
            if (!nons.isEmpty()) {
                Pattern p = Pattern.compile("\\{([^\\}]*)\\}");
                Matcher m = p.matcher(nonsv);
                if (m.find()) {
                    ns = m.group(1);
                }
            }
            
            if ((nons.matches(".*(allprop|propname).*") ) && all) {
                OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
            }
            else if (nons.matches("(allprop|propname)")) {
                all = true;
                noval = nons.equals("propname");
                if (noval) {
                    props.addAll(ConfigService.KNOWN_COLL_PROPS);
                    props.addAll(ConfigService.KNOWN_FILE_PROPS);
                }
                
                if (!noval) {
                    props.addAll(ConfigService.ALLPROP_PROPS);
                }
            }
            else if (nons.matches("(prop|include)")) {
                handlePropElement(requestParams, (HashMap<String, Object>)xmldata.get(propfind), props);
            }
            else {
                
                boolean grepfound = false;
                
                Iterator<String> it2 = ConfigService.IGNORE_PROPS.iterator();
                while (it2.hasNext()) {
                    String p = it2.next();
                    if (p.matches(".*" + Pattern.quote(nons) + ".*")) {
                        grepfound = true;
                        break;
                    }
                }
                
                if (grepfound) {
                    continue;
                }
                else if (ConfigService.NAMESPACES.get(xmldata.get(propfind)) != null ||
                        ConfigService.NAMESPACES.get(ns) != null) {
                    // sometimes the namespace: ignore
                }
                else {
                    Logger.debug("Unknown element " + propfind + " (" + nons + ") in PROPFIND request");
                    Logger.debug(ConfigService.NAMESPACES.get(xmldata.get(propfind)));
                    OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
                    return null; // TODO: was "exit;"--check behaviour
                }
            }
	    }
	    
	    return new Object[] {props, new Boolean(all), new Boolean(noval)};
	}
	
	public static void handlePropElement(RequestParams requestParams, HashMap<String, Object> xmldata, ArrayList<String> props) {
	    
	    Set<String> keys = xmldata.keySet();
	    Iterator<String> it = keys.iterator();
	    
	    while (it.hasNext()) {
	        String prop = it.next();
	        String nons = new String(prop);
	        String ns = "";
	        String nonsv = new String(nons);
	        
	        nons = nons.replaceFirst("\\{([^}]*)\\}", "");
	        if (!nons.isEmpty()) {
    	        Pattern p = Pattern.compile("\\{([^}]*)\\}");
    	        Matcher m = p.matcher(nonsv);
    	        if (m.find()) {
    	            ns = m.group(1);
    	        }
	        }
	        
	        Object ref = xmldata.get(prop);
	        // original code: if (ref($$xmldata{$prop}) !~/^(HASH|ARRAY)$/)
	        if ((!(ref instanceof HashMap<?, ?>)) || (!ref.getClass().isArray())) {
	            // ignore namespaces
	        }
	        // original code:  elsif ($ns eq "" && ! defined $$xmldata{$prop}{xmlns}) {
	        else if (ns.equals("") && (!(ref instanceof HashMap<?, ?> && ((HashMap<String, Object>)ref).get("xmlns") != null))) {
	            OutputService.printHeaderAndContent(requestParams, "400 Bad Request");
	            return;
                // TODO: was "exit;" -- check behaviour
	        }
	        else {
	            boolean contains = false;
                Iterator<String> it2 = ConfigService.KNOWN_FILE_PROPS.iterator();
                Iterator<String> it3 = ConfigService.KNOWN_COLL_PROPS.iterator();
                
                while (it2.hasNext() && !contains) {
                    String fp = it2.next();
                    if (fp.matches(".*" + Pattern.quote(nons) + ".*")) {
                        contains = true;
                        break;
                    }
                }
                
                while (it3.hasNext() && !contains) {
                    String fp = it3.next();
                    if (fp.matches(".*" + Pattern.quote(nons) + ".*")) {
                        contains = true;
                        break;
                    }
                }
                
                if (props == null) {
                    props = new ArrayList<String>();
                }
                
                if (contains) {
                    props.add(nons);
                }
                else if (ns.equals("")) {
                    props.add("{}" + prop);
                }
                else {
                    props.add(prop);
                }
	        }
	    }
	}
	
	public static void getProperty(RequestParams requestParams, 
	        String fn, String uri, String prop, StatData statRef, 
	        StatusResponse resp_200, StatusResponse resp_404) {

	    Logger.debug("getProperty: fn=" + fn + ", uri=" + uri + ", prop=" + prop);
	    StatData stat = statRef;
	    if (stat == null) {
	        stat = FileOperationsService.stat(fn);
	    }

        if (resp_200 == null) {
            resp_200 = new StatusResponse();
        }
        
        if (resp_404 == null) {
            resp_404 = new StatusResponse();
        }
	    
	    if (prop.equals("creationdate")) {
	        
	        resp_200.putProp("creationdate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(stat.getCtimeDate()));
	    }
	    
	    if (prop.equals("displayname") && resp_200.getProp("displayname") == null) {
	        resp_200.putProp("displayname", RenderingHelper.uri_escape(FileOperationsService.basename(uri)));
	    }
	    
	    if (prop.equals("getcontentlanguage")) {
	        resp_200.putProp("getcontentlanguage", "en");
	    }
	    
	    if (prop.equals("getcontentlength")) {
            resp_200.putProp("getcontentlength", stat.getSize());
	    }
	    
	    if (prop.equals("getcontenttype")) {
	        String contentType = "";
	        if (FileOperationsService.is_directory(fn)) {
	            contentType = "httpd/unix-directory";
	        }
	        else {
	            contentType = MIMETypesHelper.getMIMEType(fn);
	        }
            resp_200.putProp("getcontenttype", contentType);
	    }
	    
        if (prop.equals("getetag")) {
            resp_200.putProp("getetag", PropertiesHelper.getETag(requestParams, fn));
        }	    

        if (prop.equals("getlastmodified")) {
            resp_200.putProp("getlastmodified", new SimpleDateFormat("EEE, dd MM yyyy HH:mm:ss z").format(stat.getMtimeDate()));
        }
        
        if (prop.equals("lockdiscovery")) {
            resp_200.putProp("lockdiscovery", LockingService.getLockDiscovery(fn));
        }
        
        if (prop.equals("resourcetype")) {
            if (!FileOperationsService.is_directory(fn)) {
                resp_200.putProp("resourcetype", null);
            }
            else {
                HashMap<String, Object> resourcetype = new HashMap<String, Object>();
                resourcetype.put("collection", null);
                
                if (ConfigService.ENABLE_CALDAV) {
                    resourcetype.put("calendar", null);
                }

                if (ConfigService.ENABLE_CALDAV_SCHEDULE) {
                    resourcetype.put("schedule-inbox", null);
                    resourcetype.put("schedule-outbox", null);
                }

                if (ConfigService.ENABLE_CARDDAV) {
                    resourcetype.put("addressbook", null);
                }

                if (ConfigService.ENABLE_GROUPDAV) {
                    resourcetype.put("vevent-collection", null);
                    resourcetype.put("vtodo-collection", null);
                    resourcetype.put("vcard-collection", null);
                }
                
                resp_200.putProp("resourcetype", resourcetype);
            }
        }
        
        if (prop.equals("component-set")) {
            resp_200.putProp("component-set", "VEVENT,VTODO,VCARD");
        }        

        if (prop.equals("supportedlock")) {
            HashMap<String, Object> supportedlock = new HashMap<String, Object>();
            ArrayList<HashMap<String, Object>> lockentry = new ArrayList<HashMap<String,Object>>();
            HashMap<String, Object> lockentry0 = new HashMap<String, Object>();
            HashMap<String, Object> lockscope0 = new HashMap<String, Object>();
            lockscope0.put("exclusive", null);
            lockentry0.put("lockscope", lockscope0);
            HashMap<String, Object> locktype0 = new HashMap<String, Object>();
            locktype0.put("write", null);
            lockentry0.put("locktype", locktype0);
            lockentry.add(lockentry0);

            HashMap<String, Object> lockentry1 = new HashMap<String, Object>();
            HashMap<String, Object> lockscope1 = new HashMap<String, Object>();
            lockscope1.put("shared", null);
            lockentry1.put("lockscope", lockscope1);
            HashMap<String, Object> locktype1 = new HashMap<String, Object>();
            locktype1.put("write", null);
            lockentry1.put("locktype", locktype1);
            lockentry.add(lockentry1);
            supportedlock.put("lockentry", lockentry);
            resp_200.putProp("resourcetype", supportedlock);
        }        
        
        if (prop.equals("executable")) {
            String executable = "F";
            if (FileOperationsService.is_file_executable(fn)) {
                executable = "T";
            }
            resp_200.putProp("executable", executable);
        }
        
        if (prop.equals("source")) {
            HashMap<String, Object> source = new HashMap<String, Object>();
            HashMap<String, Object> link = new HashMap<String, Object>();
            link.put("src", uri);
            link.put("dst", uri);
            source.put("link", link);
            resp_200.putProp("source", source);
        }
        
        if (prop.equals("quota-available-bytes") || prop.equals("quota-used-bytes") || 
                prop.equals("quota") || prop.equals("quotaused")) {
            
            Object[] quota = FileHelper.getQuota();
            
            if (quota.length >= 2 && quota[0] != null && quota[1] != null) {
                int ql = ((Integer)quota[0]).intValue();
                int qu = ((Integer)quota[1]).intValue();
                
                if (prop.equals("quota-available-bytes")) {
                    resp_200.putProp("quota-available-bytes", ql - qu);
                }
                
                if (prop.equals("quota-used-bytes")) {
                    resp_200.putProp("quota-used-bytes", qu);
                }
                
                if (prop.equals("quota")) {
                    resp_200.putProp("quota", ql);
                }
                
                if (prop.equals("quotaused")) {
                    resp_200.putProp("quotaused", qu);
                }
            }
            else {
                if (prop.equals("quota-available-bytes")) {
                    resp_404.putProp("quota-available-bytes", null);
                }
                
                if (prop.equals("quota-used-bytes")) {
                    resp_404.putProp("quota-used-bytes", null);
                }
            }
            
            // next; // original code
        }
        
        if (prop.equals("childcount")) {
            
            int dirinfo = 0;
            if (FileOperationsService.is_directory(fn)) {
                dirinfo = DirectoryOperationsService.getDirInfo(requestParams, fn, prop); 
            }
            resp_200.putProp("childcount", dirinfo);
        }
        
        if (prop.equals("id")) {
            resp_200.putProp("id", uri);
        }
        
        if (prop.equals("isfolder")) {
            int isfolder = 0;
            if (FileOperationsService.is_directory(fn)) {
                isfolder = 1;
            }
            resp_200.putProp("isfolder", isfolder);
        }
        
        if (prop.equals("ishidden")) {
            int ishidden = 0;
            if (FileOperationsService.basename(fn).startsWith(".")) {
                ishidden = 1;
            }
            resp_200.putProp("ishidden", ishidden);
        }
        
        if (prop.equals("isstructureddocument")) {
            resp_200.putProp("isstructureddocument", 0);
        }
        
        if (prop.equals("hassubs")) {
            
            int hassubs = 0;
            if (FileOperationsService.is_directory(fn)) {
                hassubs = DirectoryOperationsService.getDirInfo(requestParams, fn, prop); 
            }
            resp_200.putProp("hassubs", hassubs);
        }
        
        if (prop.equals("nosubs")) {
            
            int nosubs = 0;
            if (FileOperationsService.is_directory(fn)) {
                if (FileOperationsService.is_file_writable(fn)) {
                    nosubs = 1;
                }
                else {
                    nosubs = 0;
                }
            }
            else {
                nosubs = 1;
            }
            resp_200.putProp("nosubs", nosubs);
        }
        
        if (prop.equals("objectcount")) {
            
            int objectcount = 0;
            if (FileOperationsService.is_directory(fn)) {
                objectcount = DirectoryOperationsService.getDirInfo(requestParams, fn, prop); 
            }
            resp_200.putProp("objectcount", objectcount);
        }        
        
        if (prop.equals("reserved")) {
            resp_200.putProp("reserved", 0);
        } 
        
        if (prop.equals("visiblecount")) {
            
            int visiblecount = 0;
            if (FileOperationsService.is_directory(fn)) {
                visiblecount = DirectoryOperationsService.getDirInfo(requestParams, fn, prop); 
            }
            resp_200.putProp("visiblecount", visiblecount);
        }
        
        if (prop.equals("iscollection")) {
            
            int iscollection = 0;
            if (FileOperationsService.is_directory(fn)) {
                iscollection = 1;
            }
            resp_200.putProp("iscollection", iscollection);
        }
        
        if (prop.equals("isFolder")) {
            int isFolder = 0;
            if (FileOperationsService.is_directory(fn)) {
                isFolder = 1;
            }
            resp_200.putProp("isFolder", isFolder);
        }
        
        if (prop.equals("authoritative-directory")) {
            String ad = "f";
            if (FileOperationsService.is_directory(fn)) {
                ad = "t";
            }
            resp_200.putProp("authoritative-directory", ad);
        }
        
        if (prop.equals("resourcetag")) {
            resp_200.putProp("resourcetag", requestParams.getRequestURI());
        }
        
        if (prop.equals("repl-uid")) {
            resp_200.putProp("repl-uid", GeneratorService.getuuid(fn));
        }        

        if (prop.equals("modifiedby")) {
            String user = "";
            // user = $ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER}; // TODO
            resp_200.putProp("modifiedby", user);
        }        

        if (prop.equals("Win32CreationTime")) {
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE, dd MM yyyy HH:mm:ss z");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));            
            resp_200.putProp("Win32CreationTime", dateFormatGmt.format(stat.getCtimeDate()));
        }
        
        if (prop.equals("Win32FileAttributes")) {
            int fileattr = 128 + 32; // # 128 - Normal, 32 - Archive, 4 - System, 2 - Hidden, 1 - Read-Only
            if (!FileOperationsService.is_file_writable(fn)) {
                fileattr += 1;
            }
            if (FileOperationsService.basename(fn).startsWith(".")) {
                fileattr += 2;
            }
            resp_200.putProp("Win32FileAttributes", String.format("%08x", fileattr));
        }

        if (prop.equals("Win32LastAccessTime")) {
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE, dd MM yyyy HH:mm:ss z");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));            
            resp_200.putProp("Win32LastAccessTime", dateFormatGmt.format(stat.getAtimeDate()));
        }
        
        if (prop.equals("Win32LastModifiedTime")) {
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE, dd MM yyyy HH:mm:ss z");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));            
            resp_200.putProp("Win32LastModifiedTime", dateFormatGmt.format(stat.getMtimeDate()));
        }
        
        if (prop.equals("name")) {
            resp_200.putProp("name", RenderingHelper.HTMLEncode(FileOperationsService.basename(fn)));
        }
        
        if (prop.equals("href")) {
            resp_200.putProp("href", uri);
        }
        
        if (prop.equals("parentname")) {
            resp_200.putProp("parentname", RenderingHelper.HTMLEncode(FileOperationsService.basename(FileOperationsService.dirname(uri))));
        }
        
        if (prop.equals("isreadonly")) {
            int isreadonly = 0;
            if (!FileOperationsService.is_file_writable(fn)) {
                isreadonly = 1;
            }
            resp_200.putProp("isreadonly", isreadonly);
        }
       
        if (prop.equals("isroot")) {
            int isroot = 0;
            if (!fn.equals(ConfigService.DOCUMENT_ROOT)) {
                isroot = 1;
            }
            resp_200.putProp("isroot", isroot);
        }

        if (prop.equals("getcontentclass")) {
            String c;
            if (FileOperationsService.is_directory(fn)) {
                c = "urn:content-classes:folder";
            }
            else {
                c = "urn:content-classes:document";
            }
            resp_200.putProp("getcontentclass", c);
        }

        if (prop.equals("contentclass")) {
            String c;
            if (FileOperationsService.is_directory(fn)) {
                c = "urn:content-classes:folder";
            }
            else {
                c = "urn:content-classes:document";
            }
            resp_200.putProp("contentclass", c);
        }     
        
        if (prop.equals("lastaccessed")) {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            resp_200.putProp("lastaccessed", df.format(stat.getAtimeDate()));
        }        

        if (prop.equals("owner")) {
            HashMap<String, Object> p = new HashMap<String, Object>();
            p.put("href", uri);
            resp_200.putProp("owner", p);
        }
        
        if (prop.equals("group")) {
            HashMap<String, Object> p = new HashMap<String, Object>();
            p.put("href", uri);
            resp_200.putProp("group", p);
        }
        
        if (prop.equals("supported-privilege-set")) {
            resp_200.putProp("supported-privilege-set", ACLActions.getACLSupportedPrivilegeSet(fn));
        }
        
        if (prop.equals("current-user-privilege-set")) {
            resp_200.putProp("current-user-privilege-set", ACLActions.getACLCurrentUserPrivilegeSet(fn));
        }
        
        if (prop.equals("acl")) {
            resp_200.putProp("acl", ACLActions.getACLProp(stat.getMode()));
        }
        
        if (prop.equals("acl-restrictions")) {
            HashMap<String, Object> rest = new HashMap<String, Object>();
            rest.put("no-invert", null);
            ArrayList<HashMap<String, Object>> property = new ArrayList<HashMap<String,Object>>();
            HashMap<String, Object> p1 = new HashMap<String, Object>();
            p1.put("owner", null);
            HashMap<String, Object> p2 = new HashMap<String, Object>();
            p2.put("group", null);
            property.add(p1);
            property.add(p2);
            HashMap<String, Object> rp = new HashMap<String, Object>();
            rp.put("all", null);
            rp.put("property", property);
            rest.put("required-principal", rp);
            resp_200.putProp("acl-restrictions", rest);
        }
        
        if (prop.equals("inherited-acl-set")) {
            resp_200.putProp("inherited-acl-set", null);
        }        
        
        if (prop.equals("principal-collection-set")) {
            HashMap<String, Object> pcs = new HashMap<String, Object>();
            pcs.put("href", ConfigService.PRINCIPAL_COLLECTION_SET);
            resp_200.putProp("principal-collection-set", pcs);
        }        
        
        if (prop.equals("calendar-descriptio")) {
            resp_200.putProp("calendar-descriptio", null);
        }        
        
        if (prop.equals("calendar-timezone")) {
            resp_200.putProp("calendar-timezone", null);
        }                      
        
        if (prop.equals("supported-calendar-component-set")) {
            resp_200.putProp("supported-calendar-component-set", "<C:comp name=\"VEVENT\"/><C:comp name=\"VTODO\"/><C:comp name=\"VJOURNAL\"/><C:comp name=\"VTIMEZONE\"/>");
        }                      
        
        if (prop.equals("supported-calendar-data")) {
            resp_200.putProp("supported-calendar-data", "<C:calendar-data content-type=\"text/calendar\" version=\"2.0\"/>");
        }                      
        
        if (prop.equals("max-resource-size")) {
            resp_200.putProp("max-resource-size", 20000000);
        }                      
        
        if (prop.equals("min-date-time")) {
            resp_200.putProp("min-date-time", "19000101T000000Z");
        }                      
        
        if (prop.equals("max-date-time")) {
            resp_200.putProp("max-date-time", "20491231T235959Z");
        }                      
        
        if (prop.equals("max-instances")) {
            resp_200.putProp("max-instances", 100);
        }   
        
        if (prop.equals("max-attendees-per-instance")) {
            resp_200.putProp("max-attendees-per-instance", 100);
        }
        
//        if (prop.equals("calendar-data")) {
//            resp_200.putProp("calendar-data", "<![CDATA[" + FileOperationsService.getFileContent(fn) + "]]>");
//        }
        
        if (prop.equals("calendar-data")) {
            if (fn.toLowerCase().endsWith(".ics")) {
                resp_200.putProp("calendar-data", RenderingHelper.HTMLEncode(FileOperationsService.getFileContent(fn)));
            }
            else {
                resp_404.putProp("calendar-data", null);
            }
        }

        if (prop.equals("getctag")) {
            resp_200.putProp("getctag", PropertiesHelper.getETag(requestParams, fn));
        }        

        if (prop.equals("current-user-principal")) {
            HashMap<String, Object> cup = new HashMap<String, Object>();
            cup.put("href", ConfigService.CURRENT_USER_PRINCIPAL);
            resp_200.putProp("current-user-principal", cup);
        }

        if (prop.equals("principal-URL")) {
            HashMap<String, Object> cup = new HashMap<String, Object>();
            cup.put("href", ConfigService.CURRENT_USER_PRINCIPAL);
            resp_200.putProp("principal-URL", cup);
        }

        if (prop.equals("calendar-home-set")) {
            HashMap<String, Object> cup = new HashMap<String, Object>();
            cup.put("href", HomesetActions.getCalendarHomeSet(uri));
            resp_200.putProp("calendar-home-set", cup);
        }

        if (prop.equals("calendar-user-address-set")) {
            HashMap<String, Object> cup = new HashMap<String, Object>();
            cup.put("href", ConfigService.CURRENT_USER_PRINCIPAL);
            resp_200.putProp("calendar-user-address-set", cup);
        }
        
        if (prop.equals("schedule-inbox-URL")) {
            HashMap<String, Object> cup = new HashMap<String, Object>();
            cup.put("href", HomesetActions.getCalendarHomeSet(uri));
            resp_200.putProp("schedule-inbox-URL", cup);
        }        
        
        if (prop.equals("schedule-outbox-URL")) {
            HashMap<String, Object> cup = new HashMap<String, Object>();
            cup.put("href", HomesetActions.getCalendarHomeSet(uri));
            resp_200.putProp("schedule-outbox-URL", cup);
        }     
        
        if (prop.equals("calendar-user-type")) {
            resp_200.putProp("calendar-user-type", "INDIVIDUAL");
        }           

        if (prop.equals("schedule-calendar-transp")) {
            HashMap<String, Object> cup = new HashMap<String, Object>();
            cup.put("transparent", null);
            resp_200.putProp("schedule-calendar-transp", cup);
        }       
        
        if (prop.equals("schedule-default-calendar-URL")) {
            resp_200.putProp("schedule-default-calendar-URL", HomesetActions.getCalendarHomeSet(uri));
        }              
        
        if (prop.equals("schedule-tag")) {
            resp_200.putProp("schedule-tag", PropertiesHelper.getETag(requestParams, fn));
        }       
        
        if (prop.equals("address-data")) {
            if (fn.toLowerCase().endsWith(".vcf")) {
                resp_200.putProp("address-data", RenderingHelper.HTMLEncode(FileOperationsService.getFileContent(fn)));
            }
            else {
                resp_404.putProp("address-data", null);
            }            
        }       
        
        if (prop.equals("addressbook-description")) {
            resp_200.putProp("addressbook-description", RenderingHelper.HTMLEncode(FileOperationsService.basename(fn)));
        }
        
        if (prop.equals("supported-address-data")) {
            resp_200.putProp("supported-address-data", "<A:address-data-type content-type=\"text/vcard\" version=\"3.0\"/>");
        }
        
        if (prop.equals("max-resource-size") && ConfigService.ENABLE_CARDDAV) {
            resp_200.putProp("{urn:ietf:params:xml:ns:carddav}max-resource-size", 20000000);
        }            
        
        if (prop.equals("principal-address")) {
            HashMap<String, Object> cup = new HashMap<String, Object>();
            cup.put("href", uri);
            resp_200.putProp("principal-address", cup);
        }
        
        if (prop.equals("supported-report-set")) {
            HashMap<String, Object> srs = new HashMap<String, Object>();
            ArrayList<HashMap<String, Object>> sr = new ArrayList<HashMap<String,Object>>();
            
            HashMap<String, Object> r1 = new HashMap<String, Object>();
            HashMap<String, Object> r11 = new HashMap<String, Object>();
            r11.put("acl-principal-prop-set", null);
            r1.put("report", r11);
            sr.add(r1);
            
            HashMap<String, Object> r2 = new HashMap<String, Object>();
            HashMap<String, Object> r21 = new HashMap<String, Object>();
            r21.put("principal-match", null);
            r2.put("report", r21);
            sr.add(r2);
            
            HashMap<String, Object> r3 = new HashMap<String, Object>();
            HashMap<String, Object> r31 = new HashMap<String, Object>();
            r31.put("principal-property-search", null);
            r3.put("report", r31);
            sr.add(r3);
            
            HashMap<String, Object> r4 = new HashMap<String, Object>();
            HashMap<String, Object> r41 = new HashMap<String, Object>();
            r41.put("calendar-multiget", null);
            r4.put("report", r41);
            sr.add(r4);
            
            HashMap<String, Object> r5 = new HashMap<String, Object>();
            HashMap<String, Object> r51 = new HashMap<String, Object>();
            r51.put("calendar-query", null);
            r5.put("report", r51);
            sr.add(r5);
            
            HashMap<String, Object> r6 = new HashMap<String, Object>();
            HashMap<String, Object> r61 = new HashMap<String, Object>();
            r61.put("free-busy-query", null);
            r6.put("report", r61);
            sr.add(r6);
            
            HashMap<String, Object> r7 = new HashMap<String, Object>();
            HashMap<String, Object> r71 = new HashMap<String, Object>();
            r71.put("addressbook-query", null);
            r7.put("report", r71);
            sr.add(r7);
            
            HashMap<String, Object> r8 = new HashMap<String, Object>();
            HashMap<String, Object> r81 = new HashMap<String, Object>();
            r81.put("addressbook-multiget", null);
            r8.put("report", r81);
            sr.add(r8);
            
            srs.put("supported-report", sr);
            resp_200.putProp("supported-report-set", srs);
        }   
        
        if (prop.equals("supported-method-set")) {
            String sm = "";
            
            ArrayList<String> methods = SupportedMethodsHandler.getSupportedMethods(fn);
            Iterator<String> it = methods.iterator();
            while (it.hasNext()) {
                String method = it.next();
                sm += "<D:supported-method name=\"" + method + "\"/>";
            }
            resp_200.putProp("supported-method-set", sm);
        }           
        
        if (prop.equals("resource-id")) {
            String e = PropertiesHelper.getETag(requestParams, FileOperationsService.full_resolve(fn));
            e = e.replaceAll("\"", "");
            resp_200.putProp("resource-id", "urn:uuid:" + e);
        }           
	}
	
	// TODO: params and return type
	public static void setProperty(RequestParams requestParams, String propname, HashMap<String, Object> elementParentRef, 
	        StatusResponse resp_200, StatusResponse resp_403) {
	    
	    String fn = requestParams.getPathTranslated();
	    String ru = requestParams.getRequestURI();
	    
	    String ns = "";
	    String pn = "";
        Pattern p = Pattern.compile("^\\{([^\\}]+)\\}(.*)$");
        Matcher m = p.matcher(propname);
        if (m.find()) {
            ns = m.group(1);
            pn = m.group(2);
        }
        
        Logger.debug("setProperty: " + propname + " (ns=" + ns + ", pn=" + pn + ")");
        
        if (propname.equals("{http://apache.org/dav/props/}executable")) {
            if (elementParentRef.get(propname) != null && elementParentRef.get(propname) instanceof HashMap<?, ?>) {
                String executable = (String)((HashMap<String, Object>)elementParentRef.get(propname)).get("content");
                if (executable != null) {
                    StatData stat = FileOperationsService.stat(fn);
                    int mode;
                    if (executable.matches(".*F.*")) {
                        mode = stat.getMode() & 0666;
                    }
                    else {
                        mode = stat.getMode() | 0111;
                    }
                    
                    FileOperationsService.chmod(mode, fn);
                    resp_200.setHref(ru);
                    resp_200.putPropstatProp("executable", executable);
                    resp_200.setPropstatStatus("HTTP/1.1 200 OK");
                }
            }
        }
        else if (propname.equals("{DAV:}getlastmodified") || 
                propname.equals("{urn:schemas-microsoft-com:}Win32LastModifiedTime") || 
                propname.equals("{urn:schemas-microsoft-com:}Win32LastAccessTime") || 
                propname.equals("{urn:schemas-microsoft-com:}Win32CreationTime")) {
            
            String getlastmodified = (String)elementParentRef.get("{DAV:}getlastmodified");
            if (getlastmodified == null) {
                getlastmodified = (String)elementParentRef.get("{urn:schemas-microsoft-com:}Win32LastModifiedTime");
            }
            
            String lastaccesstime = (String)elementParentRef.get("{urn:schemas-microsoft-com:}Win32LastAccessTime");
            
            if (getlastmodified != null) {
                Date mtime = RenderingHelper.parseHTTPDate(getlastmodified);
                Date atime = (Date)mtime.clone();
                if (lastaccesstime != null) {
                    atime = RenderingHelper.parseHTTPDate(lastaccesstime);
                }
                
                FileOperationsService.utime(atime, mtime, fn);
                resp_200.setHref(ru);
                if (elementParentRef.containsKey("{DAV:}getlastmodified")) {
                    resp_200.putPropstatProp("getlastmodified", getlastmodified);
                }
                if (elementParentRef.get("{urn:schemas-microsoft-com:}Win32LastModifiedTime") != null) {
                    resp_200.putPropstatProp("Win32LastModifiedTime", getlastmodified);
                }
                if (elementParentRef.get("{urn:schemas-microsoft-com:}Win32LastAccessTime") != null) {
                    resp_200.putPropstatProp("Win32LastAccessTime", lastaccesstime);
                }
                if (elementParentRef.containsKey("{urn:schemas-microsoft-com:}Win32CreationTime")) {
                    resp_200.putPropstatProp("Win32CreationTime", elementParentRef.get("{urn:schemas-microsoft-com:}Win32CreationTime"));
                }
                resp_200.setPropstatStatus("HTTP/1.1 200 OK");
            }
            
        }
        else if (propname.equals("{urn:schemas-microsoft-com:}Win32FileAttributes")) {
            resp_200.setHref(ru);
            resp_200.putPropstatProp("Win32FileAttributes", null);
            resp_200.setPropstatStatus("HTTP/1.1 200 OK");            
        }
        else if (ConfigService.NAMESPACES.get(ns) != null && ConfigService.PROTECTED_PROPS.contains(pn)) {
            resp_403.setHref(ru);
            resp_403.putPropstatProp(propname, null);
            resp_403.setPropstatStatus("HTTP/1.1 403 Forbidden");             
        }
        else {
            
            String n = new String(propname);
            if (elementParentRef.get(propname) instanceof HashMap<?, ?> && 
                    elementParentRef.get(propname) != null &&
                    ((HashMap<String, Object>)elementParentRef.get(propname)) != null &&
                    (((String)((HashMap<String, Object>)elementParentRef.get(propname)).get("xmlns")).isEmpty()) &&
                            !n.matches("\\{[^\\}]*\\}.*")) {
                n = "{}" + n;
            }
            
            String value = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, (HashMap<String, Object>)elementParentRef.get(propname), false);
            boolean ret = ConfigService.properties.setProperty(fn, n, value);
            
            if (ret) {
                resp_200.setHref(ru);
                resp_200.putPropstatProp(propname, null);
                resp_200.setPropstatStatus("HTTP/1.1 200 OK");
            }
            else {
                resp_403.setHref(ru);
                resp_403.putPropstatProp(propname, null);
                resp_403.setPropstatStatus("HTTP/1.1 403 Forbidden");
            }
        }
	}
	
}
