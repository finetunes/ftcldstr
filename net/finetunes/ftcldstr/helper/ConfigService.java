package net.finetunes.ftcldstr.helper;

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

    ## -- ALLOW_INFINITE_PROPFIND
    ## enables/disables infinite PROPFIND requests
    ## if disabled the default depth is set to 1
    $ALLOW_INFINITE_PROPFIND = 1;

    ## -- ALLOW_FILE_MANAGEMENT
    ## enables file management with a web browser
    ## ATTENTATION: locks will be ignored
    $ALLOW_FILE_MANAGEMENT = 1;

    ## -- ALLOW_SEARCH
    ## enable file/folder search in the Web interface
    $ALLOW_SEARCH = 1;

    ## -- ALLOW_ZIP_UPLOAD
    ## enable zip file upload (incl. extraction)
    $ALLOW_ZIP_UPLOAD = 1;

    ## -- ALLOW_ZIP_DOWNLOAD
    ## enable zip file download 
    $ALLOW_ZIP_DOWNLOAD = 1;

    ## -- SHOW_STAT
    ## shows file statistics after file/folder list in the Web interface
    $SHOW_STAT = 1;

    ## -- PAGE_LIMIT
    ## limits number of files/folders shown in the Web interface
    ## EXAMPLE: $PAGE_LIMIT = 20;
    $PAGE_LIMIT=15;

    ## -- ALLOW_POST_UPLOADS
    ## enables a upload form in a fancy index of a folder (browser access)
    ## ATTENTATION: locks will be ignored
    ## Apache configuration:
    ## DEFAULT: $ALLOW_POST_UPLOADS = 1;
    $ALLOW_POST_UPLOADS = 1;

    ## -- POST_MAX_SIZE
    ## maximum post size (only POST requests)
    ## EXAMPLE: $POST_MAX_SIZE = 1073741824; # 1GB
    $POST_MAX_SIZE = 1073741824;
    #$POST_MAX_SIZE = 10240000;

    ## -- SHOW_QUOTA
    ## enables/disables quota information for fancy indexing
    ## DEFAULT: $SHOW_QUOTA = 0;
    $SHOW_QUOTA = 1;

    ## -- SHOW_PERM
    ## show file permissions
    ## DEFAULT: $SHOW_PERM = 0;
    $SHOW_PERM = 1;

    ## -- ALLOW_CHANGEPERM
    ## allow users to change file permissions
    ## DEFAULT: ALLOW_CHANGEPERM = 0;
    $ALLOW_CHANGEPERM = 1;

    ## -- ALLOW_CHANGEPERMRECURSIVE
    ## allow users to change file/folder permissions recursively
    $ALLOW_CHANGEPERMRECURSIVE = 1;

    ## -- PERM_USER
    # if ALLOW_CHANGEPERM is set to 1 the PERM_USER variable 
    # defines the file/folder permissions for user/owner allowed to change
    # EXAMPLE: $PERM_USER = [ 'r','w','x','s' ];
    $PERM_USER = [ 'r','w','x','s' ];

    ## -- PERM_GROUP
    # if ALLOW_CHANGEPERM is set to 1 the PERM_GROUP variable 
    # defines the file/folder permissions for group allowed to change
    # EXMAMPLE: $PERM_GROUP = [ 'r','w','x','s' ];
    $PERM_GROUP = [ 'r','w','x','s' ];

    ## -- PERM_OTHERS
    # if ALLOW_CHANGEPERM is set to 1 the PERM_OTHERS variable 
    # defines the file/folder permissions for other users allowed to change
    # EXAMPLE: $PERM_OTHERS = [ 'r','w','x','t' ];
    $PERM_OTHERS = [ 'r','w','x','t' ];

    ## -- LANGSWITCH
    ## a simple language switch
    $LANGSWITCH = '<div style="font-size:0.6em;text-align:right;border:0px;padding:0px;"><a href="?lang=default">[EN]</a> <a href="?lang=de">[DE]</a></div>';

    ## -- HEADER
    ## content after body tag in the Web interface
    $HEADER = '<div style="padding-left:3px;background-color:#444444;color:#ffffff;">WebDAV CGI - Web interface: You are logged in as '.($ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER}).'.</div>';

    ## -- SIGNATURE
    ## for fancy indexing
    ## EXAMPLE: $SIGNATURE=$ENV{SERVER_SIGNATURE};
    $SIGNATURE = '<div style="padding-left:3px;background-color:#444444;color:#ffffff;">&copy; ZE CMS, Humboldt-Universit&auml;t zu Berlin | Written 2010 by <a style="color:#ffffff;" href="http://amor.cms.hu-berlin.de/~rohdedan/webdav/">Daniel Rohde</a></div>';

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

    ## -- ENABLE_LOCK
    ## enable/disable lock/unlock support (WebDAV compiance class 2) 
    ## if disabled it's unsafe for shared collections/files but improves performance 
    $ENABLE_LOCK = 1;

    ## -- ENABLE_ACL
    ## enable ACL support: only Unix like read/write access changes for user/group/other are supported
    $ENABLE_ACL = 1;

    ## --- CURRENT_USER_PRINCIPAL
    ## a virtual URI for ACL principals
    ## for Apple's iCal &  Addressbook
    $CURRENT_USER_PRINCIPAL = "/principals/".($ENV{REDIRECT_REMOTE_USER} || $ENV{REMOTE_USER}) .'/';

    ## -- PRINCIPAL_COLLECTION_SET 
    ## don't change it for MacOS X Addressbook support
    ## DEFAULT: $PRINCIPAL_COLLECTION_SET = '/directory/';
    $PRINCIPAL_COLLECTION_SET = '/directory/';

    ## -- ENABLE_CALDAV
    ## enable CalDAV support for Lightning/Sunbird/iCal/iPhone calender/task support
    $ENABLE_CALDAV = 1;

    ## -- CALENDAR_HOME_SET
    ## maps UID numbers or remote users (accounts) to calendar folders
    %CALENDAR_HOME_SET = ( default=> '/', 1000 =>  '/caldav'  );

    ## -- ENABLE_CALDAV_SCHEDULE
    ## really incomplete (ALPHA) - properties exists but POST requests are not supported yet
    $ENABLE_CALDAV_SCHEDULE = 0;

    ## -- ENABLE_CARDDAV
    ## enable CardDAV support for Apple's Addressbook
    $ENABLE_CARDDAV = 1;

    ## -- ADDRESSBOOK_HOME_SET
    ## maps UID numbers or remote users to addressbook folders 
    %ADDRESSBOOK_HOME_SET = ( default=> '/',  1000 => '/carddav/'  );

    ## -- ENABLE_TRASH
    ## enables the server-side trash can (don't forget to setup $TRASH_FOLDER)
    $ENABLE_TRASH = 0;

    ## -- TRASH_FOLDER
    ## neccessary if you enable trash 
    ## it should be writable by your users (chmod a+rwxt <trash folder>)
    ## EXAMPLE: $TRASH_FOLDER = '/tmp/trash';
    $TRASH_FOLDER = '/usr/local/www/var/trash';

    ## -- ENABLE_GROUPDAV
    ## enables GroupDAV (http://groupdav.org/draft-hess-groupdav-01.txt)
    ## EXAMPLE: $ENABLE_GROUPDAV = 0;
    $ENABLE_GROUPDAV = 1;

    ## -- ENABLE_SEARCH
    ##  enables server-side search (WebDAV SEARCH/DASL, RFC5323)
    ## EXAMPLE: $ENABLE_SEARCH = 1;
    $ENABLE_SEARCH = 1;
*/
    /*
     * -- ENABLE_THUMBNAIL
     * enables image thumbnail support for folder listings of the Web interface.
     * If enabled the default icons for images will be replaced by thumbnails
     * and if the mouse is over a icon the icon will be zoomed to the size of $THUMBNAIL_WIDTH.
     * DEFAULT: $ENABLE_THUMBNAIL = 0;
     */ 
    public static final boolean ENABLE_THUMBNAIL = true;

    /*
    ## -- ENABLE_THUMBNAIL_CACHE
    ## enable image thumbnail caching (improves performance - 2x faster)
    ## DEFAULT: $ENABLE_THUMBNAIL_CACHE = 0;
    $ENABLE_THUMBNAIL_CACHE = 1;

    ## -- THUMBNAIL_WIDTH
    ## defines the width of a image thumbnail
    $THUMBNAIL_WIDTH=110;

    ## -- THUMBNAIL_CACHEDIR
    ## defines the path to a cache directory for image thumbnails
    ## this is neccessary if you enable the thumbnail cache ($ENABLE_THUMBNAIL_CACHE)
    ## EXAMPLE: $THUMBNAIL_CACHEDIR=".thumbs";
    $THUMBNAIL_CACHEDIR="/usr/local/www/tmp/thumbs";

    ## -- ENABLE_BIND
    ## enables BIND/UNBIND/REBIND methods defined in http://tools.ietf.org/html/draft-ietf-webdav-bind-27
    $ENABLE_BIND = 1;

    ## -- DEBUG
    ## enables/disables debug output
    ## you can find the debug output in your web server error log
    $DEBUG = 0;

    ############  S E T U P - END ###########################################
    #########################################################################
*/    
    
    
    
	
//	public static String getValue(String parameter) {
//		
//		// TODO: implement
//		return null;
//		
//	}
    
    public static String getSupportedDAVClasses() {
        // TODO: implement
        
        /*
## supported DAV compliant classes:
our $DAV='1';
$DAV.=', 2' if $ENABLE_LOCK;
$DAV.=', 3, <http://apache.org/dav/propset/fs/1>, extended-mkcol';
$DAV.=', access-control' if $ENABLE_ACL || $ENABLE_CALDAV || $ENABLE_CARDDAV;
$DAV.=', calendar-access, calendarserver-private-comments' if $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE; 
$DAV.=', calendar-auto-schedule' if  $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE;
$DAV.=', addressbook' if $ENABLE_CARDDAV;
$DAV.=', bind' if $ENABLE_BIND;         
         
         */
        
        
        return "";
        
    }
    

}
