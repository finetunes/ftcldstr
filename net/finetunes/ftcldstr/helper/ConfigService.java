package net.finetunes.ftcldstr.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.finetunes.ftcldstr.routines.webdav.WebDAVLocks;
import net.finetunes.ftcldstr.routines.webdav.properties.Properties;

/**
 * Config class to store global configuration values
 *
 */
public class ConfigService {
    
    // has to have the trailing slash
    public static final String BASE_PATH = "/home/";
    // Root path of the servlet
    public static String ROOT_PATH = null; // init in InitializationService.init();
    
    /*
     * -- VIRTUAL_BASE
     * only necessary if you use redirects or rewrites
     * from a VIRTUAL_BASE to the DOCUMENT_ROOT
     * regular expressions are allowed
     * DEFAULT: $VIRTUAL_BASE = "";
     */
    public static final String VIRTUAL_BASE = "(/webdav|)";
    
    /*
     * -- DOCUMENT_ROOT
     * by default the server document root
     * (don't forget a trailing slash '/'):
     */
    // $DOCUMENT_ROOT = $ENV{DOCUMENT_ROOT};
    // the variable is initialised in InitializationService.initRequestParams
    // trailing slash shouldn't be used
    public static String DOCUMENT_ROOT = null;

    /*
     * -- UMASK
     * mask for file/folder creation
     * (it does not change permission of existing files/folders):
     * DEFAULT: $UMASK = 0002; # read/write/execute for users and groups, others get read/execute permissions
     */
    public static final int UMASK = 0022; // TODO: use it for file operations
    
    /*
     * -- MIMETYPES
     * some MIME types for Web browser access and GET access
     * you can add some missing types ('extension list' => 'mime-type'):
     */
    public static final Map<String, String> MIMETYPES = 
        Collections.unmodifiableMap(new HashMap<String, String>() {{ 
            put("html htm shtm shtml", "text/html");
            put("css", "text/css");
            put("xml xsl", "text/xml");
            put("js", "application/x-javascript");
            put("txt asc pl cgi php php3 php4 php5 php6 csv log out java jsp tld tag", "text/plain");
            put("c", "text/x-csrc");
            put("h", "text/x-chdr");
            put("gif", "image/gif");
            put("jpeg jpg jpe", "image/jpg");
            put("png", "image/png");
            put("bmp", "image/bmp");
            put("tiff", "image/tiff");
            put("pdf", "application/pdf");
            put("ps", "application/ps");
            put("dvi", "application/x-dvi");
            put("tex", "application/x-tex");
            put("zip", "application/zip");
            put("tar", "application/x-tar");
            put("gz", "application/x-gzip");
            put("doc dot", "application/msword");
            put("xls xlm xla xlc xlt xlw", "application/vnd.ms-excel");
            put("ppt pps pot", "application/vnd.ms-powerpoint");
            put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
            put("ics", "text/calendar");
            put("avi", "video/x-msvideo");
            put("wmv", "video/x-ms-wmv");
            put("ogv", "video/ogg");
            put("mpeg mpg mpe", "video/mpeg");
            put("qt mov", "video/quicktime");
            put("default", "application/octet-stream");
        }});        
        
    /*
     * -- FANCYINDEXING
     * enables/disables fancy indexing for GET requests on folders
     * if disabled you get a 404 error for a GET request on a folder
     * DEFAULT: $FANCYINDEXING = 1;
     */
    public static final boolean FANCYINDEXING = true;
    
    /*
     * -- MAXFILENAMESIZE
     * Web interface: width of filename column
     */
    public static final int MAXFILENAMESIZE = 30;

    /*
     * -- MAXLASTMODIFIEDSIZE
     * Web interface: width of last modified column
     */
    public static final int MAXLASTMODIFIEDSIZE = 20;

    /*
     * -- MAXSIZESIZE
     * Web interface: width of size column
     */
    public static final int MAXSIZESIZE = 12;

    /*
     * -- ICONS
     * for fancy indexing (you need a server alias /icons to your Apache icons directory):
     */
    // note proper paths
    public static final Map<String, String> ICONS = 
        Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("< .. >", "/icons/back.gif");
            put("< folder >", "/icons/folder.gif");
            put("text/plain", "/icons/text.gif"); 
            put("text/html", "/icons/text.gif");
            put("application/zip", "/icons/compressed.gif"); 
            put("application/x-gzip", "/icons/compressed.gif");
            put("image/gif", "/icons/image2.gif"); 
            put("image/jpg", "/icons/image2.gif");
            put("image/png", "/icons/image2.gif"); 
            put("application/pdf", "/icons/pdf.gif"); 
            put("application/ps", "/icons/ps.gif");
            put("application/msword", "/icons/text.gif");
            put("application/vnd.ms-powerpoint", "/icons/world2.gif");
            put("application/vnd.ms-excel", "/icons/quill.gif");
            put("application/x-dvi", "/icons/dvi"); 
            put("text/x-chdr", "/icons/c.gif"); 
            put("text/x-csrc", "/icons/c.gif");
            put("video/x-msvideo", "/icons/movie.gif"); 
            put("video/x-ms-wmv", "/icons/movie.gif"); 
            put("video/ogg", "/icons/movie.gif");
            put("video/mpeg", "/icons/movie.gif"); 
            put("video/quicktime", "/icons/movie.gif");
            put("default", "/icons/unknown.gif");               
        }});        

    /*
     * -- ICON_WIDTH
     * specifies the icon width for the folder listings of the Web interface
     * DEFAULT: $ICON_WIDTH = 22;
     */
    public static final int ICON_WIDTH = 22;

    /*
     * -- FORBIDDEN_UID
     * a comman separated list of UIDs to block
     * (process id of this CGI will be checked against this list)
     * common "forbidden" UIDs: root, Apache process owner UID
     * DEFAULT: @FORBIDDEN_UID = ( 0 );
     */
    public final static ArrayList<String> FORBIDDEN_UID = new ArrayList<String>(Arrays.asList(
            "0" 
            ));    

    /*
     * -- HIDDEN
     * hide some special files/folders (GET/PROPFIND)
     * EXAMPLES: @HIDDEN = ( '.DAV/?$', '~$', '.bak$' );
     */
    public final static ArrayList<String> HIDDEN = new ArrayList<String>(Arrays.asList(
            "/.ht", 
            "/.DAV" 
            ));        

    /*
     * -- ALLOW_INFINITE_PROPFIND
     * enables/disables infinite PROPFIND requests
     * if disabled the default depth is set to 1
     */
    public static final boolean ALLOW_INFINITE_PROPFIND = true;

    /*
     * -- ALLOW_FILE_MANAGEMENT
     * enables file management with a web browser
     * ATTENTATION: locks will be ignored
     */
    public static volatile boolean ALLOW_FILE_MANAGEMENT = true;

    /*
     * -- ALLOW_SEARCH
     * enable file/folder search in the Web interface
     */
    public static final boolean ALLOW_SEARCH = true;

    /*
     * -- ALLOW_ZIP_UPLOAD
     * enable zip file upload (incl. extraction)
     */
    public static final boolean ALLOW_ZIP_UPLOAD = true;

    /*
     * -- ALLOW_ZIP_DOWNLOAD
     * enable zip file download
     */ 
    public static final boolean ALLOW_ZIP_DOWNLOAD = true;

    /*
     * -- SHOW_STAT
     * shows file statistics after file/folder list in the Web interface
     */
    public static final boolean SHOW_STAT = true;

    /*
     * -- PAGE_LIMIT
     * limits number of files/folders shown in the Web interface
     * EXAMPLE: $PAGE_LIMIT = 20;
     */
    public static final int PAGE_LIMIT = 15;

    /*
     * -- ALLOW_POST_UPLOADS
     * enables a upload form in a fancy index of a folder (browser access)
     * ATTENTATION: locks will be ignored
     * Apache configuration:
     * DEFAULT: $ALLOW_POST_UPLOADS = 1;
     */
    public static final boolean ALLOW_POST_UPLOADS = true;

    /*
     * -- POST_MAX_SIZE
     * maximum post size (only POST requests)
     * EXAMPLE: $POST_MAX_SIZE = 1073741824; # 1GB
     */
    public static final int POST_MAX_SIZE = 1073741824;
    // public static final int POST_MAX_SIZE = 10240000;

    /*
     * -- SHOW_QUOTA
     * enables/disables quota information for fancy indexing
     * DEFAULT: $SHOW_QUOTA = 0;
     */
    public static final boolean SHOW_QUOTA = true;

    /*
     * -- SHOW_PERM
     * show file permissions
     * DEFAULT: $SHOW_PERM = 0;
     */
    public static final boolean SHOW_PERM = true;

    /*
     * -- ALLOW_CHANGEPERM
     * allow users to change file permissions
     * DEFAULT: ALLOW_CHANGEPERM = 0;
     */
    public static final boolean ALLOW_CHANGEPERM = true;

    /*
     * -- ALLOW_CHANGEPERMRECURSIVE
     * allow users to change file/folder permissions recursively
     */
    public static final boolean ALLOW_CHANGEPERMRECURSIVE = true;

    /*
     * -- PERM_USER
     * if ALLOW_CHANGEPERM is set to 1 the PERM_USER variable
     * defines the file/folder permissions for user/owner allowed to change
     * EXAMPLE: $PERM_USER = [ 'r','w','x','s' ];
     */
    public final static ArrayList<String> PERM_USER = new ArrayList<String>(Arrays.asList("r", "w", "x", "s"));

    /*
     * -- PERM_GROUP
     * if ALLOW_CHANGEPERM is set to 1 the PERM_GROUP variable
     * defines the file/folder permissions for group allowed to change
     * EXMAMPLE: $PERM_GROUP = [ 'r','w','x','s' ];
     */
    public final static ArrayList<String> PERM_GROUP = new ArrayList<String>(Arrays.asList("r", "w", "x", "s"));

    /*
     * -- PERM_OTHERS
     * if ALLOW_CHANGEPERM is set to 1 the PERM_OTHERS variable
     * defines the file/folder permissions for other users allowed to change
     * EXAMPLE: $PERM_OTHERS = [ 'r','w','x','t' ];
     */
    public final static ArrayList<String> PERM_OTHERS = new ArrayList<String>(Arrays.asList("r", "w", "x", "t"));

    /*
     * -- HEADER
     * content after body tag in the Web interface
     */
    public static final String HEADER = "<div style=\"padding-left:3px;background-color:#444444;color:#ffffff;\">" +
    		"WebDAV CGI - Web interface: You are logged in as %s.</div>";

    /*
     * -- SIGNATURE
     * for fancy indexing
     * EXAMPLE: $SIGNATURE=$ENV{SERVER_SIGNATURE};
     */
    public static final String SIGNATURE = "<div style=\"padding-left:3px;background-color:#444444;color:#ffffff;\">&copy; ZE CMS, Humboldt-Universit&auml;t zu Berlin | Written 2010 by <a style=\"color:#ffffff;\" href=\"http://amor.cms.hu-berlin.de/~rohdedan/webdav/\">Daniel Rohde</a></div>";
    
    /*
     * -- DEFAULT_LOCK_OWNER
     * lock owner if not given by client
     * EXAMPLE: $DEFAULT_LOCK_OWNER=$ENV{REMOTE_USER}.'@'.$ENV{REMOTE_ADDR}; ## loggin user @ ip
     */
    // note %s in the value; have to be used with String.format
    public static final Map<String, Object> DEFAULT_LOCK_OWNER =
        Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("href", "%s@%s");
        }});              
    
    /*
     * -- CHARSET
     * change it if you get trouble with special characters
     * DEFAULT: $CHARSET='utf-8';
     */
    public static final String CHARSET = "utf-8";
  
    /*
     * -- LOGFILE
     * simple log for file/folder modifications (PUT/MKCOL/DELETE/COPY/MOVE)
     * EXAMPLE: $LOGFILE='/tmp/webdavcgi.log';
     */
    public static final String LOGFILE = "/tmp/webdavcgi.log";

    /*
     * -- GFSQUOTA
     * if you use a GFS/GFS2 filesystem and if you want quota property support set this variable
     * ($FS - will be replaced by the filesystem (filename/folder))
     * EXAMPLE: $GFSQUOTA='/usr/sbin/gfs2_quota -f';
     */
    public static final String GFSQUOTA= "/usr/sbin/gfs_quota -f";

    
    /*
     * -- ENABLE_LOCK
     * enable/disable lock/unlock support (WebDAV compiance class 2)
     * if disabled it's unsafe for shared collections/files but improves performance
     */ 
    public static final boolean ENABLE_LOCK = true;

    /*
     * -- ENABLE_ACL
     * enable ACL support: only Unix like read/write access changes for user/group/other are supported
     */
    public static final boolean ENABLE_ACL = true;

    /*
     * --- CURRENT_USER_PRINCIPAL
     * a virtual URI for ACL principals
     * for Apple's iCal &  Addressbook
     */
    // note %s in the value; have to be used with String.format
    public static final String CURRENT_USER_PRINCIPAL = "/principals/%s/";
    
    /*
     * -- PRINCIPAL_COLLECTION_SET
     * don't change it for MacOS X Addressbook support
     * DEFAULT: $PRINCIPAL_COLLECTION_SET = '/directory/';
     */
    public static final String PRINCIPAL_COLLECTION_SET = "/directory/";
    
    /*
     * -- ENABLE_CALDAV
     * enable CalDAV support for Lightning/Sunbird/iCal/iPhone calender/task support
     */
    public static final boolean ENABLE_CALDAV = true;

    /*
     * -- CALENDAR_HOME_SET
     * maps UID numbers or remote users (accounts) to calendar folders
     */
    public static final Map<String, String> CALENDAR_HOME_SET = 
        Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("default", "/");
            put("1000", "/caldav");
        }});      
    
    /*
     * -- ENABLE_CALDAV_SCHEDULE
     * really incomplete (ALPHA) - properties exists but POST requests are not supported yet
     */
    public static final boolean ENABLE_CALDAV_SCHEDULE = false;

    /*
     * -- ENABLE_CARDDAV
     * enable CardDAV support for Apple's Addressbook
     */
    public static final boolean ENABLE_CARDDAV = true;

    /*
     * -- ADDRESSBOOK_HOME_SET
     * maps UID numbers or remote users to addressbook folders
     */  
    public static final Map<String, String> ADDRESSBOOK_HOME_SET = 
        Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("default", "/");
            put("1000", "/carddav/");
        }});       
    
    /*
     * -- ENABLE_TRASH
     * enables the server-side trash can (don't forget to setup $TRASH_FOLDER)
     */
    public static final boolean ENABLE_TRASH = false;

    /*
     * -- TRASH_FOLDER
     * neccessary if you enable trash
     * it should be writable by your users (chmod a+rwxt <trash folder>)
     * EXAMPLE: $TRASH_FOLDER = '/tmp/trash';
     */
    public static final String TRASH_FOLDER = "/usr/local/www/var/trash";
    
    /*
     * -- ENABLE_GROUPDAV
     * enables GroupDAV (http://groupdav.org/draft-hess-groupdav-01.txt)
     * EXAMPLE: $ENABLE_GROUPDAV = 0;
     */
    public static final boolean ENABLE_GROUPDAV = true;

    /*
     * -- ENABLE_SEARCH
     * enables server-side search (WebDAV SEARCH/DASL, RFC5323)
     * EXAMPLE: $ENABLE_SEARCH = 1;
     */
    public static final boolean ENABLE_SEARCH = true;

    /*
     * -- ENABLE_THUMBNAIL
     * enables image thumbnail support for folder listings of the Web interface.
     * If enabled the default icons for images will be replaced by thumbnails
     * and if the mouse is over a icon the icon will be zoomed to the size of $THUMBNAIL_WIDTH.
     * DEFAULT: $ENABLE_THUMBNAIL = 0;
     */ 
    public static final boolean ENABLE_THUMBNAIL = true;

    /*
     * -- ENABLE_THUMBNAIL_CACHE
     * enable image thumbnail caching (improves performance - 2x faster)
     * DEFAULT: $ENABLE_THUMBNAIL_CACHE = 0;
     */
    public static final boolean ENABLE_THUMBNAIL_CACHE = true;

    /*
     * -- THUMBNAIL_WIDTH
     * defines the width of a image thumbnail
     */
    public static final int THUMBNAIL_WIDTH = 110;

    /*
     * -- THUMBNAIL_CACHEDIR
     * defines the path to a cache directory for image thumbnails
     * this is neccessary if you enable the thumbnail cache ($ENABLE_THUMBNAIL_CACHE)
     * EXAMPLE: $THUMBNAIL_CACHEDIR=".thumbs";
     */
    public static final String THUMBNAIL_CACHEDIR = "/usr/local/www/tmp/thumbs";
    
    /*
     * -- ENABLE_BIND
     * enables BIND/UNBIND/REBIND methods defined in http://tools.ietf.org/html/draft-ietf-webdav-bind-27
     */
    public static final boolean ENABLE_BIND = true;

    /*
     * -- DEBUG
     * enables/disables debug output
     * you can find the debug output in your web server error log
     */
    public static final boolean DEBUG = true;
/*
    ############  S E T U P - END ###########################################
    #########################################################################
*/    
    
    
/*
    ############  INITIALIZATION START ###########################################
    #########################################################################
*/    

    public static ArrayList<String> KNOWN_COLL_PROPS = null;
    public static ArrayList<String> KNOWN_ACL_PROPS = null;
    public static ArrayList<String> KNOWN_CALDAV_COLL_PROPS = null;
    public static ArrayList<String> KNOWN_CALDAV_FILE_PROPS = null;
    public static ArrayList<String> KNOWN_CARDDAV_COLL_PROPS = null;
    public static ArrayList<String> KNOWN_CARDDAV_FILE_PROPS = null;
    public static ArrayList<String> KNOWN_COLL_LIVE_PROPS = null;
    public static ArrayList<String> KNOWN_FILE_LIVE_PROPS = null;
    public static ArrayList<String> KNOWN_CALDAV_COLL_LIVE_PROPS = null;
    public static ArrayList<String> KNOWN_CALDAV_FILE_LIVE_PROPS = null;
    public static ArrayList<String> KNOWN_CARDDAV_COLL_LIVE_PROPS = null;
    public static ArrayList<String> KNOWN_CARDDAV_FILE_LIVE_PROPS = null;
    public static ArrayList<String> KNOWN_FILE_PROPS = null;
    public static ArrayList<String> UNSUPPORTED_PROPS = null;
    public static ArrayList<String> PROTECTED_PROPS = null;
    public static ArrayList<String> ALLPROP_PROPS = null;
    
    // # XML

    public static final Map<String, String> NAMESPACES = 
        Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("DAV:", "D");
            put("http://apache.org/dav/props/", "lp2");
            put("urn:schemas-microsoft-com:", "Z");
            put("urn:schemas-microsoft-com:datatypes", "M");
            put("urn:schemas-microsoft-com:office:office", "Office");
            put("http://schemas.microsoft.com/repl/", "Repl");
            put("urn:ietf:params:xml:ns:caldav", "C");
            put("http://calendarserver.org/ns/", "CS");
            put("http://www.apple.com/webdav_fs/props/", "Apple");
            put("http://www.w3.org/2000/xmlns/", "x");
            put("urn:ietf:params:xml:ns:carddav", "A");
            put("http://www.w3.org/2001/XMLSchema", "xs");
            put("http://groupdav.org/", "G");      
        }});      
    
    public static final Map<String, String> ELEMENTS = 
        Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("calendar", "C");
            put("calendar-description", "C");
            put("calendar-timezone", "C");
            put("supported-calendar-component-set", "C");
            put("supported-calendar-data", "C");
            put("max-resource-size", "C");
            put("min-date-time", "C");
            put("max-date-time", "C");
            put("max-instances", "C");
            put("max-attendees-per-instance", "C");
            put("read-free-busy", "C");
            put("calendar-home-set", "C");
            put("supported-collation-set", "C");
            put("schedule-tag", "C");
            put("calendar-data", "C");
            put("mkcalendar-response", "C"); 
            put("getctag", "CS");
            put("calendar-user-address-set", "C");
            put("schedule-inbox-URL", "C");
            put("schedule-outbox-URL", "C");
            put("calendar-user-type", "C");
            put("schedule-calendar-transp", "C");
            put("schedule-default-calendar-URL", "C");
            put("schedule-inbox", "C");
            put("schedule-outbox", "C");
            put("transparent", "C");
            put("calendar-multiget", "C");
            put("calendar-query", "C");
            put("free-busy-query", "C");
            put("addressbook", "A");
            put("addressbook-description", "A");
            put("supported-address-data", "A");
            put("addressbook-home-set", "A");
            put("principal-address", "A");
            put("address-data", "A");
            put("addressbook-query", "A");
            put("addressbook-multiget", "A");
            put("string", "xs");
            put("anyURI", "xs");
            put("nonNegativeInteger", "xs");
            put("dateTime", "xs");
            put("vevent-collection", "G");
            put("vtodo-collection", "G");
            put("vcard-collection", "G");
            put("component-set", "G");
            put("executable", "lp2");
            put("Win32CreationTime", "Z");
            put("Win32LastModifiedTime", "Z");
            put("Win32LastAccessTime", "Z"); 
            put("authoritative-directory", "Repl");
            put("resourcetag", "Repl");
            put("repl-uid", "Repl");
            put("modifiedby", "Office");
            put("specialFolderType", "Office");
            put("Win32CreationTime", "Z");
            put("Win32FileAttributes", "Z");
            put("Win32LastAccessTime", "Z");
            put("Win32LastModifiedTime", "Z");
            put("default", "D");          
        }});        
    
    public static final Map<String, String> NAMESPACEABBR = 
        Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("D", "DAV:");
            put("lp2", "http://apache.org/dav/props/");
            put("Z", "urn:schemas-microsoft-com:");
            put("Office", "urn:schemas-microsoft-com:office:office");
            put("Repl", "http://schemas.microsoft.com/repl/");
            put("M", "urn:schemas-microsoft-com:datatypes");
            put("C", "urn:ietf:params:xml:ns:caldav");
            put("CS", "http://calendarserver.org/ns/");
            put("Apple", "http://www.apple.com/webdav_fs/props/");
            put("A", "urn:ietf:params:xml:ns:carddav");
            put("xs", "http://www.w3.org/2001/XMLSchema");
            put("G", "http://groupdav.org/");         
        }});       
    
    public static final Map<String, String> DATATYPES = 
        Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("isfolder", "M:dt=\"boolean\"");
            put("ishidden", "M:dt=\"boolean\"");
            put("isstructureddocument", "M:dt=\"boolean\"");
            put("hassubs", "M:dt=\"boolean\"");
            put("nosubs", "M:dt=\"boolean\"");    
            put("reserved", "M:dt=\"boolean\"");
            put("iscollection ", "M:dt=\"boolean\"");
            put("isFolder", "M:dt=\"boolean\"'");
            put("isreadonly", "M:dt=\"boolean\"");
            put("isroot", "M:dt=\"boolean\"");
            put("lastaccessed", "M:dt=\"dateTime\"");
            put("Win32CreationTime", "M:dt=\"dateTime\"");
            put("Win32LastAccessTime", "M:dt=\"dateTime\"");
            put("Win32LastModifiedTime", "M:dt=\"dateTime\"");
            put("description", "xml:lang=\"en\"");            
        }});      
    
    public static volatile Map<String, Integer> NAMESPACEELEMENTS = new HashMap<String, Integer>() {{ 
            put("multistatus", 1);
            put("prop", 1);
            put("error", 1);
            put("principal-search-property-set", 1);
        }};    
        
    public static final Map<String, Integer> ELEMENTORDER = 
        Collections.unmodifiableMap(new HashMap<String, Integer>() {{ 
            put("multistatus", 1);
            put("responsedescription", 4);
            put("allprop", 1);
            put("include", 2);
            put("prop", 1);
            put("propstat", 2);
            put("status", 3);
            put("error", 4);
            put("href", 1);
            put("responsedescription", 5);
            put("location", 6);
            put("locktype", 1);
            put("lockscope", 2);
            put("depth", 3);
            put("owner", 4);
            put("timeout", 5);
            put("locktoken", 6);
            put("lockroot", 7);
            put("getcontentlength", 1001);
            put("getlastmodified", 1002);
            put("resourcetype", 0);
            put("getcontenttype", 1);
            put("supportedlock", 1010);
            put("lockdiscovery", 1011);
            put("src", 1);
            put("dst", 2);
            put("principal", 1);
            put("grant", 2);
            put("privilege", 1);
            put("abstract", 2);
            put("description", 3);
            put("supported-privilege", 4);
            put("collection", 1);
            put("calendar", 2);
            put("schedule-inbox", 3);
            put("schedule-outbox", 4);
            put("calendar-data", 101);
            put("getetag", 100);
            put("properties", 1);
            put("operators", 2);
            put("default", 1000);
        }});       
    
    public static final Map<String, String> SEARCH_PROPTYPES = 
        Collections.unmodifiableMap(new HashMap<String, String>() {{ 
            put("default", "string");
            put("{DAV:}getlastmodified", "dateTime");
            put("{DAV:}lastaccessed", "dateTime");
            put("{DAV:}getcontentlength", "int");
            put("{DAV:}creationdate", "dateTime");
            put("{urn:schemas-microsoft-com:}Win32CreationTime", "dateTime");
            put("{urn:schemas-microsoft-com:}Win32LastAccessTime", "dateTime" );
            put("{urn:schemas-microsoft-com:}Win32LastModifiedTime", "dateTime");
            put("{DAV:}childcount", "int");
            put("{DAV:}objectcount", "int");
            put("{DAV:}visiblecount", "int");
            put("{DAV:}acl", "xml");
            put("{DAV:}acl-restrictions", "c");
            put("{urn:ietf:params:xml:ns:carddav}addressbook-home-set", "xml");
            put("{urn:ietf:params:xml:ns:caldav}calendar-home-set", "xml");
            put("{DAV:}current-user-principal}", "xml");
            put("{DAV:}current-user-privilege-set", "xml");
            put("{DAV:}group", "xml");
            put("{DAV:}owner", "xml");
            put("{urn:ietf:params:xml:ns:carddav}principal-address", "xml");
            put("{DAV:}principal-collection-set", "xml");
            put("{DAV:}principal-URL", "xml");
            put("{DAV:}resourcetype", "xml");
            put("{urn:ietf:params:xml:ns:caldav}schedule-calendar-transp", "xml");
            put("{urn:ietf:params:xml:ns:caldav}schedule-inbox-URL", "xml");
            put("{urn:ietf:params:xml:ns:caldav}schedule-outbox-URL", "xml");
            put("{DAV:}source", "xml");
            put("{urn:ietf:params:xml:ns:carddav}supported-address-data", "xml");
            put("{urn:ietf:params:xml:ns:caldav}supported-calendar-component-set", "xml");
            put("{urn:ietf:params:xml:ns:caldav}supported-calendar-data", "xml");
            put("{DAV:}supported-method-set", "xml");
            put("{DAV:}supported-privilege-set", "xml");
            put("{DAV:}supported-report-set", "xml");
            put("{DAV:}supportedlock", "xml");
        }});     
    
    public static final Map<String, String> SEARCH_SPECIALCONV = 
        Collections.unmodifiableMap(new HashMap<String, String>() {{ 
            put("dateTime", "str2time");
            put("xml", "convXML2Str");
        }});
    
    public static final Map<String, HashMap<String, String>> SEARCH_SPECIALOPS = 
        Collections.unmodifiableMap(new HashMap<String, HashMap<String, String>>() {{ 
            put("int", new HashMap<String, String>() {{
                put("eq", "==");
                put("gt", ">");
                put("lt", "<");
                put("gte", ">=");
                put("lte", "<=");
                put("cmp", "<=>");
            }});
            put("dateTime", new HashMap<String, String>() {{
                put("eq", "==");
                put("gt", ">");
                put("lt", "<");
                put("gte", ">=");
                put("lte", "<=");
                put("cmp", "<=>");
            }});
            put("string", new HashMap<String, String>() {{
                put("lte", "le");
                put("gte", "ge");
            }});
        }});       
    
    public final static ArrayList<String> IGNORE_PROPS = new ArrayList<String>(Arrays.asList(
            "xmlns", 
            "CS" 
            ));    
    
    public static String DAV = null;
    public static volatile Properties properties = null;
    public static volatile WebDAVLocks locks = null;
    
    // phrases
    public static HashMap<String, String> stringMessages = null;
    
    public static final String URL_PARAM_SEPARATOR = "&"; // was ";" in perl
    
    public static final String PROPERTIES_FILE_PATH = "properties.dat";
    public static final String LOCKS_FILE_PATH = "locks.dat";
    

}
