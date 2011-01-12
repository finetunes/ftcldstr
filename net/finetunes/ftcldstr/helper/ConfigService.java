package net.finetunes.ftcldstr.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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

    // TODO: Translate paths and hash
    /*
    ## -- ICONS
    ## for fancy indexing (you need a server alias /icons to your Apache icons directory):
    %ICONS = (
        '< .. >' => '/icons/back.gif',
        '< folder >' => '/icons/folder.gif',
        'text/plain' => '/icons/text.gif', 'text/html' => '/icons/text.gif',
        'application/zip'=> '/icons/compressed.gif', 'application/x-gzip'=>'/icons/compressed.gif',
        'image/gif'=>'/icons/image2.gif', 'image/jpg'=>'/icons/image2.gif',
        'image/png'=>'/icons/image2.gif', 
        'application/pdf'=>'/icons/pdf.gif', 'application/ps' =>'/icons/ps.gif',
        'application/msword' => '/icons/text.gif',
        'application/vnd.ms-powerpoint' => '/icons/world2.gif',
        'application/vnd.ms-excel' => '/icons/quill.gif',
        'application/x-dvi'=>'/icons/dvi', 'text/x-chdr' =>'/icons/c.gif', 'text/x-csrc'=>'/icons/c.gif',
        'video/x-msvideo'=>'/icons/movie.gif', 'video/x-ms-wmv'=>'/icons/movie.gif', 'video/ogg'=>'/icons/movie.gif',
        'video/mpeg'=>'/icons/movie.gif', 'video/quicktime'=>'/icons/movie.gif',
        default => '/icons/unknown.gif',
    );
    */

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
