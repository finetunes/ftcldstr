package net.finetunes.ftcldstr.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Config class to store global configuration values
 *
 */
public class ConfigService {
    
    // TODO: add all the config values here
    
/*    
    ## -- VIRTUAL_BASE
    ## only neccassary if you use redirects or rewrites 
    ## from a VIRTUAL_BASE to the DOCUMENT_ROOT
    ## regular expressions are allowed
    ## DEFAULT: $VIRTUAL_BASE = "";
    $VIRTUAL_BASE = '(/webdav|)';

    ## -- DOCUMENT_ROOT
    ## by default the server document root
    ## (don't forget a trailing slash '/'):
    $DOCUMENT_ROOT = $ENV{DOCUMENT_ROOT};

    ## -- UMASK
    ## mask for file/folder creation 
    ## (it does not change permission of existing files/folders):
    ## DEFAULT: $UMASK = 0002; # read/write/execute for users and groups, others get read/execute permissions
    $UMASK = 0022;

    ## -- MIMETYPES
    ## some MIME types for Web browser access and GET access
    ## you can add some missing types ('extension list' => 'mime-type'):
    %MIMETYPES = (
        'html htm shtm shtml' => 'text/html',
        'css' => 'text/css', 'xml xsl'=>'text/xml',
        'js' => 'application/x-javascript',
        'txt asc pl cgi php php3 php4 php5 php6 csv log out java jsp tld tag' => 'text/plain',
        'c'=> 'text/x-csrc', 'h'=>'text/x-chdr',
        'gif'=>'image/gif', 'jpeg jpg jpe'=>'image/jpg', 
        'png'=>'image/png', 'bmp'=>'image/bmp', 'tiff'=>'image/tiff',
        'pdf'=>'application/pdf', 'ps'=>'application/ps',
        'dvi'=>'application/x-dvi','tex'=>'application/x-tex',
        'zip'=>'application/zip', 'tar'=>'application/x-tar','gz'=>'application/x-gzip',
        'doc dot' => 'application/msword',
        'xls xlm xla xlc xlt xlw' => 'application/vnd.ms-excel',
        'ppt pps pot'=>'application/vnd.ms-powerpoint',
        'pptx'=>'application/vnd.openxmlformats-officedocument.presentationml.presentation',
        'ics' => 'text/calendar',
        'avi' => 'video/x-msvideo', 'wmv' => 'video/x-ms-wmv', 'ogv'=>'video/ogg',
        'mpeg mpg mpe' => 'video/mpeg', 'qt mov'=>'video/quicktime',
        default => 'application/octet-stream',
        ); 
*/        
        
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
    // TODO: set proper paths
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
     * ## -- FORBIDDEN_UID
     * ## a comman separated list of UIDs to block
     * ## (process id of this CGI will be checked against this list)
     * ## common "forbidden" UIDs: root, Apache process owner UID
     * ## DEFAULT: @FORBIDDEN_UID = ( 0 );
     */
    // TODO
    // @FORBIDDEN_UID = ( 0 );

    /*
    ## -- HIDDEN 
    ## hide some special files/folders (GET/PROPFIND) 
    ## EXAMPLES: @HIDDEN = ( '.DAV/?$', '~$', '.bak$' );
    @HIDDEN = ('/.ht','/.DAV');
*/

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
    public static final boolean ALLOW_FILE_MANAGEMENT = true;

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


//    PZ: no language support
//    ## -- LANGSWITCH
//    ## a simple language switch
//    $LANGSWITCH = '<div style="font-size:0.6em;text-align:right;border:0px;padding:0px;"><a href="?lang=default">[EN]</a> <a href="?lang=de">[DE]</a></div>';

    /*
     * -- HEADER
     * content after body tag in the Web interface
     */
    public static final String HEADER = "<div style=\"padding-left:3px;background-color:#444444;color:#ffffff;\">" +
    		"WebDAV CGI - Web interface: You are logged in as " + 
            // TODO: get current user name
            // ($ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER}) + 
    		"<b>TODO</b>" + 
    		".</div>";

    /*
     * -- SIGNATURE
     * for fancy indexing
     * EXAMPLE: $SIGNATURE=$ENV{SERVER_SIGNATURE};
     */
    public static final String SIGNATURE = "<div style=\"padding-left:3px;background-color:#444444;color:#ffffff;\">&copy; ZE CMS, Humboldt-Universit&auml;t zu Berlin | Written 2010 by <a style=\"color:#ffffff;\" href=\"http://amor.cms.hu-berlin.de/~rohdedan/webdav/\">Daniel Rohde</a></div>";

/*
    ## -- LANG
    ## defines the default language for the Web interface
    ## see %TRANSLATION option for supported languages
    ## DEFAULT: $LANG='default';
    $LANG = 'default';
    #$LANG = 'de';

    // TRANSLATION WAS HERE
    

    ## -- DBI_(SRC/USER/PASS)
    ## database setup for LOCK/UNLOCK/PROPPATCH/PROPFIND data
    ## EXAMPLE: $DBI_SRC='dbi:SQLite:dbname=/tmp/webdav.'.($ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER}).'.db';
    ## ATTENTION: if users share the same folder they should use the same database. The example works only for users with unshared folders and $CREATE_DB should be enabled.
    $DBI_SRC='dbi:SQLite:dbname=/usr/local/www/var/webdav/webdav.'.($ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER}).'.db';
    $DBI_USER="";
    $DBI_PASS="";

    ## enables persitent database connection (only usefull in conjunction with mod_perl, Speedy/PersistenPerl)
    $DBI_PERSISTENT = 0;

    ## -- CREATE_DB
    ## if set to 1 this script creates the database schema ($DB_SCHEMA)
    ## performance hint: if the database schema exists set CREATE_DB to 0
    ## DEFAULT: $CREATE_DB = 1;
    $CREATE_DB = 1;

    ## -- DB_SCHEMA
    ## database schema (works with SQlite3 & MySQL5)
    ## WARNING!!! do not use a unique index 
    @DB_SCHEMA = (
        'CREATE TABLE IF NOT EXISTS webdav_locks (basefn VARCHAR(255) NOT NULL, fn VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL, scope VARCHAR(255), token VARCHAR(255) NOT NULL, depth VARCHAR(255) NOT NULL, timeout VARCHAR(255) NULL, owner TEXT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)',
        'CREATE TABLE IF NOT EXISTS webdav_props (fn VARCHAR(255) NOT NULL, propname VARCHAR(255) NOT NULL, value TEXT)',
        'CREATE INDEX IF NOT EXISTS webdav_locks_idx1 ON webdav_locks (fn)',
        'CREATE INDEX IF NOT EXISTS webdav_locks_idx2 ON webdav_locks (basefn)',
        'CREATE INDEX IF NOT EXISTS webdav_locks_idx3 ON webdav_locks (fn,basefn)',
        'CREATE INDEX IF NOT EXISTS webdav_locks_idx4 ON webdav_locks (fn,basefn,token)',
        'CREATE INDEX IF NOT EXISTS webdav_props_idx1 ON webdav_props (fn)',
        'CREATE INDEX IF NOT EXISTS webdav_props_idx2 ON webdav_props (fn,propname)',
        );

    ## -- DEFAULT_LOCK_OWNER
    ## lock owner if not given by client
    ## EXAMPLE: $DEFAULT_LOCK_OWNER=$ENV{REMOTE_USER}.'@'.$ENV{REMOTE_ADDR}; ## loggin user @ ip
    $DEFAULT_LOCK_OWNER= { href=> ($ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER}).'@'.$ENV{REMOTE_ADDR} };
*/
    
    /*
     * -- CHARSET
     * change it if you get trouble with special characters
     * DEFAULT: $CHARSET='utf-8';
     */
    public static final String CHARSET = "utf-8";
/*    
    # and Perl's UTF-8 pragma for the right string length:
    # use utf8;
    # no utf8;

    
    ## -- BUFSIZE
    ## buffer size for read and write operations
    $BUFSIZE = 1073741824;

    ## -- LOGFILE
    ## simple log for file/folder modifications (PUT/MKCOL/DELETE/COPY/MOVE)
    ## EXAMPLE: $LOGFILE='/tmp/webdavcgi.log';
    # $LOGFILE='/tmp/webdavcgi.log';

    ## -- GFSQUOTA
    ## if you use a GFS/GFS2 filesystem and if you want quota property support set this variable
    ## ($FS - will be replaced by the filesystem (filename/folder))
    ## EXAMPLE: $GFSQUOTA='/usr/sbin/gfs2_quota -f';
    $GFSQUOTA='/usr/sbin/gfs_quota -f';
*/
    
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
    ## --- CURRENT_USER_PRINCIPAL
    ## a virtual URI for ACL principals
    ## for Apple's iCal &  Addressbook
    $CURRENT_USER_PRINCIPAL = "/principals/".($ENV{REDIRECT_REMOTE_USER} || $ENV{REMOTE_USER}) .'/';

    ## -- PRINCIPAL_COLLECTION_SET 
    ## don't change it for MacOS X Addressbook support
    ## DEFAULT: $PRINCIPAL_COLLECTION_SET = '/directory/';
    $PRINCIPAL_COLLECTION_SET = '/directory/';
*/
    
    /*
     * -- ENABLE_CALDAV
     * enable CalDAV support for Lightning/Sunbird/iCal/iPhone calender/task support
     */
    public static final boolean ENABLE_CALDAV = true;

/*
    ## -- CALENDAR_HOME_SET
    ## maps UID numbers or remote users (accounts) to calendar folders
    %CALENDAR_HOME_SET = ( default=> '/', 1000 =>  '/caldav'  );
*/

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
    ## -- ADDRESSBOOK_HOME_SET
    ## maps UID numbers or remote users to addressbook folders 
    %ADDRESSBOOK_HOME_SET = ( default=> '/',  1000 => '/carddav/'  );
*/
    
    /*
     * -- ENABLE_TRASH
     * enables the server-side trash can (don't forget to setup $TRASH_FOLDER)
     */
    public static final boolean ENABLE_TRASH = false;

/*
 * PZ: the variable must have the trailing slash if a directory
 * 
    ## -- TRASH_FOLDER
    ## neccessary if you enable trash 
    ## it should be writable by your users (chmod a+rwxt <trash folder>)
    ## EXAMPLE: $TRASH_FOLDER = '/tmp/trash';
    $TRASH_FOLDER = '/usr/local/www/var/trash';
*/
    
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
    // TODO: PZ: set the proper path here 
    // public static final String THUMBNAIL_CACHEDIR = "/usr/local/www/tmp/thumbs";
    public static final String THUMBNAIL_CACHEDIR = "e:\\webdav\\thumbs";
    
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
    public static final boolean DEBUG = false;
/*
    ############  S E T U P - END ###########################################
    #########################################################################
*/    
    
    
/*
    ############  INITIALIZATION START ###########################################
    #########################################################################
*/    

/*
    @KNOWN_COLL_PROPS = ( 
            'creationdate', 'displayname','getcontentlanguage', 
            'getlastmodified', 'lockdiscovery', 'resourcetype', 
            'getetag', 'getcontenttype',
            'supportedlock', 'source',
            'quota-available-bytes', 'quota-used-bytes', 'quota', 'quotaused',
            'childcount', 'id', 'isfolder', 'ishidden', 'isstructureddocument',
            'hassubs', 'nosubs', 'objectcount', 'reserved', 'visiblecount',
            'iscollection', 'isFolder', 
            'authoritative-directory', 'resourcetag', 'repl-uid',
            'modifiedby', 
            'Win32CreationTime', 'Win32FileAttributes', 'Win32LastAccessTime', 'Win32LastModifiedTime', 
            'name','href', 'parentname', 'isreadonly', 'isroot', 'getcontentclass', 'lastaccessed', 'contentclass',
            'supported-report-set', 'supported-method-set',
            );
    @KNOWN_ACL_PROPS = (
                'owner','group','supported-privilege-set', 'current-user-privilege-set', 'acl', 'acl-restrictions',
                'inherited-acl-set', 'principal-collection-set', 'current-user-principal'
                  );
    @KNOWN_CALDAV_COLL_PROPS = (
                'calendar-description', 'calendar-timezone', 'supported-calendar-component-set',
                'supported-calendar-data', 'max-resource-size', 'min-date-time',
                'max-date-time', 'max-instances', 'max-attendees-per-instance',
                'getctag',
                    'principal-URL', 'calendar-home-set', 'schedule-inbox-URL', 'schedule-outbox-URL',
                'calendar-user-type', 'schedule-calendar-transp', 'schedule-default-calendar-URL',
                'schedule-tag', 'calendar-user-address-set',
                );
    @KNOWN_CALDAV_FILE_PROPS = ( 'calendar-data' );
    
    @KNOWN_CARDDAV_COLL_PROPS = ('addressbook-description', 'supported-address-data', 'addressbook-home-set', 'principal-address');
    @KNOWN_CARDDAV_FILE_PROPS = ('address-data');
    
    @KNOWN_COLL_LIVE_PROPS = ( );
    @KNOWN_FILE_LIVE_PROPS = ( );
    @KNOWN_CALDAV_COLL_LIVE_PROPS = ( 'resourcetype', 'displayname', 'calendar-description', 'calendar-timezone', 'calendar-user-address-set');
    @KNOWN_CALDAV_FILE_LIVE_PROPS = ( );
    @KNOWN_CARDDAV_COLL_LIVE_PROPS = ( 'addressbook-description');
    @KNOWN_CARDDAV_FILE_LIVE_PROPS = ( );
    
    push @KNOWN_COLL_LIVE_PROPS, @KNOWN_CALDAV_COLL_LIVE_PROPS if $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE || $ENABLE_CARDDAV;
    push @KNOWN_FILE_LIVE_PROPS, @KNOWN_CALDAV_FILE_LIVE_PROPS if $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE || $ENABLE_CARDDAV;
    push @KNOWN_COLL_LIVE_PROPS, @KNOWN_CARDDAV_COLL_LIVE_PROPS if $ENABLE_CARDDAV;
    push @KNOWN_COLL_PROPS, @KNOWN_ACL_PROPS if $ENABLE_ACL || $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE || $ENABLE_CARDDAV;
    push @KNOWN_COLL_PROPS, @KNOWN_CALDAV_COLL_PROPS if $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE;
    push @KNOWN_COLL_PROPS, @KNOWN_CARDDAV_COLL_PROPS if $ENABLE_CARDDAV;
    push @KNOWN_COLL_PROPS, 'resource-id' if $ENABLE_BIND;
    
    
    @KNOWN_FILE_PROPS = ( @KNOWN_COLL_PROPS, 'getcontentlength', 'executable' );
    push @KNOWN_FILE_PROPS, @KNOWN_CALDAV_FILE_PROPS if $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE;
    push @KNOWN_FILE_PROPS, @KNOWN_CARDDAV_FILE_PROPS if $ENABLE_CARDDAV;
    
    push @KNOWN_COLL_PROPS, 'component-set' if $ENABLE_GROUPDAV;
    
    @UNSUPPORTED_PROPS = ( 'checked-in', 'checked-out', 'xmpp-uri', 'dropbox-home-URL' ,'appledoubleheader','parent-set' );
    
    @PROTECTED_PROPS = ( @UNSUPPORTED_PROPS, 
                'getcontentlength', 'getcontenttype', 'getetag', 'lockdiscovery', 
                'source', 'supportedlock',
                'supported-report-set',
                'quota-available-bytes, quota-used-bytes', 'quota', 'quotaused',
                'childcount', 'id', 'isfolder', 'ishidden', 'isstructureddocument', 
                'hassubs', 'nosubs', 'objectcount', 'reserved', 'visiblecount',
                'iscollection', 'isFolder',
                'authoritative-directory', 'resourcetag', 'repl-uid',
                'modifiedby', 
                'name', 'href', 'parentname', 'isreadonly', 'isroot', 'getcontentclass', 'contentclass',
                'owner', 'group', 'supported-privilege-set', 'current-user-privilege-set', 
                'acl', 'acl-restrictions', 'inherited-acl-set', 'principal-collection-set',
                'supported-calendar-component-set','supported-calendar-data', 'max-resource-size',
                'min-date-time','max-date-time','max-instances','max-attendees-per-instance', 'getctag',
                'current-user-principal', 
                'calendar-user-address-set', 'schedule-inbox-URL', 'schedule-outbox-URL', 'schedule-calendar-transp',
                'schedule-default-calendar-URL', 'schedule-tag', 'supported-address-data', 
                'supported-collation-set', 'supported-method-set', 'supported-method',
                'supported-query-grammar'
            );
    
    @ALLPROP_PROPS = ( 'creationdate', 'displayname', 'getcontentlanguage', 'getlastmodified', 
                'lockdiscovery', 'resourcetype','supportedlock', 'getetag', 'getcontenttype', 
                'getcontentlength', 'executable' );
    
    
    ### XML
    %NAMESPACES = ( 'DAV:'=>'D', 'http://apache.org/dav/props/'=>'lp2', 'urn:schemas-microsoft-com:' => 'Z', 'urn:schemas-microsoft-com:datatypes'=>'M', 'urn:schemas-microsoft-com:office:office' => 'Office', 'http://schemas.microsoft.com/repl/' => 'Repl', 'urn:ietf:params:xml:ns:caldav'=>'C', 'http://calendarserver.org/ns/'=>'CS', 'http://www.apple.com/webdav_fs/props/'=>'Apple', 'http://www.w3.org/2000/xmlns/'=>'x', 'urn:ietf:params:xml:ns:carddav' => 'A', 'http://www.w3.org/2001/XMLSchema'=>'xs', 'http://groupdav.org/'=>'G');
    
    %ELEMENTS = (   'calendar'=>'C','calendar-description'=>'C', 'calendar-timezone'=>'C', 'supported-calendar-component-set'=>'C',
            'supported-calendar-data'=>'C', 'max-resource-size'=>'C', 'min-date-time'=>'C',
            'max-date-time'=>'C','max-instances'=>'C', 'max-attendees-per-instance'=>'C',
            'read-free-busy'=>'C', 'calendar-home-set'=>'C', 'supported-collation-set'=>'C', 'schedule-tag'=>'C',
            'calendar-data'=>'C', 'mkcalendar-response'=>'C', getctag=>'CS',
            'calendar-user-address-set'=>'C', 'schedule-inbox-URL'=>'C', 'schedule-outbox-URL'=>'C',
            'calendar-user-type'=>'C', 'schedule-calendar-transp'=>'C', 'schedule-default-calendar-URL'=>'C',
            'schedule-inbox'=>'C', 'schedule-outbox'=>'C', 'transparent'=>'C',
            'calendar-multiget'=>'C', 'calendar-query'=>'C', 'free-busy-query'=>'C',
            'addressbook'=>'A', 'addressbook-description'=>'A', 'supported-address-data'=>'A', 'addressbook-home-set'=>'A', 'principal-address'=>'A',
            'address-data'=>'A',
            'addressbook-query'=>'A', 'addressbook-multiget'=>'A',
            'string'=>'xs', 'anyURI'=>'xs', 'nonNegativeInteger'=>'xs', 'dateTime'=>'xs',
            'vevent-collection'=>'G', 'vtodo-collection'=>'G', 'vcard-collection'=>'G', 'component-set'=>'G',
            'executable'=>'lp2','Win32CreationTime'=>'Z', 'Win32LastModifiedTime'=>'Z', 'Win32LastAccessTime'=>'Z', 
            'authoritative-directory'=>'Repl', 'resourcetag'=>'Repl', 'repl-uid'=>'Repl', 'modifiedby'=>'Office', 'specialFolderType'=>'Office',
            'Win32CreationTime'=>'Z', 'Win32FileAttributes'=>'Z', 'Win32LastAccessTime'=>'Z', 'Win32LastModifiedTime'=>'Z',default=>'D' );
    
    %NAMESPACEABBR = ( 'D'=>'DAV:', 'lp2'=>'http://apache.org/dav/props/', 'Z'=>'urn:schemas-microsoft-com:', 'Office'=>'urn:schemas-microsoft-com:office:office','Repl'=>'http://schemas.microsoft.com/repl/', 'M'=>'urn:schemas-microsoft-com:datatypes', 'C'=>'urn:ietf:params:xml:ns:caldav', 'CS'=>'http://calendarserver.org/ns/', 'Apple'=>'http://www.apple.com/webdav_fs/props/', 'A'=> 'urn:ietf:params:xml:ns:carddav', 'xs'=>'http://www.w3.org/2001/XMLSchema', 'G'=>'http://groupdav.org/');
    
    %DATATYPES = ( isfolder=>'M:dt="boolean"', ishidden=>'M:dt="boolean"', isstructureddocument=>'M:dt="boolean"', hassubs=>'M:dt="boolean"', nosubs=>'M:dt="boolean"', reserved=>'M:dt="boolean"', iscollection =>'M:dt="boolean"', isFolder=>'M:dt="boolean"', isreadonly=>'M:dt="boolean"', isroot=>'M:dt="boolean"', lastaccessed=>'M:dt="dateTime"', Win32CreationTime=>'M:dt="dateTime"',Win32LastAccessTime=>'M:dt="dateTime"',Win32LastModifiedTime=>'M:dt="dateTime"', description=>'xml:lang="en"');
*/
    
    public static final Map<String, String> NAMESPACEELEMENTS = 
        Collections.unmodifiableMap(new HashMap<String, String>() {{ 
            put("multistatus", "1");
            put("prop", "1");
            put("error", "1");
            put("principal-search-property-set", "1");
        }});    
    
/*    
    %ELEMENTORDER = ( multistatus=>1, responsedescription=>4, 
                allprop=>1, include=>2,
                prop=>1, propstat=>2,status=>3, error=>4,
                href=>1, responsedescription=>5, location=>6,
                locktype=>1, lockscope=>2, depth=>3, owner=>4, timeout=>5, locktoken=>6, lockroot=>7, 
                getcontentlength=>1001, getlastmodified=>1002, 
                resourcetype=>0,
                getcontenttype=>1, 
                supportedlock=>1010, lockdiscovery=>1011, 
                src=>1,dst=>2,
                principal => 1, grant => 2,
                privilege => 1, abstract=> 2, description => 3, 'supported-privilege' => 4,
                collection=>1, calendar=>2, 'schedule-inbox'=>3, 'schedule-outbox'=>4,
                'calendar-data'=>101, getetag=>100,
                properties => 1, operators=>2,
                default=>1000);
    %SEARCH_PROPTYPES = ( default=>'string',
                  '{DAV:}getlastmodified'=> 'dateTime', '{DAV:}lastaccessed'=>'dateTime', '{DAV:}getcontentlength' => 'int', 
                  '{DAV:}creationdate' => 'dateTime','{urn:schemas-microsoft-com:}Win32CreationTime' =>'dateTime', 
                  '{urn:schemas-microsoft-com:}Win32LastAccessTime'=>'dateTime',  '{urn:schemas-microsoft-com:}Win32LastModifiedTime'=>'dateTime',
                  '{DAV:}childcount'=>'int', '{DAV:}objectcount'=>'int','{DAV:}visiblecount'=>'int',
                  '{DAV:}acl'=>'xml', '{DAV:}acl-restrictions'=>'xml','{urn:ietf:params:xml:ns:carddav}addressbook-home-set'=>'xml',
                  '{urn:ietf:params:xml:ns:caldav}calendar-home-set'=>'xml', '{DAV:}current-user-principal}'=>'xml',
                  '{DAV:}current-user-privilege-set'=>'xml', '{DAV:}group'=>'xml',
                  '{DAV:}owner'=>'xml', '{urn:ietf:params:xml:ns:carddav}principal-address'=>'xml',
                  '{DAV:}principal-collection-set'=>'xml', '{DAV:}principal-URL'=>'xml',
                  '{DAV:}resourcetype'=>'xml', '{urn:ietf:params:xml:ns:caldav}schedule-calendar-transp'=>'xml',
                  '{urn:ietf:params:xml:ns:caldav}schedule-inbox-URL'=>'xml', '{urn:ietf:params:xml:ns:caldav}schedule-outbox-URL'=>'xml',
                  '{DAV:}source'=>'xml', '{urn:ietf:params:xml:ns:carddav}supported-address-data'=>'xml',
                  '{urn:ietf:params:xml:ns:caldav}supported-calendar-component-set'=>'xml','{urn:ietf:params:xml:ns:caldav}supported-calendar-data'=>'xml',
                  '{DAV:}supported-method-set'=>'xml','{DAV:}supported-privilege-set'=>'xml','{DAV:}supported-report-set'=>'xml',
                  '{DAV:}supportedlock'=>'xml'
                );
    %SEARCH_SPECIALCONV = ( dateTime => 'str2time', xml=>'convXML2Str' );
    %SEARCH_SPECIALOPS = ( int => { eq => '==', gt => '>', lt =>'<', gte=>'>=', lte=>'<=', cmp=>'<=>' }, 
                               dateTime => { eq => '==', gt => '>', lt =>'<', gte=>'>=', lte=>'<=', cmp=>'<=>' }, 
                               string => { lte=>'le', gte=>'ge' } );
    
    @IGNORE_PROPS = ( 'xmlns', 'CS');
*/
    
    
    
    
	
//	public static String getValue(String parameter) {
//		
//		// TODO: implement
//		return null;
//		
//	}
    
    public static String DAV = null;
    
    // phrases
    public static HashMap<String, String> stringMessages = null;
    

}
