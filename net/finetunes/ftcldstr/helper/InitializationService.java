package net.finetunes.ftcldstr.helper;

import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;

public class InitializationService {
    
    
    public volatile static boolean initialized = false;
    
    /*
     * initialises runtime variables
     */
    public synchronized static void init() {
        
        if (initialized) {
            return;
        }
        
/*
        use strict;
        #use warnings;

        use CGI;

        use File::Basename;

        use File::Spec::Link;

        use XML::Simple;
        use Date::Parse;
        use POSIX qw(strftime);

        use URI::Escape;
        use OSSP::uuid;
        use Digest::MD5;

        use DBI;

        use Quota;

        use Archive::Zip;

        use Graphics::Magick;

        do($CONFIGFILE) if defined $CONFIGFILE && -e $CONFIGFILE;

        ## flush immediately:
        $|=1;
*/
        
        // umask $UMASK; // TODO: PZ: how to implement this?

        // TODO: PZ: how to handle this?
        // ## before 'new CGI' to read POST requests:
        // $ENV{REQUEST_METHOD}=$ENV{REDIRECT_REQUEST_METHOD} if (defined $ENV{REDIRECT_REQUEST_METHOD}) ;

        /*
        $CGI::POST_MAX = $POST_MAX_SIZE;
        $CGI::DISABLE_UPLOADS = $ALLOW_POST_UPLOADS?0:1; // PZ: TODO: manual checking?
*/
        
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
        
/*


        #### PROPERTIES:
        # from RFC2518:
        #    creationdate, displayname, getcontentlanguage, getcontentlength, 
        #    getcontenttype, getetag, getlastmodified, lockdiscovery, resourcetype,
        #    source, supportedlock
        # from RFC4918:
        #    -source
        # from RFC4331:
        #    quota-available-bytes, quota-used-bytes
        # from draft-hopmann-collection-props-00.txt:
        #    childcount, defaultdocument (live), id, isfolder, ishidden, isstructureddocument, 
        #    hassubs, nosubs, objectcount, reserved, visiblecount
        # from MS-WDVME:
        #    iscollection, isFolder, ishidden (=draft), 
        #    Repl:authoritative-directory, Repl:resourcetag, Repl:repl-uid,
        #    Office:modifiedby, Office:specialFolderType (dead),
        #    Z:Win32CreationTime, Z:Win32FileAttributes, Z:Win32LastAccessTime, Z:Win32LastModifiedTime
        # from reverse engineering:
        #    name, href, parentname, isreadonly, isroot, getcontentclass, lastaccessed, contentclass
        #    executable
        # from RFC3744 (ACL):
        #    owner, group, supported-privilege-set, current-user-privilege-set, acl, acl-restrictions
        # from RFC4791 (CalDAV):
        #    calendar-description, calendar-timezone, supported-calendar-component-set, supported-calendar-data,
        #    max-resource-size, min-date-time, max-date-time, max-instances, max-attendees-per-instance,
        #    calendar-home-set,
        # from http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-ctag.txt
        #    getctag
        # from RFC5397 (WebDAV Current User Principal)
        #    current-user-principal
        # from http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-08
        #    principal: schedule-inbox-URL, schedule-outbox-URL, calendar-user-type, calendar-user-address-set,
        #    collection: schedule-calendar-transp,schedule-default-calendar-URL,schedule-tag
        # from http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-pubsubdiscovery.txt
        # from RFC3253 (DeltaV)
        #    supported-report-set
        #    supported-method-set for RFC5323 (DASL/SEARCH):
        # from http://datatracker.ietf.org/doc/draft-ietf-vcarddav-carddav/
        #    collection: addressbook-description, supported-address-data 
        #    principal: addressbook-home-set, principal-address
        #    report: address-data
        # from RFC5842 (bind)
        #    resource-id, parent-set (unsupported yet)


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

        %NAMESPACEELEMENTS = ( 'multistatus'=>1, 'prop'=>1 , 'error'=>1, 'principal-search-property-set'=>1);

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
        
        initMessages();
        
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

        // Setting the path_translated value
        // String servletPath = request.getServletPath().substring(1).replaceAll("/", System.getProperty("path.separator"));

        // our $PATH_TRANSLATED = $ENV{PATH_TRANSLATED};
        // our $REQUEST_URI = $ENV{REQUEST_URI};
        String pathTranslated = servletContext.getRealPath(request.getServletPath());
        String requestURI = request.getServletPath();
        
        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            requestURI += "?" + request.getQueryString();
        }
        
/*        
        our $cgi = $ENV{REQUEST_METHOD} eq 'PUT' ? new CGI({}) : new CGI; // PZ: TODO: what is the difference?
        my $method = $cgi->request_method();
*/        

/*        
        debug("$0 called with UID='$<' EUID='$>' GID='$(' EGID='$)' method=$method");
        debug("User-Agent: $ENV{HTTP_USER_AGENT}");

        debug("$0: X-Litmus: ".$cgi->http("X-Litmus")) if defined $cgi->http("X-Litmus");
        debug("$0: X-Litmus-Second: ".$cgi->http("X-Litmus-Second")) if defined $cgi->http("X-Litmus-Second");
*/        

/*
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

/*        
        # protect against direct CGI script call:
            if (!defined $PATH_TRANSLATED || $PATH_TRANSLATED eq "") {
                debug('FORBIDDEN DIRECT CALL!');
                printHeaderAndContent('404 Not Found');
                exit();
            }
*/            

        if (FileOperationsService.is_directory(pathTranslated) && !pathTranslated.endsWith(pathSeparator)) {
            pathTranslated += pathSeparator;
        }
        
        if (FileOperationsService.is_directory(pathTranslated) && !requestURI.endsWith("/")) {
            requestURI += "/";
        }
        
/*
            TODO:
            if (grep(/^\Q$<\E$/, @FORBIDDEN_UID)>0) {
                debug("Forbidden UID");
                printHeaderAndContent('403 Forbidden');
                exit(0);
            }
*/            
        
        params.setPathTranslated(pathTranslated);
        params.setRequestURI(requestURI);
        
        return params;
    }
    
    
    public static void initMessages() {
        
        // Don't use entities like &auml; for buttons and table header (names, lastmodified, size, mimetype).
        HashMap<String, String> strings = new HashMap<String, String>();
        
        strings.put("search", "Search for file/folder name:");
        strings.put("searchtooltip", "allowed are: file/folder name, regular expression");
        strings.put("searchnothingfound", "Nothing found for ");
        strings.put("searchgoback ", " in ");
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
        strings.put("lastmodifiedformat", "%d-%b-%Y %H:%M");
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
        strings.put("zipdownloadtext", " selected files/folders (zip archive)");
        strings.put("zipuploadtext", "Upload zip archive: ");
        strings.put("zipuploadbutton", "Upload & Extract");
        strings.put("zipuploadconfirm", "Do you really want to upload zip, extract it and replace existing files?");
        strings.put("fileuploadtext", "File: ");
        strings.put("fileuploadbutton", "Upload");
        strings.put("fileuploadmore ", "more");
        strings.put("fileuploadconfirm ", "Do you really want to upload file(s) and replace existing file(s)?");
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
        strings.put("msg_zipuploadsingle", "%s zip archive (%s) uploaded successfully.");
        strings.put("msg_zipuploadmulti", "%s zip archives (%s) uploaded successfully.");
        strings.put("msg_zipuploadnothingerr", "Please select a local zip archive (Browse...) for upload.");
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

}
