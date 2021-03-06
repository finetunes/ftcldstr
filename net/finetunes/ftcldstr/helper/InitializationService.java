package net.finetunes.ftcldstr.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.finetunes.ftcldstr.ExitException;
import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.WebDAVLocks;
import net.finetunes.ftcldstr.routines.webdav.properties.Properties;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;

public class InitializationService {
    
    
    public volatile static boolean initialized = false;
    
    /*
     * initialises runtime variables
     */
    public synchronized static void init(final ServletContext servletContext) {
        
        if (initialized) {
            return;
        }
        
        String rootPath = servletContext.getRealPath("");
        if (!rootPath.endsWith( System.getProperty("file.separator"))) {
            rootPath +=  System.getProperty("file.separator");
        }        
        
        ConfigService.ROOT_PATH = rootPath;
        initProps();
        
        // supported DAV compliant classes:
        String DAV = "1";
        if (ConfigService.ENABLE_LOCK) {
            DAV += ", 2";
        }
        
        DAV += ", 3, <http://apache.org/dav/propset/fs/1>, extended-mkcol";
        
        if (ConfigService.ENABLE_ACL || ConfigService.ENABLE_CALDAV || ConfigService.ENABLE_CARDDAV) {
            DAV += ", access-control";
        }
        
        if (ConfigService.ENABLE_CALDAV || ConfigService.ENABLE_CALDAV_SCHEDULE) {
            DAV += ", calendar-access, calendarserver-private-comments";
        }
        
        if (ConfigService.ENABLE_CALDAV || ConfigService.ENABLE_CALDAV_SCHEDULE) {
            DAV += ", calendar-auto-schedule";
        }
        
        if (ConfigService.ENABLE_CARDDAV) {
            DAV += ", addressbook";
        }
        
        if (ConfigService.ENABLE_BIND) {
            DAV += ", bind";
        }
            
        ConfigService.DAV = DAV;
        
        initMessages();

        Properties p = Properties.deserialize(ConfigService.PROPERTIES_FILE_PATH);
        ConfigService.properties = p;
        
        WebDAVLocks l = WebDAVLocks.deserialize(ConfigService.LOCKS_FILE_PATH);
        ConfigService.locks = l;
        
        initialized = true;
    }
    
    public synchronized static RequestParams initRequestParams(
            final HttpServletRequest request, final HttpServletResponse response,
            final ServletContext servletContext) {
        
        String pathSeparator = System.getProperty("file.separator");
        
        RequestParams params = new RequestParams();
        params.setRequest(request);
        params.setResponse(response);
        params.setRequestedMethod(request.getMethod());

        String requestURI = request.getServletPath();

        String pathTranslated;
        String documentRoot;
        
        if (ConfigService.BASE_PATH == null || ConfigService.BASE_PATH.isEmpty()) {
            pathTranslated = servletContext.getRealPath(request.getServletPath());
            if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
                // requestURI += "?" + request.getQueryString(); // PZ: seems no param should be here
            }
            
            documentRoot = servletContext.getRealPath("");
        }
        else {
            pathTranslated = ConfigService.BASE_PATH + request.getServletPath().replaceFirst("^/", "");
            documentRoot = ConfigService.BASE_PATH;
        }
        documentRoot = documentRoot.replaceFirst("/$", "");
        ConfigService.DOCUMENT_ROOT = documentRoot;

/*
 * not required in java
        # 404/rewrite/redirect handling:
        if (!defined $PATH_TRANSLATED) {
            $PATH_TRANSLATED = $ENV{REDIRECT_PATH_TRANSLATED};

            if (!defined $PATH_TRANSLATED && (defined $ENV{SCRIPT_URL} || defined $ENV{REDIRECT_URL})) {
                my $su = $ENV{SCRIPT_URL} || $ENV{REDIRECT_URL};
                $su=~s/^$VIRTUAL_BASE//;
                $PATH_TRANSLATED = $DOCUMENT_ROOT.$su;
            }
        }
*/

        if (FileOperationsService.is_directory(params, pathTranslated) && !pathTranslated.endsWith(pathSeparator)) {
            pathTranslated += pathSeparator;
        }
        
        if (FileOperationsService.is_directory(params, pathTranslated) && !requestURI.endsWith("/")) {
            requestURI += "/";
        }
        
        String userIP = request.getRemoteAddr();
        
        params.setPathTranslated(pathTranslated);
        params.setRequestURI(requestURI);
        params.setScriptURI(ConfigService.ROOT_PATH);
        params.setServletContext(servletContext);
        params.setUserIP(userIP);

        return params;
    }
    
    public static void checkRunPermissions(RequestParams requestParams) {
        
        String uid = SystemCalls.getCurrentProcessUid(requestParams);
        if (ConfigService.FORBIDDEN_UID != null && ConfigService.FORBIDDEN_UID.contains(uid)) {
            Logger.debug("Forbidden UID");
            OutputService.printHeaderAndContent(requestParams, "403 Forbidden");
            throw new ExitException("Forbidden UID");
        }
    }
    
    
    public static void initMessages() {
        
        // Don't use entities like &auml; for buttons and table header (names, lastmodified, size, mimetype).
        HashMap<String, String> strings = new HashMap<String, String>();
        
        strings.put("search", "Search for file/folder name:");
        strings.put("searchtooltip", "allowed are: file/folder name, regular expression");
        strings.put("searchnothingfound", "Nothing found for ");
        strings.put("searchgoback", " in ");
        strings.put("searchresultsfor", " search results for ");
        strings.put("searchresultfor", " search result for ");
        strings.put("searchresults", " results in");
        strings.put("searchresult", " result in");
        strings.put("mount", "[M]");
        strings.put("mounttooltip", "View this collection in your WebDAV client (WebDAV mount).");
        strings.put("quotalimit", "Quota limit: ");
        strings.put("quotaused", " used: ");
        strings.put("quotaavailable", " available: ");
        strings.put("navpage", "Page ");
        strings.put("navfirst", " |&lt; ");
        strings.put("navprev", " &lt;&lt; ");
        strings.put("navnext", " &gt;&gt; ");
        strings.put("navlast", " &gt;| ");
        strings.put("navall", "All");
        strings.put("navpageview", "View by page");
        strings.put("navfirstblind", " |&lt; ");
        strings.put("navprevblind", " &lt;&lt; ");
        strings.put("navnextblind", " &gt;&gt; ");
        strings.put("navlastblind", " &gt;| ");
        strings.put("navfirsttooltip", "First Page");
        strings.put("navprevtooltip", "Previous Page");
        strings.put("navnexttooltip", "Next Page");
        strings.put("navlasttooltip", "Last Page");
        strings.put("navalltooltip", "Show All");
        strings.put("togglealltooltip", "Toggle All");
        strings.put("showproperties", "Show Properties");
        strings.put("properties", " properties");
        strings.put("propertyname", "Name");
        strings.put("propertyvalue", "Value");
        strings.put("names", "Files/Folders");
        strings.put("lastmodified", "Last Modified");
        strings.put("size", "Size");
        strings.put("mimetype", "MIME Type");
        
        // strings.put("lastmodifiedformat", "%d-%b-%Y %H:%M");
        strings.put("lastmodifiedformat", "dd-MM-yyyy HH:mm");
        
        strings.put("statfiles", "files:");
        strings.put("statfolders", "folders:");
        strings.put("statsum", "sum:");
        strings.put("statsize", "size:");
        strings.put("createfoldertext", "Create new folder: ");
        strings.put("createfolderbutton", "Create Folder");
        strings.put("movefilestext", "Rename/Move selected files/folders to: ");
        strings.put("movefilesbutton", "Rename/Move");
        strings.put("movefilesconfirm", "Do you really want to rename/move selected files/folders to the new file name or folder?");
        strings.put("deletefilesbutton", "Delete");
        strings.put("deletefilestext", " selected files/folders");
        strings.put("deletefilesconfirm", "Do you really want to delete selected files/folders?");
        strings.put("zipdownloadbutton", "Download");
        strings.put("zipdownloadtext", " selected files/folders (tar.gz archive)");
        strings.put("zipuploadtext", "Upload tar.gz archive: ");
        strings.put("zipuploadbutton", "Upload & Extract");
        strings.put("zipuploadconfirm", "Do you really want to upload tar.gz, extract it and replace existing files?");
        strings.put("fileuploadtext", "File: ");
        strings.put("fileuploadbutton", "Upload");
        strings.put("fileuploadmore", "more");
        strings.put("fileuploadconfirm", "Do you really want to upload file(s) and replace existing file(s)?");
        strings.put("confirm", "Please confirm.");
        strings.put("foldernotwriteable", "This folder is not writeable (no write permission).");
        strings.put("foldernotreadable", "This folder is not readable (no read permission).");
        strings.put("msg_deletedsingle", "%s file/folder was deleted.");
        strings.put("msg_deletedmulti", "%s files/folders were deleted.");
        strings.put("msg_deleteerr", "Could not delete selected files/folders.");
        strings.put("msg_deletenothingerr", "Please select file(s)/folder(s) to delete.");
        strings.put("msg_foldercreated", "Folder '%s' was created successfully.");
        strings.put("msg_foldererr", "Could not create folder '%s' (%s).");
        strings.put("msg_foldernothingerr", "Please specify a folder to create.");
        strings.put("msg_rename", "Moved files/folders '%s' to '%s'.");
        strings.put("msg_renameerr", "Could not move files/folders '%s' to '%s'.");
        strings.put("msg_renamenothingerr", "Please select files/folders to rename/move.");
        strings.put("msg_renamenotargeterr", "Please specify a target folder/name for move/rename.");
        strings.put("msg_uploadsingle", "%s file (%s) uploaded successfully.");
        strings.put("msg_uploadmulti", "%s files (%s) uploaded successfully.");
        strings.put("msg_uploadnothingerr", "Please select a local file (Browse ...) for upload.");
        strings.put("msg_zipuploadsingle", "%s tar.gz archive (%s) uploaded successfully.");
        strings.put("msg_zipuploadmulti", "%s tar.gz archives (%s) uploaded successfully.");
        strings.put("msg_zipuploadnothingerr", "Please select a local tar.gz archive (Browse...) for upload.");
        strings.put("clickforfullsize", "Click for full size");
        strings.put("permissions", "Permissions");
        strings.put("user", "user: ");
        strings.put("group", "; group: ");
        strings.put("others", "; others: ");
        strings.put("recursive", "recursive");
        strings.put("changefilepermissions", "Change file permissions: ");
        strings.put("changepermissions", "Change");
        strings.put("readable", "r");
        strings.put("writeable", "w");
        strings.put("executable", "x");
        strings.put("sticky", "t");
        strings.put("setuid", "s");
        strings.put("setgid", "s");
        strings.put("add", "add (+)");
        strings.put("set", "set (=)");
        strings.put("remove", "remove (-)");
        strings.put("changepermconfirm", "Do you really want to change file/folder permissions for selected files/folders?");
        strings.put("msg_changeperm", "Changed file/folder permissions successfully.");
        strings.put("msg_chpermnothingerr", "Please select files/folders to change permissions.");
        strings.put("changepermlegend", "r - read, w - write, x - execute, s - setuid/setgid, t - sticky bit");
        
        ConfigService.stringMessages = strings;
        
    }
    
    public static void initProps() {

        ConfigService.KNOWN_COLL_PROPS = new ArrayList<String>(Arrays.asList(
                "creationdate", 
                "displayname", 
                "getcontentlanguage", 
                "getlastmodified", 
                "lockdiscovery", 
                "resourcetype", 
                "getetag", 
                "getcontenttype", 
                "supportedlock", 
                "source", 
                "quota-available-bytes", 
                "quota-used-bytes", 
                "quota", 
                "quotaused", 
                "childcount", 
                "id", 
                "isfolder", 
                "ishidden", 
                "isstructureddocument", 
                "hassubs", 
                "nosubs", 
                "objectcount", 
                "reserved", 
                "visiblecount", 
                "iscollection", 
                "isFolder", 
                "authoritative-directory", 
                "resourcetag", 
                "repl-uid", 
                "modifiedby", 
                "Win32CreationTime", 
                "Win32FileAttributes", 
                "Win32LastAccessTime", 
                "Win32LastModifiedTime", 
                "name", 
                "href", 
                "parentname", 
                "isreadonly", 
                "isroot", 
                "getcontentclass", 
                "lastaccessed", 
                "contentclass", 
                "supported-report-set", 
                "supported-method-set"
                ));
        
        ConfigService.KNOWN_ACL_PROPS = new ArrayList<String>(Arrays.asList(
                "owner",
                "group",
                "supported-privilege-set", 
                "current-user-privilege-set", 
                "acl",
                "acl-restrictions",
                "inherited-acl-set", 
                "principal-collection-set", 
                "current-user-principal"
                ));
        
        ConfigService.KNOWN_CALDAV_COLL_PROPS = new ArrayList<String>(Arrays.asList(
                "calendar-description",
                "calendar-timezone",
                "supported-calendar-component-set",
                "supported-calendar-data",
                "max-resource-size",
                "min-date-time",
                "max-date-time",
                "max-instances",
                "max-attendees-per-instance",
                "getctag",
                "principal-URL",
                "calendar-home-set",
                "schedule-inbox-URL",
                "schedule-outbox-URL",
                "calendar-user-type",
                "schedule-calendar-transp",
                "schedule-default-calendar-URL",
                "schedule-tag",
                "calendar-user-address-set"                
                ));
    
        ConfigService.KNOWN_CALDAV_FILE_PROPS = new ArrayList<String>(Arrays.asList(
                "calendar-data"                
                ));
        
        ConfigService.KNOWN_CARDDAV_COLL_PROPS = new ArrayList<String>(Arrays.asList(
                "addressbook-description",
                "supported-address-data",
                "addressbook-home-set",
                "principal-address"
                ));

        ConfigService.KNOWN_CARDDAV_FILE_PROPS = new ArrayList<String>(Arrays.asList(
                "address-data"
                ));
        
        ConfigService.KNOWN_COLL_LIVE_PROPS = new ArrayList<String>();
        ConfigService.KNOWN_FILE_LIVE_PROPS = new ArrayList<String>();
        
        ConfigService.KNOWN_CALDAV_COLL_LIVE_PROPS = new ArrayList<String>(Arrays.asList(
                "resourcetype", 
                "displayname", 
                "calendar-description", 
                "calendar-timezone", 
                "calendar-user-address-set"
                ));
        
        ConfigService.KNOWN_CALDAV_FILE_LIVE_PROPS = new ArrayList<String>();
        
        ConfigService.KNOWN_CARDDAV_COLL_LIVE_PROPS = new ArrayList<String>(Arrays.asList(
                "addressbook-description"
                ));

        ConfigService.KNOWN_CARDDAV_FILE_LIVE_PROPS = new ArrayList<String>();
        
        if (ConfigService.ENABLE_CALDAV || ConfigService.ENABLE_CALDAV_SCHEDULE || ConfigService.ENABLE_CARDDAV) {
            ConfigService.KNOWN_COLL_LIVE_PROPS.addAll(ConfigService.KNOWN_CALDAV_COLL_LIVE_PROPS);
            ConfigService.KNOWN_FILE_LIVE_PROPS.addAll(ConfigService.KNOWN_CALDAV_FILE_LIVE_PROPS);
        }
        
        if (ConfigService.ENABLE_CARDDAV) {
            ConfigService.KNOWN_COLL_LIVE_PROPS.addAll(ConfigService.KNOWN_CARDDAV_COLL_LIVE_PROPS);
        }
        
        if (ConfigService.ENABLE_ACL || ConfigService.ENABLE_CALDAV || ConfigService.ENABLE_CALDAV_SCHEDULE || ConfigService.ENABLE_CARDDAV) {
            ConfigService.KNOWN_COLL_PROPS.addAll(ConfigService.KNOWN_ACL_PROPS);
        }
        
        if (ConfigService.ENABLE_CALDAV || ConfigService.ENABLE_CALDAV_SCHEDULE) {
            ConfigService.KNOWN_COLL_PROPS.addAll(ConfigService.KNOWN_CALDAV_COLL_PROPS);
        }
        
        if (ConfigService.ENABLE_CARDDAV) {
            ConfigService.KNOWN_COLL_PROPS.addAll(ConfigService.KNOWN_CARDDAV_COLL_PROPS);
        }
        
        if (ConfigService.ENABLE_BIND) {
            ConfigService.KNOWN_COLL_PROPS.add("resource-id");
        }
        
        
        ConfigService.KNOWN_FILE_PROPS = new ArrayList<String>();
        ConfigService.KNOWN_FILE_PROPS.addAll(ConfigService.KNOWN_COLL_PROPS);
        ConfigService.KNOWN_FILE_PROPS.add("getcontentlength");
        ConfigService.KNOWN_FILE_PROPS.add("executable");
        
        if (ConfigService.ENABLE_CALDAV || ConfigService.ENABLE_CALDAV_SCHEDULE) {
            ConfigService.KNOWN_FILE_PROPS.addAll(ConfigService.KNOWN_CALDAV_FILE_PROPS);
        }
        
        if (ConfigService.ENABLE_CARDDAV) {
            ConfigService.KNOWN_FILE_PROPS.addAll(ConfigService.KNOWN_CARDDAV_FILE_PROPS);
        }
        
        if (ConfigService.ENABLE_GROUPDAV) {
            ConfigService.KNOWN_COLL_PROPS.add("component-set");
        }
        
        ConfigService.UNSUPPORTED_PROPS = new ArrayList<String>(Arrays.asList(
                "checked-in", 
                "checked-out", 
                "xmpp-uri", 
                "dropbox-home-URL",
                "appledoubleheader",
                "parent-set"
                ));
        
        ConfigService.PROTECTED_PROPS = new ArrayList<String>();
        ConfigService.PROTECTED_PROPS.addAll(ConfigService.UNSUPPORTED_PROPS);
        ConfigService.PROTECTED_PROPS.addAll(Arrays.asList(
                "getcontentlength",
                "getcontenttype",
                "getetag",
                "lockdiscovery",
                "source",
                "supportedlock",
                "supported-report-set",
                "quota-available-bytes, quota-used-bytes",
                "quota",
                "quotaused",
                "childcount",
                "id",
                "isfolder",
                "ishidden",
                "isstructureddocument",
                "hassubs",
                "nosubs",
                "objectcount",
                "reserved",
                "visiblecount",
                "iscollection","isFolder",
                "authoritative-directory",
                "resourcetag",
                "repl-uid",
                "modifiedby",
                "name","href",
                "parentname",
                "isreadonly",
                "isroot",
                "getcontentclass",
                "contentclass",
                "owner",
                "group",
                "supported-privilege-set",
                "current-user-privilege-set",
                "acl",
                "acl-restrictions",
                "inherited-acl-set",
                "principal-collection-set",
                "supported-calendar-component-set",
                "supported-calendar-data",
                "max-resource-size",
                "min-date-time",
                "max-date-time",
                "max-instances",
                "max-attendees-per-instance",
                "getctag",
                "current-user-principal",
                "calendar-user-address-set",
                "schedule-inbox-URL",
                "schedule-outbox-URL",
                "schedule-calendar-transp",
                "schedule-default-calendar-URL",
                "schedule-tag",
                "supported-address-data",
                "supported-collation-set",
                "supported-method-set",
                "supported-method",
                "supported-query-grammar"
                ));
        
        ConfigService.ALLPROP_PROPS = new ArrayList<String>(Arrays.asList(
                "creationdate",
                "displayname",
                "getcontentlanguage",
                "getlastmodified",
                "lockdiscovery",
                "resourcetype",
                "supportedlock",
                "getetag",
                "getcontenttype",
                "getcontentlength",
                "executable" 
                ));        
    }
    
    public synchronized static void initUmaskSettings(RequestParams requestParams) {
        if (!requestParams.isUmaskInitialized()) {
            WrappingUtilities.umask(requestParams, String.format("%03o", ConfigService.UMASK));
            requestParams.setUmaskInitialized(true);
        }
    }

}
