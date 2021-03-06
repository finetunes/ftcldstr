package net.finetunes.ftcldstr;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.actionhandlers.base.NotSupportedMethodActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.ACLActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.BindActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.CopyActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.DeleteActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.GetActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.GetlibActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.HeadActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.LockActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.MkCalendarActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.MkcolActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.MoveActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.OptionsActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.PostActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.PropfindActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.ProppatchActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.PutActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.RebindActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.ReportActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.SearchActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.TraceActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.UnbindActionHandler;
import net.finetunes.ftcldstr.actionhandlers.webdav.UnlockActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.InitializationService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.helper.SystemCalls;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.SearchService;
import net.finetunes.ftcldstr.routines.webdav.WebDAVLocks;
import net.finetunes.ftcldstr.routines.webdav.properties.Properties;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;

public class ActionServlet extends MServlet {

    public static final String NOT_SUPPORTED = "NOT_SUPPORTED";
    private HashMap<String, AbstractActionHandler> methods;    
    
    public ActionServlet() {
        super();
        initMethods();
        
/*        
<d:where>
  <d:eq>
    <d:prop>
      <d:getcontentlength/>
    </d:prop>
    <d:literal>100</d:literal>
  </d:eq>
</d:where>
        
*/      
        
    }
    
    public void destroy() {
        Properties.serialize(ConfigService.ROOT_PATH + ConfigService.PROPERTIES_FILE_PATH, ConfigService.properties);
        WebDAVLocks.serialize(ConfigService.ROOT_PATH + ConfigService.LOCKS_FILE_PATH, ConfigService.locks);
    }    
    
    
	public void bla() {
//		super.doDelete(req, resp);
//		super.doGet(req, resp);
//		super.doHead(req, resp);
//		super.doOptions(arg0, arg1);
//		super.doPost(req, resp);
//		super.doPut(req, resp);
//		super.doTrace(req, resp);
	}
	
    public void service(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        
        RequestParams requestParams = InitializationService.initRequestParams(request, response,
                getServletContext());

        String auth = request.getHeader("Authorization");
        if (authorized(requestParams, auth)) {
            
            try {
                InitializationService.checkRunPermissions(requestParams);
                // System.out.println("MServlet::service::METHOD: " + requestParams.getRequestedMethod());
                Logger.debug("MServlet called with UID=" + SystemCalls.getCurrentProcessUid(requestParams) + 
                        " (" + requestParams.getUsername() + ") method=" + requestParams.getRequestedMethod());
                Logger.debug("User-Agent: " + requestParams.getHeader("User-Agent"));
                
                if (requestParams.headerExists("X-Litmus")) {
                    Logger.debug("MServlet: X-Litmus: " + requestParams.getHeader("X-Litmus"));
                }
                if (requestParams.headerExists("X-Litmus-Second")) {
                    Logger.debug("MServlet: X-Litmus-Second: " + requestParams.getHeader("X-Litmus-Second"));
                }
                
                handleRequest(requestParams);
            }
            catch (ExitException e) {
                // do nothing
                // the script just wants to exit before the normal finish
            }
        }
        else {
            response.setHeader("WWW-Authenticate", "BASIC realm=\"webdav\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

        response.getOutputStream().flush();
        response.flushBuffer();
    }
    
    private boolean authorized(RequestParams requestParams, String auth) {
        if (auth == null) {
            return false;
        }
        
        if (!auth.toUpperCase().startsWith("BASIC ")) {
            return false; // only BASIC is accepted now
        }
        
        try {
            String userpassEncoded = auth.substring(6); // trimming "BASIC "
            sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
            String userpassDecoded = new String(dec.decodeBuffer(userpassEncoded));
            String[] u = userpassDecoded.split(":");
            if (u.length >= 2) {
                String user = u[0];
                String pass = u[1];
                
                if (WrappingUtilities.isUserValid(requestParams, user, pass)) {
                    requestParams.setUsername(user);
                    return true;
                }
                
                return false;
            }
        }
        catch (IOException e) {
            Logger.log("Exception on authorization: " + e.getMessage());
        }
        
        return false;
        
    }

    //@SuppressWarnings("unchecked")
    public void handleRequest(RequestParams requestParams) {
        
        // request processing

        String pathinfo = requestParams.getRequest().getPathInfo();
        if (pathinfo == null) {
            pathinfo = "";
        }

        // Handle the request
        // with the corresponding method handler
        AbstractActionHandler actionHandler = getMethodHandler(requestParams.getRequestedMethod());
        
        if (actionHandler == null) {
            actionHandler = getMethodHandler(ActionServlet.NOT_SUPPORTED);
        }
        
        actionHandler.handle(requestParams);
    }
    
    public void initMethods() {
        
        methods = new HashMap<String, AbstractActionHandler>();
        addMethodHandler("GET", new GetActionHandler());
        addMethodHandler("HEAD", new HeadActionHandler());
        addMethodHandler("POST", new PostActionHandler());
        addMethodHandler("OPTIONS", new OptionsActionHandler());
        addMethodHandler("TRACE", new TraceActionHandler());
        addMethodHandler("GETLIB", new GetlibActionHandler());
        addMethodHandler("PROPFIND", new PropfindActionHandler());
        addMethodHandler("PROPPATCH", new ProppatchActionHandler());
        addMethodHandler("PUT", new PutActionHandler());
        addMethodHandler("COPY", new CopyActionHandler());
        addMethodHandler("MOVE", new MoveActionHandler());
        addMethodHandler("DELETE", new DeleteActionHandler());
        addMethodHandler("MKCALENDAR", new MkCalendarActionHandler());
        addMethodHandler("MKCOL", new MkcolActionHandler());
        addMethodHandler("LOCK", new LockActionHandler());
        addMethodHandler("UNLOCK", new UnlockActionHandler());
        addMethodHandler("ACL", new ACLActionHandler());
        addMethodHandler("REPORT", new ReportActionHandler());
        addMethodHandler("SEARCH", new SearchActionHandler());
        addMethodHandler("BIND", new BindActionHandler());
        addMethodHandler("UNBIND", new UnbindActionHandler());
        addMethodHandler("REBIND", new RebindActionHandler());
        
        addMethodHandler(ActionServlet.NOT_SUPPORTED, new NotSupportedMethodActionHandler());
    }
    
    public void addMethodHandler(String method, AbstractActionHandler handler) {
        methods.put(method, handler);
    }
    
    public AbstractActionHandler getMethodHandler(String method) {
        return methods.get(method);
    }    
    
}

//
//#!/usr/bin/perl
//###!/usr/bin/speedy --  -r20 -M5
//#########################################################################
//# (C) ZE CMS, Humboldt-Universitaet zu Berlin
//# Written 2010 by Daniel Rohde <d.rohde@cms.hu-berlin.de>
//#########################################################################
//# This is a very pure WebDAV server implementation that
//# uses the CGI interface of a Apache webserver.
//# Use this script in conjunction with a UID/GID wrapper to
//# get and preserve file permissions.
//# IT WORKs ONLY WITH UNIX/Linux.
//#########################################################################
//# This program is free software: you can redistribute it and/or modify
//# it under the terms of the GNU General Public License as published by
//# the Free Software Foundation, either version 3 of the License, or
//# (at your option) any later version.
//#
//# This program is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//# GNU General Public License for more details.
//#
//# You should have received a copy of the GNU General Public License
//# along with this program.  If not, see <http://www.gnu.org/licenses/>.
//#########################################################################
//# VERSION 0.5.4 BETA
//# REQUIREMENTS:
//#   - CPAN Package (Debian/Ubuntu package):
//#       CGI (libcgi-perl)
//#       DBI (libdbi-perl) + Driver, e.g. DBD::SQLite (libdbd-sqlite3-perl)
//#       OSSP::uuid  (libossp-uuid-perl)
//#       Date::Parse (libtimedate-perl)
//#       XML::Simple (libxml-simple-perl)
//#       Quota (libquota-perl)
//#       Archive::Zip (libarchive-zip-perl)
//#       Graphics::Magick (libgraphics-magick-perl)
//#       File::Sepc::Link (CPAN only)
//# INSTALLATION:
//#    - see http://amor.cms.hu-berlin.de/~rohdedan/webdav/
//#       
//# CHANGES:
//#   0.5.4: BETA
//#        - fixed default DOCUMENT_ROOT and VIRTUAL_BASE (Apache's DOCUMENT_ROOT is without a trailing slash by default)
//#        - added mime.types file support requested by Hanz Makmur <makmur@cs.rutgers.edu>
//#        - added a per folder filter for files/folders listed by PROPFIND and the Web interface (GET/PROPFIND)
//#        - added a per folder limit for file/folders listed by PROPFIND and the Web interface (GET/PROPFIND)
//#        - added a switch to ignore file permissions in the Web interface for full AFS support (GET)
//#        - improved Web interface (GET)
//#            - used HTML table instead of preformatted text for file/folder listing
//#            - added row and column highlighting for file/folder listing 
//#            - added tooltips to the last modified and size column
//#        - fixed minor DVI file icon bug (GET)
//#        - fixed minor documentation bug
//#   0.5.3: 2010/10/11
//#        - fixed minor link loop bug (depth != infinity => read until depth was reached) (PROPFIND)
//#        - improved Web interface (GET/POST):
//#            - added missing MIME types and icons (video, source code) (GET)
//#            - fixed root folder navigation bug in the Web interface reported by Andre Schaaf (GET)
//#            - added file permissions column to the Web interface (GET)
//#            - added change file permission feature to the Web interface (GET/POST)
//#            - added simple language switch to the Web interface (GET/POST)
//#            - fixed German translations (GET)
//#            - fixed minor sorting and properties view bug in the Web interface (GET)
//#        - improved performance (direct method call instead of eval)
//#        - replaced Image::Magick by Graphics::Magick for thumbnail support (GET)
//#        - added Speedy support requested by Hanz Makmur (mod_perl and pperl should also work)
//#   0.5.2: 2010/23/09
//#        - added BIND/UNBIND/REBIND methods (RFC5842)
//#        - fixed major link loop bug (PROPFIND/GET/SEARCH/LOCK)
//#        - fixed major move/copy/delete symbolic link bug (MOVE/COPY/POST)
//#        - fixed minor long URL after file upload bug (POST)
//#   0.5.1: 2010/07/09
//#        - fixed minor file not readable bug (GET)
//#        - improved Web interface (GET/POST):
//#            - fixed property view HTML conformance bug (GET)
//#            - fixed major illegal regex bug in file/folder name search (GET)
//#            - added image thumbnail support (GET)
//#            - fixed minor readable/writeable folder bug (GET)
//#            - added (error/acknowledgement) messages for file/folder actions (GET/POST)
//#            - changed HTML conformance from XHTML to HTML5 (GET/POST)
//#            - added multiple file upload support within a single file field supported by Firefox 3.6, Chrome4, ??? (GET/POST)
//#   0.5.0: 2010/20/08
//#        - improved database performance (indexes)
//#        - added WebDAV SEARCH/DASL (RFC5323, SEARCH)
//#        - added GroupDAV support (http://groupdav.org/draft-hess-groupdav-01.txt)
//#        - improved Web interface (GET/POST):
//#            - added localization support (GET)
//#            - added paging (GET)
//#            - added confirmation dialogs (GET)
//#            - added 'Toggle All' button (GET)
//#            - added zip upload (GET/POST)
//#            - added zip download (GET/POST)
//#            - added sorting feature (GET)
//#            - added quick folder navigation (GET)
//#            - added search feature (GET)
//#            - added file/folder statistics (GET)
//#        - fixed PUT trouble (empty files) with some Apache configurations reported by Cyril Elkaim
//#        - added configuration file feature requested by Cyril Elkaim
//#        - fixed SQL bugs to work with MySQL reported by Cyril Elkaim
//#        - added missing MIME types (GET,PROPFIND,REPORT)
//#        - fixed XML namespace for transparent element (PROPFIND)
//#   0.4.1: 2010/05/07
//#        - added a server-side trash can (DELETE/POST)
//#        - added a property view to the web interface (GET)
//#        - fixed missing bind/unbind privileges for ACL properties (PROPFIND)
//#        - fixed missing data types for some Windows properties (PROPFIND)
//#   0.4.0: 2010/24/06
//#        - added CardDAV support (incomplete: no preconditions, no filter in REPORT queries; PROPFIND/REPORT)
//#        - fixed missing current user privileges bug (PROPFIND)
//#        - fixed supported-report-set property bug (PROPFIND)
//#        - fixed depth greater than one bug in calendar-query REPORT query  (REPORT)
//#        - fixed unknown report bug (REPORT)
//#   0.3.7: 2010/14/06
//#        - added current-user-principal property (RFC5397; PROPFIND)
//#        - added incomplete CalDAV schedule support (http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-08; REPORT/PROPFIND)
//#        - added incomplete CalDAV support (RFC4791; PROPFIND/REPORT)
//#        - added incomplete ACL support (RFC3744; PROPFIND/ACL/REPORT)
//#        - added extendend MKCOL support (RFC5689; MKCOL)
//#        - added mixed content support for user defined properties (PROPPATCH/PROPFIND)
//#        - added switches to enable/disable features (LOCK, CalDAV, CalDAV-Schedule, ACL; OPTIONS/ACL/LOCK/UNLOCK/REPORT)
//#        - improved performance with caching (PROPFIND/REPORT)
//#        - improved XML generation: define only used namespaces (PROPFIND/REPORT/PROPATCH/DELETE/LOCK)
//#        - fixed missing property protection bug (PROPPATCH)
//#        - fixed OPTIONS bug
//#        - fixed lock owner bug (LOCK)
//#        - fixed bug: hidden files should not be counted (PROPFIND:childcount,visiblecount,hassubs,objectcount)
//#        - fixed isroot bug (PROPFIND)
//#   0.3.6: 2010/03/06
//#        - improved security (POST)
//#        - small but safe performance improvements (MOVE)
//#        - fixed quota bug (quota-available-bytes; PROPFIND)
//#        - added GFS/GFS2 quota support (GET, PROPFIND)
//#        - fixed bug: missing '/' in href property of a folder (Dreamweaver works now; PROPFIND)
//#        - improved performance with caching (PROPFIND)
//#        - added missing source property (PROPFIND)
//#   0.3.5: 2010/31/05
//#        - added logging
//#        - fixed redirect bugs reported by Paulo Estrela (POST,MKCOL,...)
//#        - added user property support (PROPPATCH/PROPFIND)
//#        - fixed datatype bug (PROPFIND)
//#        - improved allprop request performance (PROPFIND)
//#        - fixed include handling (PROPFIND)
//#        - passed all litmus tests (http://www.webdav.org/neon/litmus/)
//#        - fixed lock token generation bug (LOCK)
//#        - fixed database schema bug (LOCK)
//#        - fixed LOCK/UNLOCK shared/exclusive bugs (litmus locks)
//#        - fixed PROPFIND bugs (litmus props)
//#        - fixed PROPPATCH bugs (litmus props)
//#        - fixed COPY bugs (litmus copymove/props)
//#        - fixed MOVE bugs (litmus copymove/props)
//#        - fixed MCOL bugs (litmus basic)
//#        - fixed DELETE bugs (litmus basic)
//#   0.3.4: 2010/25/05
//#        - added WebDAV mount feature (RFC4709 - GET)
//#        - added quota properties (RFC4331 - PROPFIND)
//#        - added M$ name spaces (PROPFIND/PROPPATCH)
//#        - added M$-WDVME support
//#        - added M$-WDVSE support
//#        - fixed depth handling (PROPFIND)
//#   0.3.3: 2010/11/05
//#        - improved file upload (POST)
//#        - fixed Windows file upload bug (POST)
//#        - fixed fency indexing header formatting bug (GET)
//#        - fixed fency indexing URI encoding bug (GET)
//#        - fixed redirect bug (CGI and POST)
//#   0.3.2: 2010/10/05
//#        - added simple file management (mkdir, rename/move, delete)
//#        - fixed missing (REDIRECT_)PATH_TRANSLATED environment bug
//#        - fixed double URL encoding problem (COPY/MOVE)
//#   0.3.1: 2010/10/05
//#        - fixed Vista/Windows7 problems (PROPFIND)
//#   0.3.0: 2010/07/05
//#        - added LOCK/UNLOCK
//#        - added ETag support
//#        - fixed account in destination URI bug (COPY/MOVE)
//#        - fixed delete none existing resource bug (DELETE)
//#        - fixed element order in XML responses bug (PROPFIND/PROPPATCH/DELETE/LOCK)
//#        - fixed direct call bug (MKCOL/PUT/LOCK)
//#        - fixed MIME type detection bug (GET/PROPFIND)
//#   0.2.4: 2010/29/04
//#        - added fancy indexing setup switch
//#        - added additional properties for files/folders
//#        - fixed PROPFIND request handling
//#   0.2.3: 2010/28/04
//#        - improved debugging
//#        - fixed URI encoded characters in 'Destination' request header bug (MOVE/COPY)
//#   0.2.2: 2010/27/04
//#        - added Apache namespace (executable)
//#        - fixed namespace bugs (PROPFIND/PROPPATCH)
//#        - added hide feature: hide special files/folders 
//#        - fixed PROPPATCH bug (Multi-Status)
//#   0.2.1: 2010/27/04
//#        - added PROPPATCH support for getlastmodified updates
//#        - fixed MIME types (jpg)
//#        - fixed error handling (Method Not Allowed, Not Implemented)
//#        - added table header for fancy indexing
//#        - code cleanup
//#        - added setup documentation
//#   0.2: 2010/26/04
//#        - added umask configuration parameter
//#        - fixed invalid HTML encoding for '<folder>' 
//#          in GET requests on collections
//#   0.1: 2010/26/04 
//#        - initial implementation
//# TODO:
//#    - add a property editor to the Web interface
//#    - handle LOCK timeouts 
//#    - RFC5689 (extended MKCOL) is incomplete (error handling/precondition check)
//#    - RFC3744 (ACL's) (incomplete)
//#    - RFC4791 (CalDAV) incomplete
//#    - Collection Synchronization for WebDAV (http://tools.ietf.org/html/draft-daboo-webdav-sync-03)
//#    - RFC5842 (Binding Extensions to Web Distributed Authoring and Versioning (WebDAV))
//#    - RFC5785 (Defining Well-Known Uniform Resource Identifiers (URIs))
//#    - improve/fix precondition checks (If header)
//# KNOWN PROBLEMS:
//#    - see http://amor.cms.hu-berlin.de/~rohdedan/webdav/
//#########################################################################
//# Example Apache configuration (.htaccess):
//#
//#   RewriteEngine On
//#   RewriteRule .* /cgi-bin/webdavwrapper
//# 
//#   AuthType Basic
//#   AuthName "A protected WebDAV folder"
//#   AuthUserFile /path-to-my-auth-file
//#   require valid-user
//#########################################################################
//use vars qw($VIRTUAL_BASE $DOCUMENT_ROOT $UMASK %MIMETYPES $FANCYINDEXING %ICONS @FORBIDDEN_UID
//            @HIDDEN $ALLOW_POST_UPLOADS $BUFSIZE $MAXFILENAMESIZE $DEBUG %ELEMENTORDER
//            $DBI_SRC $DBI_USER $DBI_PASS $DBI_INIT $DEFAULT_LOCK_OWNER $ALLOW_FILE_MANAGEMENT
//            $ALLOW_INFINITE_PROPFIND %NAMESPACES %NAMESPACEELEMENTS %ELEMENTS %NAMESPACEABBR %DATATYPES
//            $CHARSET $LOGFILE %CACHE $GFSQUOTA $SHOW_QUOTA $SIGNATURE $POST_MAX_SIZE @PROTECTED_PROPS
//            @UNSUPPORTED_PROPS $ENABLE_ACL $ENABLE_CALDAV @ALLPROP_PROPS $ENABLE_LOCK
//            @KNOWN_COLL_PROPS @KNOWN_FILE_PROPS @IGNORE_PROPS @KNOWN_CALDAV_COLL_PROPS
//            @KNOWN_COLL_LIVE_PROPS @KNOWN_FILE_LIVE_PROPS
//            @KNOWN_CALDAV_COLL_LIVE_PROPS @KNOWN_CALDAV_FILE_LIVE_PROPS
//            @KNOWN_CARDDAV_COLL_LIVE_PROPS @KNOWN_CARDDAV_FILE_LIVE_PROPS
//            @KNOWN_ACL_PROPS @KNOWN_CALDAV_FILE_PROPS 
//            $ENABLE_CALDAV_SCHEDULE
//            $ENABLE_CARDDAV @KNOWN_CARDDAV_COLL_PROPS @KNOWN_CARDDAV_FILE_PROPS $CURRENT_USER_PRINCIPAL
//            %ADDRESSBOOK_HOME_SET %CALENDAR_HOME_SET $PRINCIPAL_COLLECTION_SET 
//            $ENABLE_TRASH $TRASH_FOLDER $ALLOW_SEARCH $SHOW_STAT $HEADER $CONFIGFILE $ALLOW_ZIP_UPLOAD $ALLOW_ZIP_DOWNLOAD
//            $PAGE_LIMIT $ENABLE_SEARCH $ENABLE_GROUPDAV %SEARCH_PROPTYPES %SEARCH_SPECIALCONV %SEARCH_SPECIALOPS
//            @DB_SCHEMA $CREATE_DB %TRANSLATION $LANG $MAXLASTMODIFIEDSIZE $MAXSIZESIZE
//            $THUMBNAIL_WIDTH $ENABLE_THUMBNAIL $ENABLE_THUMBNAIL_CACHE $THUMBNAIL_CACHEDIR $ICON_WIDTH
//            $ENABLE_BIND $SHOW_PERM $ALLOW_CHANGEPERM $ALLOW_CHANGEPERMRECURSIVE $LANGSWITCH
//            $PERM_USER $PERM_GROUP $PERM_OTHERS
//            $DBI_PERSISTENT
//            $FILECOUNTLIMIT %FILECOUNTPERDIRLIMIT %FILEFILTERPERDIR $IGNOREFILEPERMISSIONS
//            %COLORS $MIMEFILE
//); 
//#########################################################################
//############  S E T U P #################################################
//
//## -- ENV{PATH} 
//##  search PATH for binaries 
//$ENV{PATH}="/bin:/usr/bin:/sbin/:/usr/local/bin:/usr/sbin";
//
//## -- CONFIGFILE
//## you can overwrite all variables from this setup section with a config file
//## (simply copy the complete setup section (without 'use vars ...') or single options to your config file)
//## EXAMPLE: CONFIGFILE = './webdav.conf';
//$CONFIGFILE = '/usr/local/www/conf/webdav.conf';
//
//## -- VIRTUAL_BASE
//## only neccassary if you use redirects or rewrites 
//## from a VIRTUAL_BASE to the DOCUMENT_ROOT
//## regular expressions are allowed
//## DEFAULT: $VIRTUAL_BASE = "";
//$VIRTUAL_BASE = '(/webdav/|)';
//
//## -- DOCUMENT_ROOT
//## by default the server document root
//## (don't forget a trailing slash '/'):
//$DOCUMENT_ROOT = $ENV{DOCUMENT_ROOT}.'/';
//
//## -- UMASK
//## mask for file/folder creation 
//## (it does not change permission of existing files/folders):
//## DEFAULT: $UMASK = 0002; # read/write/execute for users and groups, others get read/execute permissions
//$UMASK = 0022;
//
//## -- MIMETYPES
//## some MIME types for Web browser access and GET access
//## you can add some missing types ('extension list' => 'mime-type'):
//%MIMETYPES = (
//	'html htm shtm shtml' => 'text/html',
//	'css' => 'text/css', 'xml xsl'=>'text/xml',
//	'js' => 'application/x-javascript',
//	'txt asc pl cgi php php3 php4 php5 php6 csv log out java jsp tld tag' => 'text/plain',
//	'c'=> 'text/x-csrc', 'h'=>'text/x-chdr',
//	'gif'=>'image/gif', 'jpeg jpg jpe'=>'image/jpeg', 
//	'png'=>'image/png', 'bmp'=>'image/bmp', 'tiff'=>'image/tiff',
//	'pdf'=>'application/pdf', 'ps'=>'application/ps',
//	'dvi'=>'application/x-dvi','tex'=>'application/x-tex',
//	'zip'=>'application/zip', 'tar'=>'application/x-tar','gz'=>'application/x-gzip',
//	'doc dot' => 'application/msword',
//	'xls xlm xla xlc xlt xlw' => 'application/vnd.ms-excel',
//	'ppt pps pot'=>'application/vnd.ms-powerpoint',
//	'pptx'=>'application/vnd.openxmlformats-officedocument.presentationml.presentation',
//	'ics' => 'text/calendar',
//	'avi' => 'video/x-msvideo', 'wmv' => 'video/x-ms-wmv', 'ogv'=>'video/ogg',
//	'mpeg mpg mpe' => 'video/mpeg', 'qt mov'=>'video/quicktime',
//	default => 'application/octet-stream',
//	); 
//
//## -- MIMEFILE
//## optionally you can use a mime.types file instead of %MIMETYPES
//## EXAMPLE: $MIMEFILE = '/etc/mime.types';
//# $MIMEFILE = '/etc/mime.types';
//
//## -- FANCYINDEXING
//## enables/disables fancy indexing for GET requests on folders
//## if disabled you get a 404 error for a GET request on a folder
//## DEFAULT: $FANCYINDEXING = 1;
//$FANCYINDEXING = 1;
//
//## -- MAXFILENAMESIZE 
//## Web interface: width of filename column
//$MAXFILENAMESIZE = 30;
//
//## -- MAXLASTMODIFIEDSIZE
//## Web interface: width of last modified column
//$MAXLASTMODIFIEDSIZE = 20;
//
//## -- MAXSIZESIZE
//## Web interface: width of size column
//$MAXSIZESIZE = 12;
//
//
//## -- ICONS
//## for fancy indexing (you need a server alias /icons to your Apache icons directory):
//%ICONS = (
//	'< .. >' => '/icons/back.gif',
//	'<folder>' => '/icons/folder.gif',
//	'text/plain' => '/icons/text.gif', 'text/html' => '/icons/text.gif',
//	'application/zip'=> '/icons/compressed.gif', 'application/x-gzip'=>'/icons/compressed.gif',
//	'image/gif'=>'/icons/image2.gif', 'image/jpg'=>'/icons/image2.gif',
//	'image/png'=>'/icons/image2.gif', 
//	'application/pdf'=>'/icons/pdf.gif', 'application/ps' =>'/icons/ps.gif',
//	'application/msword' => '/icons/text.gif',
//	'application/vnd.ms-powerpoint' => '/icons/world2.gif',
//	'application/vnd.ms-excel' => '/icons/quill.gif',
//	'application/x-dvi'=>'/icons/dvi.gif', 'text/x-chdr' =>'/icons/c.gif', 'text/x-csrc'=>'/icons/c.gif',
//	'video/x-msvideo'=>'/icons/movie.gif', 'video/x-ms-wmv'=>'/icons/movie.gif', 'video/ogg'=>'/icons/movie.gif',
//	'video/mpeg'=>'/icons/movie.gif', 'video/quicktime'=>'/icons/movie.gif',
//	default => '/icons/unknown.gif',
//);
//
//## -- ICON_WIDTH
//## specifies the icon width for the folder listings of the Web interface
//## DEFAULT: $ICON_WIDTH = 22;
//$ICON_WIDTH = 22;
//
//## -- COLORS
//## defines some colors for the Web interface
//## (required: headbgcolor, headhighlightcolor,rowbgcolors,rowhighlightcolor)
//%COLORS = (
//            headbgcolor => '#dddddd',
//	    headhighlightcolor => '#bcbcbc',
//            rowbgcolors => [ 'white','#eeeeee'  ],
//            rowhighlightcolor => '#aaaaaa',
//);
//
//
//## -- FORBIDDEN_UID
//## a comman separated list of UIDs to block 
//## (process id of this CGI will be checked against this list)
//## common "forbidden" UIDs: root, Apache process owner UID
//## DEFAULT: @FORBIDDEN_UID = ( 0 );
//@FORBIDDEN_UID = ( 0 );
//
//## -- HIDDEN 
//## hide some special files/folders (GET/PROPFIND) 
//## EXAMPLES: @HIDDEN = ( '.DAV/?$', '~$', '.bak$' );
//@HIDDEN = ('/.ht','/.DAV');
//
//## -- ALLOW_INFINITE_PROPFIND
//## enables/disables infinite PROPFIND requests
//## if disabled the default depth is set to 0
//$ALLOW_INFINITE_PROPFIND = 1;
//
//## -- ALLOW_FILE_MANAGEMENT
//## enables file management with a web browser
//## ATTENTATION: locks will be ignored
//$ALLOW_FILE_MANAGEMENT = 1;
//
//## -- ALLOW_SEARCH
//## enable file/folder search in the Web interface
//$ALLOW_SEARCH = 1;
//
//## -- ALLOW_ZIP_UPLOAD
//## enable zip file upload (incl. extraction)
//$ALLOW_ZIP_UPLOAD = 1;
//
//## -- ALLOW_ZIP_DOWNLOAD
//## enable zip file download 
//$ALLOW_ZIP_DOWNLOAD = 1;
//
//## -- SHOW_STAT
//## shows file statistics after file/folder list in the Web interface
//$SHOW_STAT = 1;
//
//## -- PAGE_LIMIT
//## limits number of files/folders shown in the Web interface
//## EXAMPLE: $PAGE_LIMIT = 20;
//$PAGE_LIMIT=15;
//
//## -- ALLOW_POST_UPLOADS
//## enables a upload form in a fancy index of a folder (browser access)
//## ATTENTATION: locks will be ignored
//## Apache configuration:
//## DEFAULT: $ALLOW_POST_UPLOADS = 1;
//$ALLOW_POST_UPLOADS = 1;
//
//## -- POST_MAX_SIZE
//## maximum post size (only POST requests)
//## EXAMPLE: $POST_MAX_SIZE = 1073741824; # 1GB
//$POST_MAX_SIZE = 1073741824;
//#$POST_MAX_SIZE = 10240000;
//
//## -- SHOW_QUOTA
//## enables/disables quota information for fancy indexing
//## DEFAULT: $SHOW_QUOTA = 0;
//$SHOW_QUOTA = 1;
//
//## -- SHOW_PERM
//## show file permissions
//## DEFAULT: $SHOW_PERM = 0;
//$SHOW_PERM = 1;
//
//## -- ALLOW_CHANGEPERM
//## allow users to change file permissions
//## DEFAULT: ALLOW_CHANGEPERM = 0;
//$ALLOW_CHANGEPERM = 1;
//
//## -- ALLOW_CHANGEPERMRECURSIVE
//## allow users to change file/folder permissions recursively
//$ALLOW_CHANGEPERMRECURSIVE = 1;
//
//## -- PERM_USER
//# if ALLOW_CHANGEPERM is set to 1 the PERM_USER variable 
//# defines the file/folder permissions for user/owner allowed to change
//# EXAMPLE: $PERM_USER = [ 'r','w','x','s' ];
//$PERM_USER = [ 'r','w','x','s' ];
//
//## -- PERM_GROUP
//# if ALLOW_CHANGEPERM is set to 1 the PERM_GROUP variable 
//# defines the file/folder permissions for group allowed to change
//# EXMAMPLE: $PERM_GROUP = [ 'r','w','x','s' ];
//$PERM_GROUP = [ 'r','w','x','s' ];
//
//## -- PERM_OTHERS
//# if ALLOW_CHANGEPERM is set to 1 the PERM_OTHERS variable 
//# defines the file/folder permissions for other users allowed to change
//# EXAMPLE: $PERM_OTHERS = [ 'r','w','x','t' ];
//$PERM_OTHERS = [ 'r','w','x','t' ];
//
//## -- LANGSWITCH
//## a simple language switch
//$LANGSWITCH = '<div style="font-size:0.6em;text-align:right;border:0px;padding:0px;"><a href="?lang=default">[EN]</a> <a href="?lang=de">[DE]</a></div>';
//
//## -- HEADER
//## content after body tag in the Web interface
//$HEADER = '<div style="padding-left:3px;background-color:#444444;color:#ffffff;">WebDAV CGI - Web interface: You are logged in as <span title="'.`id -a`.'">'.($ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER}).'</span>.</div>';
//
//## -- SIGNATURE
//## for fancy indexing
//## EXAMPLE: $SIGNATURE=$ENV{SERVER_SIGNATURE};
//$SIGNATURE = '<div style="padding-left:3px;background-color:#444444;color:#ffffff;">&copy; ZE CMS, Humboldt-Universit&auml;t zu Berlin | Written 2010 by <a style="color:#ffffff;" href="http://amor.cms.hu-berlin.de/~rohdedan/webdav/">Daniel Rohde</a></div>';
//
//## -- LANG
//## defines the default language for the Web interface
//## see %TRANSLATION option for supported languages
//## DEFAULT: $LANG='default';
//$LANG = 'default';
//#$LANG = 'de';
//
//## -- TRANSLATION
//## defines text and tooltips for the Web interface
//## if you add your own translation you don't need to translate all text keys
//## (there is a fallback to the default)
//## Don't use entities like &auml; for buttons and table header (names, lastmodified, size, mimetype).
//%TRANSLATION = ( 'default' => 
//			{
//				search => 'Search for file/folder name:', searchtooltip => 'allowed are: file/folder name, regular expression',
//				searchnothingfound => 'Nothing found for ', searchgoback =>' in ',
//				searchresultsfor => ' search results for ', searchresultfor => ' search result for ',
//				searchresults => ' results in', searchresult => ' result in',
//				mount => '[M]', mounttooltip => 'View this collection in your WebDAV client (WebDAV mount).',
//				quotalimit => 'Quota limit: ', quotaused => ' used: ', quotaavailable => ' available: ',
//				navpage => 'Page ', navfirst=>' |&lt; ', navprev=>' &lt;&lt; ', navnext=>' &gt;&gt; ', navlast=>' &gt;| ', 
//				navall=>'All', navpageview=>'View by page',
//				navfirstblind=>' |&lt; ', navprevblind=>' &lt;&lt; ', navnextblind=>' &gt;&gt; ', navlastblind=>' &gt;| ', 
//				navfirsttooltip=>'First Page', navprevtooltip=>'Previous Page', 
//				navnexttooltip=>'Next Page', navlasttooltip=>'Last Page', navalltooltip=>'Show All',
//				togglealltooltip=>'Toggle All', showproperties=>'Show Properties',
//				properties=>' properties', propertyname=>'Name', propertyvalue=>'Value',
//				names => 'Files/Folders', lastmodified => 'Last Modified', size => 'Size', mimetype => 'MIME Type',
//				lastmodifiedformat => '%d-%b-%Y %H:%M',
//				statfiles => 'files:', statfolders=> 'folders:', statsum => 'sum:', statsize => 'size:',
//				createfoldertext => 'Create new folder: ', createfolderbutton => 'Create Folder',
//				movefilestext => 'Rename/Move selected files/folders to: ', movefilesbutton => 'Rename/Move',
//				movefilesconfirm => 'Do you really want to rename/move selected files/folders to the new file name or folder?',
//				deletefilesbutton => 'Delete', deletefilestext => ' selected files/folders',
//				deletefilesconfirm => 'Do you really want to delete selected files/folders?',
//				zipdownloadbutton => 'Download', zipdownloadtext => ' selected files/folders (zip archive)',
//				zipuploadtext => 'Upload zip archive: ', zipuploadbutton => 'Upload & Extract',
//				zipuploadconfirm => 'Do you really want to upload zip, extract it and replace existing files?',
//				fileuploadtext => 'File: ', fileuploadbutton=> 'Upload', fileuploadmore =>'more',
//				fileuploadconfirm =>'Do you really want to upload file(s) and replace existing file(s)?',
//				confirm => 'Please confirm.',
//				foldernotwriteable => 'This folder is not writeable (no write permission).',
//				foldernotreadable=> 'This folder is not readable (no read permission).',
//				msg_deletedsingle => '%s file/folder was deleted.',
//				msg_deletedmulti => '%s files/folders were deleted.',
//				msg_deleteerr => 'Could not delete selected files/folders.',
//				msg_deletenothingerr => 'Please select file(s)/folder(s) to delete.',
//				msg_foldercreated=>'Folder \'%s\' was created successfully.',
//				msg_foldererr=>'Could not create folder \'%s\' (%s).',
//				msg_foldernothingerr=>'Please specify a folder to create.',
//				msg_rename=>'Moved files/folders \'%s\' to \'%s\'.',
//				msg_renameerr=>'Could not move files/folders \'%s\' to \'%s\'.',
//				msg_renamenothingerr=>'Please select files/folders to rename/move.',
//				msg_renamenotargeterr=>'Please specify a target folder/name for move/rename.',
//				msg_uploadsingle=>'%s file (%s) uploaded successfully.',
//				msg_uploadmulti=>'%s files (%s) uploaded successfully.',
//				msg_uploadnothingerr=>'Please select a local file (Browse ...) for upload.',
//				msg_zipuploadsingle=>'%s zip archive (%s) uploaded successfully.',
//				msg_zipuploadmulti=>'%s zip archives (%s) uploaded successfully.',
//				msg_zipuploadnothingerr=>'Please select a local zip archive (Browse...) for upload.',
//				clickforfullsize=>'Click for full size',
//				permissions=>'Permissions', user=>'user: ', group=>'; group: ', others=>'; others: ',
//				recursive=>'recursive', changefilepermissions=>'Change file permissions: ', changepermissions=>'Change',
//				readable=>'r', writeable=>'w', executable=>'x', sticky=>'t', setuid=>'s', setgid=>'s',
//				add=>'add (+)', set=>'set (=)', remove=>'remove (-)',
//				changepermconfirm=>'Do you really want to change file/folder permissions for selected files/folders?',
//				msg_changeperm=>'Changed file/folder permissions successfully.',
//				msg_chpermnothingerr=>'Please select files/folders to change permissions.',
//				changepermlegend=>'r - read, w - write, x - execute, s - setuid/setgid, t - sticky bit',
//				created=>'created',
//			},
//		'de' => 
//			{
//				search => 'Suche nach Datei-/Ordnernamen:', searchtooltip => 'Namen und reguläre Ausdrücke',
//				searchnothingfound => 'Es wurde nichts gefunden für ', searchgoback =>' in ',
//				searchresultsfor => ' Suchergebnisse für ', searchresultfor => ' Suchergebniss für ',
//				searchresults => ' Suchergebnisse in', searchresult => ' Suchergebniss in',
//				mount => '[M]', mounttooltip => 'Klicken Sie hier, um diesen Ordner in Ihrem lokalen WebDAV-Clienten anzuzeigen.',
//				quotalimit => 'Quota-Limit: ', quotaused => ' verwendet: ', quotaavailable => ' verf&uuml;gbar: ',
//				navpage => 'Seite ', navall=>'Alles', navpageview=>'Seitenweise anzeigen',
//				navfirsttooltip=>'Erste Seite', navprevtooltip=>'Vorherige Seite',
//				navnexttooltip=>'Nächste Seite', navlasttooltip=>'Letzte Seite', navalltooltip=>'Zeige alles auf einer Seite',
//				togglealltooltip=>'Auswahl umkehren', showproperties=>'Datei/Ordner-Attribute anzeigen',
//				properties=>' Attribute', propertyname=>'Name', propertyvalue=>'Wert',
//				names => 'Dateien/Ordner', lastmodified => 'Letzte Änderung', size => 'Größe', mimetype => 'MIME Typ',
//				lastmodifiedformat => '%d.%m.%Y %H:%M Uhr',
//				statfiles => 'Dateien:', statfolders=> 'Ordner:', statsum => 'Gesamt:', statsize => 'Größe:',
//				createfoldertext => 'Neuen Ordner: ', createfolderbutton => 'anlegen',
//				movefilestext => 'Ausgewählte Dateien nach: ', movefilesbutton => 'umbenennen/veschieben',
//				movefilesconfirm => 'Wollen Sie wirklich die ausgewählte(n) Datei(en)/Ordner umbenennen/verschieben?',
//				deletefilesbutton => 'Lösche', deletefilestext => ' alle ausgew&auml;hlten Dateien/Ordner',
//				deletefilesconfirm => 'Wollen Sie wirklich alle ausgewählten Dateien/Ordner löschen?',
//				zipdownloadbutton => 'Download', zipdownloadtext => ' aller ausgewählten Dateien und Ordner als ZIP-Archiv.',
//				zipuploadtext => 'Ein ZIP-Archiv: ', zipuploadbutton => 'hochladen & auspacken.',
//				zipuploadconfirm => 'Wollen Sie das ZIP-Archiv wirklich hochladen, auspacken und damit alle existierenden Dateien ersetzen?',
//				fileuploadtext => 'Datei: ', fileuploadbutton=> 'hochladen', fileuploadmore =>'mehr',
//				fileuploadconfirm =>'Wollen Sie wirklich die Datei(en) hochladen und existierenende Dateien ggf. ersetzen?',
//				confirm => 'Bitte bestätigen Sie.',
//				foldernotwriteable => 'In diesem Ordner darf nicht geschrieben werden (fehlende Zugriffsrechte).',
//				foldernotreadable=> 'Dieser Ordner ist nicht lesbar (fehlende Zugriffsrechte).',
//				msg_deletedsingle => '%s Datei/Ordner gelöscht.',
//				msg_deletedmulti => '%s Dateien/Ordner gelöscht.',
//				msg_deleteerr => 'Konnte die ausgewählten Dateien/Ordner nicht löschen.',
//				msg_deletenothingerr => 'Bitte wählen Sie die zu löschenden Dateien/Ordner aus.',
//				msg_foldercreated=>'Der Ordner "%s" wurde erfolgreich angelegt.',
//				msg_foldererr=>'Konnte den Ordner "%s" nicht anlegen (%s).',
//				msg_foldernothingerr=>'Bitte geben Sie einen Ordner an, der angelegt werden soll.',
//				msg_rename=>'Die Dateien/Ordner "%s" wurden nach "%s" verschoben.',
//				msg_renameerr=>'Konnte die gewählten Dateien/Ordner "%s" nicht nach "%s" verschieben.',
//				msg_renamenothingerr=>'Bitte wählen Sie Dateien/Ordner aus, die ubenannt bzw. verschoben werden sollen.',
//				msg_renamenotargeterr=>'Bitte geben Sie einen Ziel-Order/-Dateinamen an:',
//				msg_uploadsingle=>'%s Datei (%s) wurde erfolgreich hochgeladen.',
//				msg_uploadmulti=>'%s Dateien (%s) wurden erfolgreich hochgeladen.',
//				msg_uploadnothingerr=>'Bitte wählen Sie lokale Dateien zum Hochladen aus.',
//				msg_zipuploadsingle=>'%s zip-Archiv (%s) wurde erfolgreich hochgeladen.',
//				msg_zipuploadmulti=>'%s zip-Archive (%s) wurden erfolgreich hochgeladen.',
//				msg_zipuploadnothingerr=>'Bitte wählen Sie ein lokales Zip-Archiv zum Hochladen aus.',
//				clickforfullsize=>'Für volle Grösse anklicken',
//				permissions=>'Rechte', user=>'Benutzer: ', group=>'; Gruppe: ', others=>'; Andere: ',
//				recursive=>'rekursiv', changefilepermissions=>'Datei-Rechte ändern: ', changepermissions=>'ändern',
//				readable=>'r', writeable=>'w', executable=>'x', sticky=>'t', setuid=>'s', setgid=>'s', 
//				add=>'hinzufügen (+)', set=>'setzen (=)', remove=>'entfernen (-)',
//				changepermconfirm=>'Wolle Sie wirklich die Datei/Ordner-Rechte für die gewählten Dateien/Ordner ändern?',
//				msg_changeperm=>'Datei/Ordner-Rechte erfolgreich geändert.',
//				msg_chpermnothingerr=>'Sie haben keine Dateien/Ordner ausgewählt, für die die Rechte geändert werden sollen.',
//				changepermlegend=>'r - lesen, w - schreiben, x - ausführen, s - setuid/setgid, t - sticky bit',
//				created=>'erzeugt am',
//			},
//
//		);
//$TRANSLATION{'de_DE'} = $TRANSLATION{de};
//$TRANSLATION{'de_DE.UTF8'} = $TRANSLATION{de};
//
//## -- DBI_(SRC/USER/PASS)
//## database setup for LOCK/UNLOCK/PROPPATCH/PROPFIND data
//## EXAMPLE: $DBI_SRC='dbi:SQLite:dbname=/tmp/webdav.'.($ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER}).'.db';
//## ATTENTION: if users share the same folder they should use the same database. The example works only for users with unshared folders and $CREATE_DB should be enabled.
//$DBI_SRC='dbi:SQLite:dbname=/usr/local/www/var/webdav/webdav.'.($ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER}).'.db';
//$DBI_USER="";
//$DBI_PASS="";
//
//## enables persitent database connection (only usefull in conjunction with mod_perl, Speedy/PersistenPerl)
//$DBI_PERSISTENT = 0;
//
//## -- CREATE_DB
//## if set to 1 this script creates the database schema ($DB_SCHEMA)
//## performance hint: if the database schema exists set CREATE_DB to 0
//## DEFAULT: $CREATE_DB = 1;
//$CREATE_DB = 1;
//
//## -- DB_SCHEMA
//## database schema (works with SQlite3 & MySQL5)
//## WARNING!!! do not use a unique index 
//@DB_SCHEMA = (
//	'CREATE TABLE IF NOT EXISTS webdav_locks (basefn VARCHAR(255) NOT NULL, fn VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL, scope VARCHAR(255), token VARCHAR(255) NOT NULL, depth VARCHAR(255) NOT NULL, timeout VARCHAR(255) NULL, owner TEXT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)',
//	'CREATE TABLE IF NOT EXISTS webdav_props (fn VARCHAR(255) NOT NULL, propname VARCHAR(255) NOT NULL, value TEXT)',
//	'CREATE INDEX IF NOT EXISTS webdav_locks_idx1 ON webdav_locks (fn)',
//	'CREATE INDEX IF NOT EXISTS webdav_locks_idx2 ON webdav_locks (basefn)',
//	'CREATE INDEX IF NOT EXISTS webdav_locks_idx3 ON webdav_locks (fn,basefn)',
//	'CREATE INDEX IF NOT EXISTS webdav_locks_idx4 ON webdav_locks (fn,basefn,token)',
//	'CREATE INDEX IF NOT EXISTS webdav_props_idx1 ON webdav_props (fn)',
//	'CREATE INDEX IF NOT EXISTS webdav_props_idx2 ON webdav_props (fn,propname)',
//	);
//
//## -- DEFAULT_LOCK_OWNER
//## lock owner if not given by client
//## EXAMPLE: $DEFAULT_LOCK_OWNER=$ENV{REMOTE_USER}.'@'.$ENV{REMOTE_ADDR}; ## loggin user @ ip
//$DEFAULT_LOCK_OWNER= { href=> ($ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER}).'@'.$ENV{REMOTE_ADDR} };
//
//## -- CHARSET
//## change it if you get trouble with special characters
//## DEFAULT: $CHARSET='utf-8';
//$CHARSET='utf-8';
//# and Perl's UTF-8 pragma for the right string length:
//# use utf8;
//# no utf8;
//
//## -- BUFSIZE
//## buffer size for read and write operations
//$BUFSIZE = 1073741824;
//
//## -- LOGFILE
//## simple log for file/folder modifications (PUT/MKCOL/DELETE/COPY/MOVE)
//## EXAMPLE: $LOGFILE='/tmp/webdavcgi.log';
//# $LOGFILE='/tmp/webdavcgi.log';
//
//## -- GFSQUOTA
//## if you use a GFS/GFS2 filesystem and if you want quota property support set this variable
//## ($FS - will be replaced by the filesystem (filename/folder))
//## EXAMPLE: $GFSQUOTA='/usr/sbin/gfs2_quota -f';
//$GFSQUOTA='/usr/sbin/gfs_quota -f';
//
//## -- ENABLE_LOCK
//## enable/disable lock/unlock support (WebDAV compiance class 2) 
//## if disabled it's unsafe for shared collections/files but improves performance 
//$ENABLE_LOCK = 1;
//
//## -- ENABLE_ACL
//## enable ACL support: only Unix like read/write access changes for user/group/other are supported
//$ENABLE_ACL = 1;
//
//## --- CURRENT_USER_PRINCIPAL
//## a virtual URI for ACL principals
//## for Apple's iCal &  Addressbook
//$CURRENT_USER_PRINCIPAL = "/principals/".($ENV{REDIRECT_REMOTE_USER} || $ENV{REMOTE_USER}) .'/';
//
//## -- PRINCIPAL_COLLECTION_SET 
//## don't change it for MacOS X Addressbook support
//## DEFAULT: $PRINCIPAL_COLLECTION_SET = '/directory/';
//$PRINCIPAL_COLLECTION_SET = '/directory/';
//
//## -- ENABLE_CALDAV
//## enable CalDAV support for Lightning/Sunbird/iCal/iPhone calender/task support
//$ENABLE_CALDAV = 1;
//
//## -- CALENDAR_HOME_SET
//## maps UID numbers or remote users (accounts) to calendar folders
//%CALENDAR_HOME_SET = ( default=> '/', 1000 =>  '/caldav'  );
//
//## -- ENABLE_CALDAV_SCHEDULE
//## really incomplete (ALPHA) - properties exists but POST requests are not supported yet
//$ENABLE_CALDAV_SCHEDULE = 0;
//
//## -- ENABLE_CARDDAV
//## enable CardDAV support for Apple's Addressbook
//$ENABLE_CARDDAV = 1;
//
//## -- ADDRESSBOOK_HOME_SET
//## maps UID numbers or remote users to addressbook folders 
//%ADDRESSBOOK_HOME_SET = ( default=> '/',  1000 => '/carddav/'  );
//
//## -- ENABLE_TRASH
//## enables the server-side trash can (don't forget to setup $TRASH_FOLDER)
//$ENABLE_TRASH = 0;
//
//## -- TRASH_FOLDER
//## neccessary if you enable trash 
//## it should be writable by your users (chmod a+rwxt <trash folder>)
//## EXAMPLE: $TRASH_FOLDER = '/tmp/trash';
//$TRASH_FOLDER = '/usr/local/www/var/trash';
//
//## -- ENABLE_GROUPDAV
//## enables GroupDAV (http://groupdav.org/draft-hess-groupdav-01.txt)
//## EXAMPLE: $ENABLE_GROUPDAV = 0;
//$ENABLE_GROUPDAV = 1;
//
//## -- ENABLE_SEARCH
//##  enables server-side search (WebDAV SEARCH/DASL, RFC5323)
//## EXAMPLE: $ENABLE_SEARCH = 1;
//$ENABLE_SEARCH = 1;
//
//## -- ENABLE_THUMBNAIL
//## enables image thumbnail support for folder listings of the Web interface.
//## If enabled the default icons for images will be replaced by thumbnails
//## and if the mouse is over a icon the icon will be zoomed to the size of $THUMBNAIL_WIDTH.
//## DEFAULT: $ENABLE_THUMBNAIL = 0;
//$ENABLE_THUMBNAIL = 1;
//
//## -- ENABLE_THUMBNAIL_CACHE
//## enable image thumbnail caching (improves performance - 2x faster)
//## DEFAULT: $ENABLE_THUMBNAIL_CACHE = 0;
//$ENABLE_THUMBNAIL_CACHE = 1;
//
//## -- THUMBNAIL_WIDTH
//## defines the width of a image thumbnail
//$THUMBNAIL_WIDTH=110;
//
//## -- THUMBNAIL_CACHEDIR
//## defines the path to a cache directory for image thumbnails
//## this is neccessary if you enable the thumbnail cache ($ENABLE_THUMBNAIL_CACHE)
//## EXAMPLE: $THUMBNAIL_CACHEDIR=".thumbs";
//$THUMBNAIL_CACHEDIR="/usr/local/www/tmp/thumbs";
//
//## -- ENABLE_BIND
//## enables BIND/UNBIND/REBIND methods defined in http://tools.ietf.org/html/draft-ietf-webdav-bind-27
//## EXAMPLE: $ENABLE_BIND = 1;
//$ENABLE_BIND = 1;
//
//## -- FILECOUNTLIMIT
//## limits the number of files/folders listed per folder by PROPFIND requests or Web interface browsing  
//## (this will be overwritten by FILECOUNTPERDIRLIMIT)
//## EXAMPLE: $FILECOUNTLIMIT = 5000;
//$FILECOUNTLIMIT = 5000;
//
//## -- FILECOUNTPERDIRLIMIT
//## limits the number of files/folders listed by PROPFIND requests or the Web interface
//## a value less than 1 prevents a 'opendir'
//## (don't forget the trailing slash '/')
//## EXAMPLE: %FILECOUNTPERDIRLIMIT = ( '/afs/.cms.hu-berlin.de/user/' => 5, '/usr/local/www/htdocs/rohdedan/test/' => 4 );
//%FILECOUNTPERDIRLIMIT = ( '/afs/.cms.hu-berlin.de/user/' => -1, '/usr/local/www/htdocs/rohdedan/test/' => 2 );
//
//## -- FILEFILTERPERDIR
//## filter the visible files/folders per directory listed by PROPFIND or the Web interface
//## you can use full Perl's regular expressions for the filter value
//## SYNTAX: <my absolute path with trailing slash> => <my regex for visible files>;
//## EXAMPLE: 
//##   ## show only the user home in the AFS home dir 'user' of the cell '.cms.hu-berlin.de'
//##   my $_ru = (split(/\@/, ($ENV{REMOTE_USER}||$ENV{REDIRECT_REMOTE_USER})))[0];
//##   %FILEFILTERPERDIR = ( '/afs/.cms.hu-berlin.de/user/' => "$_ru\$", '/usr/local/www/htdocs/rohdedan/links/'=>'loop[1-4]$');
//my $_ru = (split(/\@/, ($ENV{REMOTE_USER}||$ENV{REDIRECT_REMOTE_USER})))[0];
//%FILEFILTERPERDIR = ( '/afs/.cms.hu-berlin.de/user/' => "$_ru\$", '/usr/local/www/htdocs/rohdedan/links/'=>'loop[1-4]$');
//
//## -- IGNOREFILEPERMISSIONS
//## if enabled all unreadable files and folders are clickable for full AFS support
//## it's not a security risk because process rights and file permissions will work
//## EXAMPLE: $IGNOREFILEPERMISSIONS = 0;
//$IGNOREFILEPERMISSIONS = 0;
//
//## -- DEBUG
//## enables/disables debug output
//## you can find the debug output in your web server error log
//$DEBUG = 0;
//
//############  S E T U P - END ###########################################
//#########################################################################
//
//use strict;
//#use warnings;
//
//use CGI;
//
//use File::Basename;
//
//use File::Spec::Link;
//
//use XML::Simple;
//use Date::Parse;
//use POSIX qw(strftime);
//
//use URI::Escape;
//use OSSP::uuid;
//use Digest::MD5;
//
//use DBI;
//
//use Quota;
//
//use Archive::Zip;
//
//use Graphics::Magick;
//
//do($CONFIGFILE) if defined $CONFIGFILE && -e $CONFIGFILE;
//
//## flush immediately:
//$|=1;
//
//umask $UMASK;
//
//## read mime.types file once:
//readMIMETypes($MIMEFILE) if defined $MIMEFILE;
//$MIMEFILE=undef;
//
//
//## before 'new CGI' to read POST requests:
//$ENV{REQUEST_METHOD}=$ENV{REDIRECT_REQUEST_METHOD} if (defined $ENV{REDIRECT_REQUEST_METHOD}) ;
//
//$CGI::POST_MAX = $POST_MAX_SIZE;
//$CGI::DISABLE_UPLOADS = $ALLOW_POST_UPLOADS?0:1;
//
//## supported DAV compliant classes:
//our $DAV='1';
//$DAV.=', 2' if $ENABLE_LOCK;
//$DAV.=', 3, <http://apache.org/dav/propset/fs/1>, extended-mkcol';
//$DAV.=', access-control' if $ENABLE_ACL || $ENABLE_CALDAV || $ENABLE_CARDDAV;
//$DAV.=', calendar-access, calendarserver-private-comments' if $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE; 
//$DAV.=', calendar-auto-schedule' if  $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE;
//$DAV.=', addressbook' if $ENABLE_CARDDAV;
//$DAV.=', bind' if $ENABLE_BIND;
//
//## create CGI instance
//our $cgi = $ENV{REQUEST_METHOD} eq 'PUT' ? new CGI({}) : new CGI;
//my $method = $cgi->request_method();
//
//our $PATH_TRANSLATED = $ENV{PATH_TRANSLATED};
//our $REQUEST_URI = $ENV{REQUEST_URI};
//
//debug("$0 called with UID='$<' EUID='$>' GID='$(' EGID='$)' method=$method");
//debug("User-Agent: $ENV{HTTP_USER_AGENT}");
//
//debug("$0: X-Litmus: ".$cgi->http("X-Litmus")) if defined $cgi->http("X-Litmus");
//debug("$0: X-Litmus-Second: ".$cgi->http("X-Litmus-Second")) if defined $cgi->http("X-Litmus-Second");
//
//# 404/rewrite/redirect handling:
//if (!defined $PATH_TRANSLATED) {
//	$PATH_TRANSLATED = $ENV{REDIRECT_PATH_TRANSLATED};
//
//	if (!defined $PATH_TRANSLATED && (defined $ENV{SCRIPT_URL} || defined $ENV{REDIRECT_URL})) {
//		my $su = $ENV{SCRIPT_URL} || $ENV{REDIRECT_URL};
//		$su=~s/^$VIRTUAL_BASE//;
//		$PATH_TRANSLATED = $DOCUMENT_ROOT.$su;
//	}
//}
//
//
//# protect against direct CGI script call:
//if (!defined $PATH_TRANSLATED || $PATH_TRANSLATED eq "") {
//	debug('FORBIDDEN DIRECT CALL!');
//	printHeaderAndContent('404 Not Found');
//	exit();
//}
//
//$PATH_TRANSLATED.='/' if -d $PATH_TRANSLATED && $PATH_TRANSLATED !~ /\/$/; 
//$REQUEST_URI=~s/\?.*$//; ## remove query strings
//$REQUEST_URI.='/' if -d $PATH_TRANSLATED && $REQUEST_URI !~ /\/$/;
//
//$TRASH_FOLDER.='/' if $TRASH_FOLDER !~ /\/$/;
//
//if (grep(/^\Q$<\E$/, @FORBIDDEN_UID)>0) {
//	debug("Forbidden UID");
//	printHeaderAndContent('403 Forbidden');
//	exit(0);
//}
//
//#### PROPERTIES:
//# from RFC2518:
//#    creationdate, displayname, getcontentlanguage, getcontentlength, 
//#    getcontenttype, getetag, getlastmodified, lockdiscovery, resourcetype,
//#    source, supportedlock
//# from RFC4918:
//#    -source
//# from RFC4331:
//#    quota-available-bytes, quota-used-bytes
//# from draft-hopmann-collection-props-00.txt:
//#    childcount, defaultdocument (live), id, isfolder, ishidden, isstructureddocument, 
//#    hassubs, nosubs, objectcount, reserved, visiblecount
//# from MS-WDVME:
//#    iscollection, isFolder, ishidden (=draft), 
//#    Repl:authoritative-directory, Repl:resourcetag, Repl:repl-uid,
//#    Office:modifiedby, Office:specialFolderType (dead),
//#    Z:Win32CreationTime, Z:Win32FileAttributes, Z:Win32LastAccessTime, Z:Win32LastModifiedTime
//# from reverse engineering:
//#    name, href, parentname, isreadonly, isroot, getcontentclass, lastaccessed, contentclass
//#    executable
//# from RFC3744 (ACL):
//#    owner, group, supported-privilege-set, current-user-privilege-set, acl, acl-restrictions
//# from RFC4791 (CalDAV):
//#    calendar-description, calendar-timezone, supported-calendar-component-set, supported-calendar-data,
//#    max-resource-size, min-date-time, max-date-time, max-instances, max-attendees-per-instance,
//#    calendar-home-set,
//# from http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-ctag.txt
//#    getctag
//# from RFC5397 (WebDAV Current User Principal)
//#    current-user-principal
//# from http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-08
//#    principal: schedule-inbox-URL, schedule-outbox-URL, calendar-user-type, calendar-user-address-set,
//#    collection: schedule-calendar-transp,schedule-default-calendar-URL,schedule-tag
//# from http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-pubsubdiscovery.txt
//# from RFC3253 (DeltaV)
//#    supported-report-set
//#    supported-method-set for RFC5323 (DASL/SEARCH):
//# from http://datatracker.ietf.org/doc/draft-ietf-vcarddav-carddav/
//#    collection: addressbook-description, supported-address-data 
//#    principal: addressbook-home-set, principal-address
//#    report: address-data
//# from RFC5842 (bind)
//#    resource-id, parent-set (unsupported yet)
//
//
//@KNOWN_COLL_PROPS = ( 
//			'creationdate', 'displayname','getcontentlanguage', 
//			'getlastmodified', 'lockdiscovery', 'resourcetype', 
//			'getetag', 'getcontenttype',
//			'supportedlock', 'source',
//			'quota-available-bytes', 'quota-used-bytes', 'quota', 'quotaused',
//			'childcount', 'id', 'isfolder', 'ishidden', 'isstructureddocument',
//			'hassubs', 'nosubs', 'objectcount', 'reserved', 'visiblecount',
//			'iscollection', 'isFolder', 
//			'authoritative-directory', 'resourcetag', 'repl-uid',
//			'modifiedby', 
//			'Win32CreationTime', 'Win32FileAttributes', 'Win32LastAccessTime', 'Win32LastModifiedTime', 
//			'name','href', 'parentname', 'isreadonly', 'isroot', 'getcontentclass', 'lastaccessed', 'contentclass',
//			'supported-report-set', 'supported-method-set',
//			);
//@KNOWN_ACL_PROPS = (
//			'owner','group','supported-privilege-set', 'current-user-privilege-set', 'acl', 'acl-restrictions',
//			'inherited-acl-set', 'principal-collection-set', 'current-user-principal'
//		      );
//@KNOWN_CALDAV_COLL_PROPS = (
//			'calendar-description', 'calendar-timezone', 'supported-calendar-component-set',
//			'supported-calendar-data', 'max-resource-size', 'min-date-time',
//			'max-date-time', 'max-instances', 'max-attendees-per-instance',
//			'getctag',
//		        'principal-URL', 'calendar-home-set', 'schedule-inbox-URL', 'schedule-outbox-URL',
//			'calendar-user-type', 'schedule-calendar-transp', 'schedule-default-calendar-URL',
//			'schedule-tag', 'calendar-user-address-set',
//			);
//@KNOWN_CALDAV_FILE_PROPS = ( 'calendar-data' );
//
//@KNOWN_CARDDAV_COLL_PROPS = ('addressbook-description', 'supported-address-data', 'addressbook-home-set', 'principal-address');
//@KNOWN_CARDDAV_FILE_PROPS = ('address-data');
//
//@KNOWN_COLL_LIVE_PROPS = ( );
//@KNOWN_FILE_LIVE_PROPS = ( );
//@KNOWN_CALDAV_COLL_LIVE_PROPS = ( 'resourcetype', 'displayname', 'calendar-description', 'calendar-timezone', 'calendar-user-address-set');
//@KNOWN_CALDAV_FILE_LIVE_PROPS = ( );
//@KNOWN_CARDDAV_COLL_LIVE_PROPS = ( 'addressbook-description');
//@KNOWN_CARDDAV_FILE_LIVE_PROPS = ( );
//
//push @KNOWN_COLL_LIVE_PROPS, @KNOWN_CALDAV_COLL_LIVE_PROPS if $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE || $ENABLE_CARDDAV;
//push @KNOWN_FILE_LIVE_PROPS, @KNOWN_CALDAV_FILE_LIVE_PROPS if $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE || $ENABLE_CARDDAV;
//push @KNOWN_COLL_LIVE_PROPS, @KNOWN_CARDDAV_COLL_LIVE_PROPS if $ENABLE_CARDDAV;
//push @KNOWN_COLL_PROPS, @KNOWN_ACL_PROPS if $ENABLE_ACL || $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE || $ENABLE_CARDDAV;
//push @KNOWN_COLL_PROPS, @KNOWN_CALDAV_COLL_PROPS if $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE;
//push @KNOWN_COLL_PROPS, @KNOWN_CARDDAV_COLL_PROPS if $ENABLE_CARDDAV;
//push @KNOWN_COLL_PROPS, 'resource-id' if $ENABLE_BIND;
//
//
//@KNOWN_FILE_PROPS = ( @KNOWN_COLL_PROPS, 'getcontentlength', 'executable' );
//push @KNOWN_FILE_PROPS, @KNOWN_CALDAV_FILE_PROPS if $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE;
//push @KNOWN_FILE_PROPS, @KNOWN_CARDDAV_FILE_PROPS if $ENABLE_CARDDAV;
//
//push @KNOWN_COLL_PROPS, 'component-set' if $ENABLE_GROUPDAV;
//
//@UNSUPPORTED_PROPS = ( 'checked-in', 'checked-out', 'xmpp-uri', 'dropbox-home-URL' ,'appledoubleheader','parent-set' );
//
//@PROTECTED_PROPS = ( @UNSUPPORTED_PROPS, 
//			'getcontentlength', 'getcontenttype', 'getetag', 'lockdiscovery', 
//			'source', 'supportedlock',
//			'supported-report-set',
//			'quota-available-bytes, quota-used-bytes', 'quota', 'quotaused',
//			'childcount', 'id', 'isfolder', 'ishidden', 'isstructureddocument', 
//			'hassubs', 'nosubs', 'objectcount', 'reserved', 'visiblecount',
//			'iscollection', 'isFolder',
//			'authoritative-directory', 'resourcetag', 'repl-uid',
//			'modifiedby', 
//			'name', 'href', 'parentname', 'isreadonly', 'isroot', 'getcontentclass', 'contentclass',
//			'owner', 'group', 'supported-privilege-set', 'current-user-privilege-set', 
//			'acl', 'acl-restrictions', 'inherited-acl-set', 'principal-collection-set',
//			'supported-calendar-component-set','supported-calendar-data', 'max-resource-size',
//			'min-date-time','max-date-time','max-instances','max-attendees-per-instance', 'getctag',
//			'current-user-principal', 
//			'calendar-user-address-set', 'schedule-inbox-URL', 'schedule-outbox-URL', 'schedule-calendar-transp',
//			'schedule-default-calendar-URL', 'schedule-tag', 'supported-address-data', 
//			'supported-collation-set', 'supported-method-set', 'supported-method',
//			'supported-query-grammar'
//		);
//
//@ALLPROP_PROPS = ( 'creationdate', 'displayname', 'getcontentlanguage', 'getlastmodified', 
//			'lockdiscovery', 'resourcetype','supportedlock', 'getetag', 'getcontenttype', 
//			'getcontentlength', 'executable' );
//
//
//### XML
//%NAMESPACES = ( 'DAV:'=>'D', 'http://apache.org/dav/props/'=>'lp2', 'urn:schemas-microsoft-com:' => 'Z', 'urn:schemas-microsoft-com:datatypes'=>'M', 'urn:schemas-microsoft-com:office:office' => 'Office', 'http://schemas.microsoft.com/repl/' => 'Repl', 'urn:ietf:params:xml:ns:caldav'=>'C', 'http://calendarserver.org/ns/'=>'CS', 'http://www.apple.com/webdav_fs/props/'=>'Apple', 'http://www.w3.org/2000/xmlns/'=>'x', 'urn:ietf:params:xml:ns:carddav' => 'A', 'http://www.w3.org/2001/XMLSchema'=>'xs', 'http://groupdav.org/'=>'G');
//
//%ELEMENTS = ( 	'calendar'=>'C','calendar-description'=>'C', 'calendar-timezone'=>'C', 'supported-calendar-component-set'=>'C',
//		'supported-calendar-data'=>'C', 'max-resource-size'=>'C', 'min-date-time'=>'C',
//		'max-date-time'=>'C','max-instances'=>'C', 'max-attendees-per-instance'=>'C',
//		'read-free-busy'=>'C', 'calendar-home-set'=>'C', 'supported-collation-set'=>'C', 'schedule-tag'=>'C',
//		'calendar-data'=>'C', 'mkcalendar-response'=>'C', getctag=>'CS',
//		'calendar-user-address-set'=>'C', 'schedule-inbox-URL'=>'C', 'schedule-outbox-URL'=>'C',
//		'calendar-user-type'=>'C', 'schedule-calendar-transp'=>'C', 'schedule-default-calendar-URL'=>'C',
//		'schedule-inbox'=>'C', 'schedule-outbox'=>'C', 'transparent'=>'C',
//		'calendar-multiget'=>'C', 'calendar-query'=>'C', 'free-busy-query'=>'C',
//		'addressbook'=>'A', 'addressbook-description'=>'A', 'supported-address-data'=>'A', 'addressbook-home-set'=>'A', 'principal-address'=>'A',
//		'address-data'=>'A',
//		'addressbook-query'=>'A', 'addressbook-multiget'=>'A',
//		'string'=>'xs', 'anyURI'=>'xs', 'nonNegativeInteger'=>'xs', 'dateTime'=>'xs',
//		'vevent-collection'=>'G', 'vtodo-collection'=>'G', 'vcard-collection'=>'G', 'component-set'=>'G',
//		'executable'=>'lp2','Win32CreationTime'=>'Z', 'Win32LastModifiedTime'=>'Z', 'Win32LastAccessTime'=>'Z', 
//		'authoritative-directory'=>'Repl', 'resourcetag'=>'Repl', 'repl-uid'=>'Repl', 'modifiedby'=>'Office', 'specialFolderType'=>'Office',
//		'Win32CreationTime'=>'Z', 'Win32FileAttributes'=>'Z', 'Win32LastAccessTime'=>'Z', 'Win32LastModifiedTime'=>'Z',default=>'D' );
//
//%NAMESPACEABBR = ( 'D'=>'DAV:', 'lp2'=>'http://apache.org/dav/props/', 'Z'=>'urn:schemas-microsoft-com:', 'Office'=>'urn:schemas-microsoft-com:office:office','Repl'=>'http://schemas.microsoft.com/repl/', 'M'=>'urn:schemas-microsoft-com:datatypes', 'C'=>'urn:ietf:params:xml:ns:caldav', 'CS'=>'http://calendarserver.org/ns/', 'Apple'=>'http://www.apple.com/webdav_fs/props/', 'A'=> 'urn:ietf:params:xml:ns:carddav', 'xs'=>'http://www.w3.org/2001/XMLSchema', 'G'=>'http://groupdav.org/');
//
//%DATATYPES = ( isfolder=>'M:dt="boolean"', ishidden=>'M:dt="boolean"', isstructureddocument=>'M:dt="boolean"', hassubs=>'M:dt="boolean"', nosubs=>'M:dt="boolean"', reserved=>'M:dt="boolean"', iscollection =>'M:dt="boolean"', isFolder=>'M:dt="boolean"', isreadonly=>'M:dt="boolean"', isroot=>'M:dt="boolean"', lastaccessed=>'M:dt="dateTime"', Win32CreationTime=>'M:dt="dateTime"',Win32LastAccessTime=>'M:dt="dateTime"',Win32LastModifiedTime=>'M:dt="dateTime"', description=>'xml:lang="en"');
//
//%NAMESPACEELEMENTS = ( 'multistatus'=>1, 'prop'=>1 , 'error'=>1, 'principal-search-property-set'=>1);
//
//%ELEMENTORDER = ( multistatus=>1, responsedescription=>4, 
//			allprop=>1, include=>2,
//			prop=>1, propstat=>2,status=>3, error=>4,
//			href=>1, responsedescription=>5, location=>6,
//			locktype=>1, lockscope=>2, depth=>3, owner=>4, timeout=>5, locktoken=>6, lockroot=>7, 
//			getcontentlength=>1001, getlastmodified=>1002, 
//			resourcetype=>0,
//			getcontenttype=>1, 
//			supportedlock=>1010, lockdiscovery=>1011, 
//			src=>1,dst=>2,
//			principal => 1, grant => 2,
//			privilege => 1, abstract=> 2, description => 3, 'supported-privilege' => 4,
//			collection=>1, calendar=>2, 'schedule-inbox'=>3, 'schedule-outbox'=>4,
//			'calendar-data'=>101, getetag=>100,
//			properties => 1, operators=>2,
//			default=>1000);
//%SEARCH_PROPTYPES = ( default=>'string',
//			  '{DAV:}getlastmodified'=> 'dateTime', '{DAV:}lastaccessed'=>'dateTime', '{DAV:}getcontentlength' => 'int', 
//			  '{DAV:}creationdate' => 'dateTime','{urn:schemas-microsoft-com:}Win32CreationTime' =>'dateTime', 
//			  '{urn:schemas-microsoft-com:}Win32LastAccessTime'=>'dateTime',  '{urn:schemas-microsoft-com:}Win32LastModifiedTime'=>'dateTime',
//			  '{DAV:}childcount'=>'int', '{DAV:}objectcount'=>'int','{DAV:}visiblecount'=>'int',
//			  '{DAV:}acl'=>'xml', '{DAV:}acl-restrictions'=>'xml','{urn:ietf:params:xml:ns:carddav}addressbook-home-set'=>'xml',
//			  '{urn:ietf:params:xml:ns:caldav}calendar-home-set'=>'xml', '{DAV:}current-user-principal}'=>'xml',
//			  '{DAV:}current-user-privilege-set'=>'xml', '{DAV:}group'=>'xml',
//			  '{DAV:}owner'=>'xml', '{urn:ietf:params:xml:ns:carddav}principal-address'=>'xml',
//			  '{DAV:}principal-collection-set'=>'xml', '{DAV:}principal-URL'=>'xml',
//			  '{DAV:}resourcetype'=>'xml', '{urn:ietf:params:xml:ns:caldav}schedule-calendar-transp'=>'xml',
//			  '{urn:ietf:params:xml:ns:caldav}schedule-inbox-URL'=>'xml', '{urn:ietf:params:xml:ns:caldav}schedule-outbox-URL'=>'xml',
//			  '{DAV:}source'=>'xml', '{urn:ietf:params:xml:ns:carddav}supported-address-data'=>'xml',
//			  '{urn:ietf:params:xml:ns:caldav}supported-calendar-component-set'=>'xml','{urn:ietf:params:xml:ns:caldav}supported-calendar-data'=>'xml',
//			  '{DAV:}supported-method-set'=>'xml','{DAV:}supported-privilege-set'=>'xml','{DAV:}supported-report-set'=>'xml',
//			  '{DAV:}supportedlock'=>'xml'
//			);
//%SEARCH_SPECIALCONV = ( dateTime => 'str2time', xml=>'convXML2Str' );
//%SEARCH_SPECIALOPS = ( int => { eq => '==', gt => '>', lt =>'<', gte=>'>=', lte=>'<=', cmp=>'<=>' }, 
//                           dateTime => { eq => '==', gt => '>', lt =>'<', gte=>'>=', lte=>'<=', cmp=>'<=>' }, 
//                           string => { lte=>'le', gte=>'ge' } );
//
//@IGNORE_PROPS = ( 'xmlns', 'CS');
//
//# method handling:
//if ($method=~/^(GET|HEAD|POST|OPTIONS|PROPFIND|PROPPATCH|MKCOL|PUT|COPY|MOVE|DELETE|LOCK|UNLOCK|GETLIB|ACL|REPORT|MKCALENDAR|SEARCH|BIND|UNBIND|REBIND)$/) { 
//
//	### performance is bad:
//#	eval "_${method}();" ;
//#	if ($@) {
//#		print STDERR "$0: Missing method handler for '$method'\n$@";
//#		printHeaderAndContent('501 Not Implemented');
//#	}
//	### performance is much better than eval:
//	gotomethod($method);
//	if (!$DBI_PERSISTENT && $DBI_INIT) {
//		$DBI_INIT->disconnect();
//		$DBI_INIT=undef;
//	}
//} else {
//	printHeaderAndContent('405 Method Not Allowed');
//}
//sub gotomethod {
//	my ($method) = @_;
//	$method="_$method";
//	goto &$method; ## I use 'goto' so I don't need 'no strict "refs"' and 'goto' works only in a subroutine
//}
//
//sub _GET {
//	my $fn = $PATH_TRANSLATED;
//	debug("_GET: $fn");
//
//	$LANG = $cgi->param('lang') || $cgi->cookie('lang') || $LANG;
//
//	if (is_hidden($fn)) {
//		printHeaderAndContent('404 Not Found','text/plain','404 - NOT FOUND');
//	} elsif (-d $fn && !$FANCYINDEXING) {
//		printHeaderAndContent('404 Not Found','text/plain','404 - NOT FOUND');
//	} elsif (-e $fn && $cgi->param('action') eq 'davmount') {
//		my $su = $ENV{REDIRECT_SCRIPT_URI} || $ENV{SCRIPT_URI};
//		my $bn = basename($fn);
//		$su =~ s/\Q$bn\E\/?//;
//		$bn.='/' if -d $fn && $bn!~/\/$/;
//		printHeaderAndContent('200 OK','application/davmount+xml',
//		       qq@<dm:mount xmlns:dm="http://purl.org/NET/webdav/mount"><dm:url>$su</dm:url><dm:open>$bn</dm:open></dm:mount>@);
//	} elsif ($ENABLE_THUMBNAIL  && -f $fn && -r $fn && $cgi->param('action') eq 'thumb') {
//		my $image = Graphics::Magick->new;
//		my $width = $THUMBNAIL_WIDTH || $ICON_WIDTH || 22;
//		if ($ENABLE_THUMBNAIL_CACHE) {
//			my $uniqname = $fn;
//			$uniqname=~s/\//_/g;
//			my $cachefile = "$THUMBNAIL_CACHEDIR/$uniqname.thumb";
//			mkdir($THUMBNAIL_CACHEDIR) if ! -e $THUMBNAIL_CACHEDIR;
//			if (! -e $cachefile || (stat($fn))[9] > (stat($cachefile))[9]) {
//				my $x;
//				$x = $image->Read($fn); warn "$x" if "$x";
//				$x = $image->Resize(geometry=>$width,filter=>'Gaussian'); warn "$x" if "$x";
//				$x = $image->Crop(geometry=>"${width}x${width}+0+0"); warn "$x" if "$x";
//				$x = $image->Write($cachefile); warn "$x" if "$x";
//			}
//			if (open(my $cf, "<$cachefile")) {
//				print $cgi->header(-status=>'200 OK',-type=>getMIMEType($fn), -ETag=>getETag($cachefile), -Content-length=>(stat($cachefile))[7]);
//				binmode $cf;
//				binmode STDOUT;
//				print while(<$cf>);
//				close($cf);
//			}
//		} else {
//			print $cgi->header(-status=>'200 OK',-type=>getMIMEType($fn), -ETag=>getETag($fn));
//			my $x;
//			$x = $image->Read($fn); warn "$x" if "$x";
//			$x = $image->Resize(geometry=>$width,filter=>'Gaussian'); warn "$x" if "$x";
//			$x = $image->Crop(geometry=>"${width}x${width}+0+0"); warn "$x" if "$x";
//			binmode STDOUT;
//			$x = $image->Write('-'); warn "$x" if "$x";
//		}
//	} elsif (-e $fn && $cgi->param('action') eq 'props') {
//		my $content = "";
//		$content .= start_html("$REQUEST_URI properties");
//		$content .= $LANGSWITCH if defined $LANGSWITCH;
//		$content .= $HEADER if defined $HEADER;
//		my $fullparent = dirname($REQUEST_URI) .'/';
//		$fullparent = '/' if $fullparent eq '//' || $fullparent eq '';
//		$content .=$cgi->h1( (-d $fn ? getQuickNavPath($REQUEST_URI,getQueryParams()) 
//					     : getQuickNavPath($fullparent,getQueryParams())
//					       .' '.$cgi->a({-href=>$REQUEST_URI}, basename($REQUEST_URI))
//				      ). _tl('properties'));
//		$content .= $cgi->a({href=>$REQUEST_URI,title=>_tl('clickforfullsize')},$cgi->img({-src=>$REQUEST_URI.($ENABLE_THUMBNAIL?'?action=thumb':''), -alt=>'image', -style=>'border:0; width:'.($ENABLE_THUMBNAIL?$THUMBNAIL_WIDTH:200)})) if getMIMEType($fn) =~ /^image\//;
//		$content .= $cgi->start_table({-style=>'width:100%;table-layout:fixed;'});
//		local(%NAMESPACEELEMENTS);
//		my $dbprops = db_getProperties($fn);
//		my @bgcolors = @{$COLORS{'rowbgcolors'}};
//		my (%visited);
//		$content.=$cgi->Tr({-style=>"background-color:#dddddd;text-align:left"}, $cgi->th({-style=>'width:25%'},_tl('propertyname')), $cgi->th({-style=>'width:75%'},_tl('propertyvalue')));
//		foreach my $prop (sort {nonamespace(lc($a)) cmp nonamespace(lc($b)) } keys %{$dbprops},-d $fn ? @KNOWN_COLL_PROPS : @KNOWN_FILE_PROPS ) {
//			my (%r200);
//			next if exists $visited{$prop} || exists $visited{'{'.getNameSpaceUri($prop).'}'.$prop};
//			if (exists $$dbprops{$prop}) {
//				$r200{prop}{$prop}=$$dbprops{$prop};
//			} else {
//				getProperty($fn, $REQUEST_URI, $prop, undef, \%r200, \my %r404);
//			}
//			$visited{$prop}=1;
//			$NAMESPACEELEMENTS{nonamespace($prop)}=1;
//			my $title = createXML($r200{prop},1);
//			my $value = createXML($r200{prop}{$prop},1);
//			my $namespace = getNameSpaceUri($prop);
//			if ($prop =~ /^\{([^\}]*)\}/) {
//				$namespace = $1;
//			}
//			push @bgcolors,  shift @bgcolors;
//			$content.= $cgi->Tr( {-style=>"background-color:$bgcolors[0]; text-align:left" },
//				 $cgi->th({-title=>$namespace, -style=>'vertical-align:top;'},nonamespace($prop))
//				.$cgi->td({-title=>$title, -style=>'vertical-align:bottom;' }, 
//						$cgi->pre({style=>'margin:0px; overflow:auto;'},$cgi->escapeHTML($value)))
//				);
//		}
//		$content.=$cgi->end_table();
//		$content.=$cgi->hr().$SIGNATURE if defined $SIGNATURE;
//		$content.=$cgi->end_html();
//		printHeaderAndContent('200 OK', 'text/html', $content, 'Cache-Control: no-cache, no-store');
//	} elsif (-d $fn) {
//		my $ru = $REQUEST_URI;
//		my $content = "";
//		debug("_GET: directory listing of $fn");
//		$content .= start_html($ru);
//		$content .= $LANGSWITCH if defined $LANGSWITCH;
//		$content .= $HEADER if defined $HEADER;
//		if ($ALLOW_SEARCH && -r $fn) {
//			my $search = $cgi->param('search');
//			$content .= $cgi->start_form(-method=>'GET');
//			$content .= $cgi->div({-style=>'text-align:right;font-size:0.8em;padding:2px 0 0 0;border:0;margin:0;'}, _tl('search'). ' '. $cgi->input({-title=>_tl('searchtooltip'),-onkeyup=>'javascript:if (this.size<this.value.length || (this.value.length<this.size && this.value.length>10)) this.size=this.value.length;', -style=>'font-size: 0.8em;', -name=>'search',-size=>$search?(length($search)>10?length($search):10):10, -value=>defined $search?$search:''}));
//			$content .= $cgi->end_form();
//		}
//		if ( my $msg = $cgi->param('errmsg') || $cgi->param('msg')) {
//			my @params = ();
//			my $p=1;
//			while (defined $cgi->param("p$p")) {
//				push @params, $cgi->escapeHTML($cgi->param("p$p"));
//				$p++;
//			}
//			$content .= $cgi->div({-style=>'background-color:'.($cgi->param('errmsg')?'#ffeeee':'#eeeeff')}, sprintf(_tl('msg_'.$msg),@params));
//		}
//		if ($cgi->param('search')) {
//			$content.=getSearchResult($cgi->param('search'),$fn,$ru);
//		} else {
//			$content .= $cgi->div({-style=>'background-color:#ffeeee'}, _tl('foldernotwriteable')) if (!-w $fn) ;
//			$content .= $cgi->div({-style=>'background-color:#ffeeee'}, _tl('foldernotreadable')) if (!-r $fn) ;
//
//			my ($list, $count) = getFolderList($fn,$ru);
//			$content.=$list;
//			if ($ALLOW_FILE_MANAGEMENT && ($IGNOREFILEPERMISSIONS || -w $fn)) {
//				$content.=$cgi->hr();
//				$content.='&bull; '._tl('createfoldertext').$cgi->input({-name=>'colname', -size=>30}).$cgi->submit(-name=>'mkcol',-value=>_tl('createfolderbutton'));
//				if ($count>0) {
//					$content.=$cgi->br().'&bull; '._tl('movefilestext');
//					$content.=$cgi->input({-name=>'newname',-size=>30}).$cgi->submit(-name=>'rename',-value=>_tl('movefilesbutton'),-onclick=>'return window.confirm("'._tl('movefilesconfirm').'");');
//					$content.=$cgi->br().'&bull; '.$cgi->submit(-name=>'delete', -value=>_tl('deletefilesbutton'), -onclick=>'return window.confirm("'._tl('deletefilesconfirm').'");')
//						._tl('deletefilestext'); 
//				}
//				$content .= $cgi->hr()
//						.'&bull; '
//						._tl('changefilepermissions')
//						.(defined $PERM_USER 
//							? _tl('user')
//								.$cgi->checkbox_group(-name=>'fp_user', -values=>$PERM_USER,
//									-labels=>{'r'=>_tl('readable'), 'w'=>_tl('writeable'), 'x'=>_tl('executable'), 's'=>_tl('setuid')})
//							: ''
//						  )
//						.(defined $PERM_GROUP
//							? _tl('group')
//								.$cgi->checkbox_group(-name=>'fp_group', -values=>$PERM_GROUP,
//									-labels=>{'r'=>_tl('readable'), 'w'=>_tl('writeable'), 'x'=>_tl('executable'), 's'=>_tl('setgid')})
//							: ''
//						 )
//						.(defined $PERM_OTHERS
//							? _tl('others')
//								.$cgi->checkbox_group(-name=>'fp_others', -values=>$PERM_OTHERS,
//									-labels=>{'r'=>_tl('readable'), 'w'=>_tl('writeable'), 'x'=>_tl('executable'), 't'=>_tl('sticky')})
//							: ''
//						 )
//						. '; '. $cgi->popup_menu(-name=>'fp_type',-values=>['a','s','r'], -labels=>{ 'a'=>_tl('add'), 's'=>_tl('set'), 'r'=>_tl('remove')})
//						.($ALLOW_CHANGEPERMRECURSIVE ? '; ' .$cgi->checkbox_group(-name=>'fp_recursive', -value=>['recursive'], 
//								-labels=>{'recursive'=>_tl('recursive')}) : '')
//						. '; '.$cgi->submit(-name=>'changeperm',-value=>_tl('changepermissions'),
//								-onclick=>'return window.confirm("'._tl('changepermconfirm').'");')
//						. $cgi->br().'&nbsp;&nbsp;'._tl('changepermlegend')
//					if $ALLOW_CHANGEPERM;
//
//				if ($ALLOW_ZIP_UPLOAD || $ALLOW_ZIP_DOWNLOAD) {
//					$content.=$cgi->hr();
//					$content.='&bull; '.$cgi->submit(-name=>'zip',-value=>_tl('zipdownloadbutton'))._tl('zipdownloadtext').$cgi->br() if $ALLOW_ZIP_DOWNLOAD && $count>0;
//					$content.='&bull; '._tl('zipuploadtext').$cgi->filefield(-name=>'zipfile_upload', -multiple=>'multiple')
//							. $cgi->submit(-name=>'uncompress', -value=>_tl('zipuploadbutton'),-onclick=>'return window.confirm("'._tl('zipuploadconfirm').'");')
//						if $ALLOW_ZIP_UPLOAD;
//				}
//			}
//			$content.=$cgi->end_form() if $ALLOW_FILE_MANAGEMENT;
//			$content .= $cgi->hr().$cgi->start_multipart_form(-onsubmit=>'return window.confirm("'._tl('confirm').'");')
//				.$cgi->hidden(-name=>'upload',-value=>1)
//				.$cgi->span({-id=>'file_upload'},'&bull; '._tl('fileuploadtext').$cgi->filefield(-name=>'file_upload', -multiple=>'multiple' ))
//				.$cgi->span({-id=>'moreuploads'},"").$cgi->submit(-name=>'filesubmit',-value=>_tl('fileuploadbutton'),-onclick=>'return window.confirm("'._tl('fileuploadconfirm').'");')
//				.' '
//				.$cgi->a({-onclick=>'javascript:document.getElementById("moreuploads").innerHTML=document.getElementById("moreuploads").innerHTML+"<br/>"+document.getElementById("file_upload").innerHTML',-href=>'#'},_tl('fileuploadmore'))
//				.' ('.($CGI::POST_MAX / 1048576).' MB max)'
//				.$cgi->end_form() if $ALLOW_POST_UPLOADS && ($IGNOREFILEPERMISSIONS || -w $fn);
//
//		}
//
//		$content .= $cgi->hr().$SIGNATURE if defined $SIGNATURE;
//		$content .= $cgi->end_html();
//		printHeaderAndContent('200 OK','text/html',$content,'Cache-Control: no-cache, no-store' );
//	} elsif (-e $fn && !-r $fn) {
//		printHeaderAndContent('403 Forbidden','text/plain', '403 Forbidden');
//	} elsif (-e $fn) {
//		debug("_GET: DOWNLOAD");
//		printFileHeader($fn);
//		if (open(F,"<$fn")) {
//			binmode(STDOUT);
//			while (read(F,my $buffer, $BUFSIZE)>0) {
//				print $buffer;
//			}
//			close(F);
//		}
//	} else {
//		debug("GET: $fn NOT FOUND!");
//		printHeaderAndContent('404 Not Found','text/plain','404 - FILE NOT FOUND');
//	}
//	
//}
//sub _HEAD {
//	if (-d $PATH_TRANSLATED) {
//		debug("_HEAD: $PATH_TRANSLATED is a folder!");
//		printHeaderAndContent('200 OK','httpd/unix-directory');
//	} elsif (-e $PATH_TRANSLATED) {
//		debug("_HEAD: $PATH_TRANSLATED exists!");
//		printFileHeader($PATH_TRANSLATED);
//	} else {
//		debug("_HEAD: $PATH_TRANSLATED does not exists!");
//		printHeaderAndContent('404 Not Found');
//	}
//}
//sub _POST {
//	debug("_POST: $PATH_TRANSLATED");
//
//	if (!$cgi->param('file_upload') && $cgi->cgi_error) {
//		printHeaderAndContent($cgi->cgi_error,undef,$cgi->cgi_error);	
//		exit 0;
//	}
//
//	my($msg,$msgparam,$errmsg);
//	my $redirtarget = $REQUEST_URI;
//	$redirtarget =~s/\?.*$//; # remove query
//	
//	if ($ALLOW_FILE_MANAGEMENT && ($cgi->param('delete')||$cgi->param('rename')||$cgi->param('mkcol')||$cgi->param('changeperm'))) {
//		debug("_POST: file management ".join(",",$cgi->param('file')));
//		if ($cgi->param('delete')) {
//			if ($cgi->param('file')) {
//				my $count = 0;
//				foreach my $file ($cgi->param('file')) {
//					debug("_POST: delete $PATH_TRANSLATED.$file");
//					if ($ENABLE_TRASH) {
//						moveToTrash($PATH_TRANSLATED.$file);
//					} else {
//						$count += deltree($PATH_TRANSLATED.$file, \my @err);
//					}
//					logger("DELETE($PATH_TRANSLATED) via POST");
//				}
//				if ($count>0) {
//					$msg= ($count>1)?'deletedmulti':'deletedsingle';
//					$msgparam="p1=$count";
//				} else {
//					$errmsg='deleteerr'; 
//				}
//			} else {
//				$errmsg='deletenothingerr';
//			}
//		} elsif ($cgi->param('rename')) {
//			if ($cgi->param('file')) {
//				if ($cgi->param('newname')) {
//					my @files = $cgi->param('file');
//					if (($#files > 0)&&(! -d $PATH_TRANSLATED.$cgi->param('newname'))) {
//						printHeaderAndContent('403 Forbidden','text/plain','403 Forbidden');
//						exit;
//					} else {
//						$msg='rename';
//						$msgparam = 'p1='.$cgi->escape(join(', ',@files))
//						          . ';p2='.$cgi->escape($cgi->param('newname'));
//						foreach my $file (@files) {
//							if (rmove($PATH_TRANSLATED.$file, $PATH_TRANSLATED.$cgi->param('newname'))) {
//								logger("MOVE($PATH_TRANSLATED,$PATH_TRANSLATED".$cgi->param('newname').") via POST");
//							} else {
//								$errmsg='renameerr';
//							}
//						}
//					}
//				} else {
//					$errmsg='renamenotargeterr';
//				}
//			} else {
//				$errmsg='renamenothingerr';
//			}
//		} elsif ($cgi->param('mkcol'))  {
//			if ($cgi->param('colname')) {
//				$msgparam="p1=".$cgi->escape($cgi->param('colname'));
//				if (mkdir($PATH_TRANSLATED.$cgi->param('colname'))) {
//					logger("MKCOL($PATH_TRANSLATED".$cgi->param('colname').") via POST");
//					$msg='foldercreated';
//				} else {
//					$errmsg='foldererr'; 
//					$msgparam.=';p2='.$cgi->escape(_tl($!));
//				}
//			} else {
//				$errmsg='foldernothingerr';
//			}
//		} elsif ($cgi->param('changeperm')) {
//			if ($cgi->param('file')) {
//				my $mode = 0000;
//				foreach my $userperm ($cgi->param('fp_user')) {
//					$mode = $mode | 0400 if $userperm eq 'r' && grep(/^r$/,@{$PERM_USER}) == 1;
//					$mode = $mode | 0200 if $userperm eq 'w' && grep(/^w$/,@{$PERM_USER}) == 1;
//					$mode = $mode | 0100 if $userperm eq 'x' && grep(/^x$/,@{$PERM_USER}) == 1;
//					$mode = $mode | 04000 if $userperm eq 's' && grep(/^s$/,@{$PERM_USER}) == 1;
//				}
//				foreach my $grpperm ($cgi->param('fp_group')) {
//					$mode = $mode | 0040 if $grpperm eq 'r' && grep(/^r$/,@{$PERM_GROUP}) == 1;
//					$mode = $mode | 0020 if $grpperm eq 'w' && grep(/^w$/,@{$PERM_GROUP}) == 1;
//					$mode = $mode | 0010 if $grpperm eq 'x' && grep(/^x$/,@{$PERM_GROUP}) == 1;
//					$mode = $mode | 02000 if $grpperm eq 's' && grep(/^s$/,@{$PERM_GROUP}) == 1;
//				}
//				foreach my $operm ($cgi->param('fp_others')) {
//					$mode = $mode | 0004 if $operm eq 'r' && grep(/^r$/,@{$PERM_OTHERS}) == 1;
//					$mode = $mode | 0002 if $operm eq 'w' && grep(/^w$/,@{$PERM_OTHERS}) == 1;
//					$mode = $mode | 0001 if $operm eq 'x' && grep(/^x$/,@{$PERM_OTHERS}) == 1;
//					$mode = $mode | 01000 if $operm eq 't' && grep(/^t$/,@{$PERM_OTHERS}) == 1;
//				}
//
//				$msg='changeperm';
//				$msgparam=sprintf("p1=%04o",$mode);
//				foreach my $file ($cgi->param('file')) {
//					changeFilePermissions($PATH_TRANSLATED.$file, $mode, $cgi->param('fp_type'), $ALLOW_CHANGEPERMRECURSIVE && $cgi->param('fp_recursive'));
//				}
//			} else {
//				$errmsg='chpermnothingerr';
//			}
//		}
//		print $cgi->redirect($redirtarget.createMsgQuery($msg,$msgparam, $errmsg, $msgparam));
//	} elsif ($ALLOW_POST_UPLOADS && -d $PATH_TRANSLATED && defined $cgi->param('file_upload')) {
//		my @filelist;
//		foreach my $filename ($cgi->param('file_upload')) {
//			next if $filename eq "";
//			my $rfn= $filename;
//			$rfn=~s/\\/\//g; # fix M$ Windows backslashes
//			my $destination = $PATH_TRANSLATED.basename($rfn);
//			debug("_POST: save $filename to $destination.");
//			push(@filelist, basename($rfn));
//			if (open(O,">$destination")) {
//				while (read($filename,my $buffer,$BUFSIZE)>0) {
//					print O $buffer;
//				}
//				close(O);
//			} else {
//				printHeaderAndContent('403 Forbidden','text/plain','403 Forbidden');
//				last;
//			}
//		}
//		if ($#filelist>-1) {
//			$msg=($#filelist>0)?'uploadmulti':'uploadsingle';
//			$msgparam='p1='.($#filelist+1).';p2='.$cgi->escape(substr(join(', ',@filelist), 0, 150));
//		} else {
//			$errmsg='uploadnothingerr';
//		}
//		print $cgi->redirect($redirtarget.createMsgQuery($msg,$msgparam,$errmsg,$msgparam));
//	} elsif ($ALLOW_ZIP_DOWNLOAD && defined $cgi->param('zip')) {
//		my $zip =  Archive::Zip->new();		
//		foreach my $file ($cgi->param('file')) {
//			if (-d $PATH_TRANSLATED.$file) {
//				$zip->addTree($PATH_TRANSLATED.$file, $file);
//			} else {
//				$zip->addFile($PATH_TRANSLATED.$file, $file);
//			}
//		}
//		my $zfn = basename($PATH_TRANSLATED).'.zip';
//		$zfn=~s/ /_/;
//		print $cgi->header(-status=>'200 OK', -type=>'application/zip',-Content_disposition=>'attachment; filename='.$zfn);
//		$zip->writeToFileHandle(\*STDOUT,0);
//	} elsif ($ALLOW_ZIP_UPLOAD && defined $cgi->param('uncompress')) {
//		my @zipfiles;
//		foreach my $fh ($cgi->param('zipfile_upload')) {
//			my $rfn= $fh;
//			$rfn=~s/\\/\//g; # fix M$ Windows backslashes
//			$rfn=basename($rfn);
//			if (open(F,">$PATH_TRANSLATED$rfn")) {
//				push @zipfiles, $rfn;
//				print F $_ while (<$fh>);
//				close(F);
//				my $zip = Archive::Zip->new();
//				my $status = $zip->read($PATH_TRANSLATED.$rfn);
//				if ($status eq $zip->AZ_OK) {
//					$zip->extractTree(undef, $PATH_TRANSLATED);
//					unlink($PATH_TRANSLATED.$rfn);
//				}
//			}
//		}
//		if ($#zipfiles>-1) {
//			$msg=($#zipfiles>0)?'zipuploadmulti':'zipuploadsingle';
//			$msgparam='p1='.($#zipfiles+1).';p2='.$cgi->escape(substr(join(', ',@zipfiles), 0, 150));
//		} else {
//			$errmsg='zipuploadnothingerr';
//		}
//		print $cgi->redirect($redirtarget.createMsgQuery($msg,$msgparam,$errmsg,$msgparam));
//		
//	} elsif ($ENABLE_CALDAV_SCHEDULE && -d $PATH_TRANSLATED) {
//		## NOT IMPLEMENTED YET
//	} else {
//		debug("_POST: forbidden POST to $PATH_TRANSLATED");
//		printHeaderAndContent('403 Forbidden','text/plain','403 Forbidden');
//	}
//}
//sub _OPTIONS {
//	debug("_OPTIONS: $PATH_TRANSLATED");
//	my $methods;
//	my $status = '200 OK';
//	my $type;
//	if (-e $PATH_TRANSLATED) {
//		$type = -d $PATH_TRANSLATED ? 'httpd/unix-directory' : getMIMEType($PATH_TRANSLATED);
//		$methods = join(', ', @{getSupportedMethods($PATH_TRANSLATED)});
//	} else {
//		$status = '404 Not Found';
//		$type = 'text/plain';
//	}
//		
//	my $header =$cgi->header(-status=>$status ,-type=>$type, -Content_length=>0);
//	$header="DASL: <DAV:basicsearch>\r\n$header" if $ENABLE_SEARCH;
//	$header="MS-Author-Via: DAV\r\nDAV: $DAV\r\nAllow: $methods\r\nPublic: $methods\r\nDocumentManagementServer: Properties Schema\r\n$header" if (defined $methods); 
//
//	print $header;
//}
//sub _TRACE {
//	my $status = '200 OK';
//	my $content = join("",<>);
//	my $type = 'message/http';
//	my $via = $cgi->http('Via') ;
//	my $addheader = "Via: $ENV{SERVER_NAME}:$ENV{SERVER_PORT}".(defined $via?", $via":"");
//
//	printHeaderAndContent($status, $type, $content, $addheader);
//}
//sub _GETLIB {
//	my $fn = $PATH_TRANSLATED;
//	my $status='200 OK';
//	my $type=undef;
//	my $content="";
//	my $addheader="";
//	if (!-e $fn) {
//		$status='404 Not Found';
//		$type='text/plain';
//	} else {
//		my $su = $ENV{SCRIPT_URI};
//		$su=~s/\/[^\/]+$/\// if !-d $fn;
//		$addheader="MS-Doclib: $su";
//	}
//	printHeaderAndContent($status,$type,$content,$addheader);
//}
//
//sub _PROPFIND {
//	my $fn = $PATH_TRANSLATED;
//	my $status='207 Multi-Status';
//	my $type ='text/xml';
//	my $noroot = 0;
//	my $depth = defined $cgi->http('Depth')? $cgi->http('Depth') : -1;
//	$noroot=1 if $depth =~ s/,noroot//;
//	$depth=-1 if $depth =~ /infinity/i;
//	$depth = 0 if $depth == -1 && !$ALLOW_INFINITE_PROPFIND;
//
//
//	my $xml = join("",<>);
//	$xml=qq@<?xml version="1.0" encoding="$CHARSET" ?>\n<D:propfind xmlns:D="DAV:"><D:allprop/></D:propfind>@ 
//		if !defined $xml || $xml=~/^\s*$/;
//
//	my $xmldata = "";
//	eval { $xmldata = simpleXMLParser($xml); };
//	if ($@) {
//		debug("_PROPFIND: invalid XML request: $@");
//		printHeaderAndContent('400 Bad Request');
//		return;
//	}
//
//	my $ru = $REQUEST_URI;
//	$ru=~s/ /%20/g;
//	debug("_PROPFIND: depth=$depth, fn=$fn, ru=$ru");
//
//	my @resps = ();
//
//	## ACL, CalDAV, CardDAV, ...:
//	if ( defined $PRINCIPAL_COLLECTION_SET && length($PRINCIPAL_COLLECTION_SET)>1 && $ru =~ /\Q$PRINCIPAL_COLLECTION_SET\E$/) { 
//		$fn =~ s/\Q$PRINCIPAL_COLLECTION_SET\E$//;
//		$depth=0;
//	} elsif (defined $CURRENT_USER_PRINCIPAL && length($CURRENT_USER_PRINCIPAL)>1 && $ru =~ /\Q$CURRENT_USER_PRINCIPAL\E\/?$/) {
//		$fn=~s/\Q$CURRENT_USER_PRINCIPAL\E\/?$//;
//		$depth=0;
//	}
//
//	if (is_hidden($fn)) {
//		# do nothing
//	} elsif (-e $fn) {
//		my ($props, $all, $noval) =  handlePropFindElement($xmldata);
//		if (defined $props) {
//			readDirRecursive($fn, $ru, \@resps, $props, $all, $noval, $depth, $noroot);
//		} else {
//			$status='400 Bad Request';
//			$type='text/plain';
//		}
//	} else {
//		$status='404 Not Found';
//		$type='text/plain';
//	}
//	my $content = ($#resps>-1) ? createXML({ 'multistatus' => { 'response'=>\@resps} }) : "" ;
//	
//	debug("_PROPFIND: status=$status, type=$type");
//	debug("_PROPFIND: REQUEST:\n$xml\nEND-REQUEST");
//	debug("_PROPFIND: RESPONSE:\n$content\nEND-RESPONSE");
//	printHeaderAndContent($status,$type,$content);
//	
//}
//sub _PROPPATCH {
//	debug("_PROPPATCH: $PATH_TRANSLATED");
//	my $fn = $PATH_TRANSLATED;
//	my $status = '403 Forbidden';
//	my $type = 'text/plain';
//	my $content = "";
//	if (-e $fn && !isAllowed($fn)) {
//		$status = '423 Locked';
//	} elsif (-e $fn) {
//		my $xml = join("",<>);
//
//
//		debug("_PROPPATCH: REQUEST: $xml");
//		my $dataRef;
//		eval { $dataRef = simpleXMLParser($xml) };	
//		if ($@) {
//			debug("_PROPPATCH: invalid XML request: $@");
//			printHeaderAndContent('400 Bad Request');
//			return;
//		}
//		my @resps = ();
//		my %resp_200 = ();
//		my %resp_403 = ();
//
//		handlePropertyRequest($xml, $dataRef, \%resp_200, \%resp_403);
//		
//		push @resps, \%resp_200 if defined $resp_200{href};
//		push @resps, \%resp_403 if defined $resp_403{href};
//		$status='207 Multi-Status';
//		$type='text/xml';
//		$content = createXML( { multistatus => { response => \@resps} });
//	} else {
//		$status='404 Not Found';
//	}
//	debug("_PROPPATCH: RESPONSE: $content");
//	printHeaderAndContent($status, $type, $content);
//}
//
//sub _PUT {
//	my $status='204 No Content';
//	my $type = 'text/plain';
//	my $content = "";
//	my $buffer;
//
//	debug("_PUT $PATH_TRANSLATED; dirname=".dirname($PATH_TRANSLATED));
//
//	if (defined $cgi->http('Content-Range'))  {
//		$status='501 Not Implemented';
//	} elsif (-d dirname($PATH_TRANSLATED) && !-w dirname($PATH_TRANSLATED)) {
//		$status='403 Forbidden';
//	} elsif (preConditionFailed($PATH_TRANSLATED)) {
//		$status='412 Precondition Failed';
//	} elsif (!isAllowed($PATH_TRANSLATED)) {
//		$status='423 Locked';
//	#} elsif (defined $ENV{HTTP_EXPECT} && $ENV{HTTP_EXPECT} =~ /100-continue/) {
//	#	$status='417 Expectation Failed';
//	} elsif (-d dirname($PATH_TRANSLATED)) {
//		if (! -e $PATH_TRANSLATED) {
//			debug("_PUT: created...");
//			$status='201 Created';
//			$type='text/html';
//			$content = qq@<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">\n<html><head><title>201 Created</title></head>@
//				 . qq@<<body><h1>Created</h1><p>Resource $ENV{'QUERY_STRING'} has been created.</p></body></html>\n@;
//		}
//		if (open(my $f,">$PATH_TRANSLATED")) {
//			binmode STDIN;
//			binmode $f;
//			my $maxread = 0;
//			while (my $read = read(STDIN, $buffer, $BUFSIZE)>0) {
//				print $f $buffer;
//				$maxread+=$read;
//			}
//			close($f);
//			inheritLock();
//			if (exists $ENV{CONTENT_LENGTH} && $maxread != $ENV{CONTENT_LENGTH}) {
//				debug("_PUT: ERROR: maxread=$maxread, content-length: $ENV{CONTENT_LENGTH}");
//				#$status='400';
//			}
//
//
//			logger("PUT($PATH_TRANSLATED)");
//		} else {
//			$status='403 Forbidden';
//			$content="";
//			$type='text/plain';
//		}
//	} else {
//		$status='409 Conflict';
//	}
//	printHeaderAndContent($status,$type,$content);
//}
//sub _COPY {
//	my $status = '201 Created';
//	my $depth = $cgi->http('Depth');
//	my $host = $cgi->http('Host');
//	my $destination = $cgi->http('Destination');
//	my $overwrite = defined $cgi->http('Overwrite')?$cgi->http('Overwrite') : "T";
//	$destination=~s@^https?://([^\@]+\@)?\Q$host\E$VIRTUAL_BASE@@;
//	$destination=uri_unescape($destination);
//	$destination=uri_unescape($destination);
//	$destination=$DOCUMENT_ROOT.$destination;
//
//	debug("_COPY: $PATH_TRANSLATED => $destination");
//
//	if ( (!defined $destination) || ($destination eq "") || ($PATH_TRANSLATED eq $destination) ) {
//		$status = '403 Forbidden';
//	} elsif ( -e $destination && $overwrite eq "F") {
//		$status = '412 Precondition Failed';
//	} elsif ( ! -d dirname($destination)) {
//		$status = "409 Conflict - $destination";
//	} elsif ( !isAllowed($destination,-d $PATH_TRANSLATED) ) {
//		$status = '423 Locked';
//	} elsif ( -d $PATH_TRANSLATED && $depth == 0 ) {
//		if (-e $destination) {
//			$status = '204 No Content' ;
//		} else {
//			if (mkdir $destination) {
//				inheritLock($destination);
//			} else {
//				$status = '403 Forbidden';
//			}
//		}
//	} else {
//		$status = '204 No Content' if -e $destination;
//		if (rcopy($PATH_TRANSLATED, $destination)) {
//			inheritLock($destination,1);
//			logger("COPY($PATH_TRANSLATED, $destination)");
//		} else {
//			$status = '403 Forbidden - copy failed';
//		}
//	}
//
//	printHeaderAndContent($status);
//}
//sub _MOVE {
//	my $status = '201 Created';
//	my $host = $cgi->http('Host');
//	my $destination = $cgi->http('Destination');
//	my $overwrite = defined $cgi->http('Overwrite')?$cgi->http('Overwrite') : "T";
//	debug("_MOVE: $PATH_TRANSLATED => $destination");
//	$destination=~s@^https?://([^\@]+\@)?\Q$host\E$VIRTUAL_BASE@@;
//	$destination=uri_unescape($destination);
//	$destination=uri_unescape($destination);
//	$destination=$DOCUMENT_ROOT.$destination;
//
//	if ( (!defined $destination) || ($destination eq "") || ($PATH_TRANSLATED eq $destination) ) {
//		$status = '403 Forbidden';
//	} elsif ( -e $destination && $overwrite eq "F") {
//		$status = '412 Precondition Failed';
//	} elsif ( ! -d dirname($destination)) {
//		$status = "409 Conflict - ".dirname($destination);
//	} elsif (!isAllowed($PATH_TRANSLATED,-d $PATH_TRANSLATED) || !isAllowed($destination, -d $destination)) {
//		$status = '423 Locked';
//	} else {
//		unlink($destination) if -f $destination;
//		$status = '204 No Content' if -e $destination;
//		if (rmove($PATH_TRANSLATED, $destination)) {
//			db_moveProperties($PATH_TRANSLATED, $destination);
//			db_delete($PATH_TRANSLATED);
//			inheritLock($destination,1);
//			logger("MOVE($PATH_TRANSLATED, $destination)");
//		} else {
//			$status = '403 Forbidden';
//		}
//	}
//	debug("_MOVE: status=$status");
//	printHeaderAndContent($status);
//}
//sub _DELETE {
//	my $status = '204 No Content';
//	# check all files are writeable and than remove it
//
//	debug("_DELETE: $PATH_TRANSLATED");
//
//	my @resps = ();
//	if (!-e $PATH_TRANSLATED) {
//		$status='404 Not Found';
//	} elsif (($REQUEST_URI=~/\#/ && $PATH_TRANSLATED!~/\#/) || (defined $ENV{QUERY_STRING} && $ENV{QUERY_STRING} ne "")) {
//		$status='400 Bad Request';
//	} elsif (!isAllowed($PATH_TRANSLATED)) {
//		$status='423 Locked';
//	} else {
//		if ($ENABLE_TRASH) {
//			$status='404 Forbidden' unless moveToTrash($PATH_TRANSLATED);
//		} else {
//			deltree($PATH_TRANSLATED, \my @err);
//			logger("DELETE($PATH_TRANSLATED)");
//			for my $diag (@err) {
//				my ($file, $message) = each %$diag;
//				push @resps, { href=>$file, status=>"403 Forbidden - $message" };
//			}
//			$status = '207 Multi-Status' if $#resps>-1;
//		}
//	}
//		
//	my $content = $#resps>-1 ? createXML({ 'multistatus' => { 'response'=>\@resps} }) : "";
//	printHeaderAndContent($status, $#resps>-1 ? 'text/xml' : undef, $content);
//	debug("_DELETE RESPONSE (status=$status): $content");
//}
//sub _MKCALENDAR {
//	_MKCOL(1);
//}
//sub _MKCOL {
//	my ($cal) = @_;
//	my $status='201 Created';
//	my ($type,$content);
//	debug("_MKCOL: $PATH_TRANSLATED");
//	my $body = join("",<>);
//	my $dataRef;
//	if ($body ne "") {
//		debug("_MKCOL: yepp #1".$cgi->content_type());
//		# maybe extended mkcol (RFC5689)
//		if ($cgi->content_type() =~/\/xml/) {
//			eval { $dataRef = simpleXMLParser($body) };	
//			if ($@) {
//				debug("_MKCOL: invalid XML request: $@");
//				printHeaderAndContent('400 Bad Request');
//				return;
//			}
//			if (ref($$dataRef{'{DAV:}set'}) !~ /(ARRAY|HASH)/) {
//				printHeaderAndContent('400 Bad Request');
//				return;
//			}
//		} else {
//			$status = '415 Unsupported Media Type';
//			printHeaderAndContent($status, $type, $content);
//			return;
//		}
//	} 
//	if (-e $PATH_TRANSLATED) {
//		$status = '405 Method Not Allowed';
//	} elsif (!-e dirname($PATH_TRANSLATED)) {
//		$status = '409 Conflict';
//	} elsif (!-w dirname($PATH_TRANSLATED)) {
//		$status = '403 Forbidden';
//	} elsif (!isAllowed($PATH_TRANSLATED)) {
//		debug("_MKCOL: not allowed!");
//		$status = '423 Locked';
//	} elsif (-e $PATH_TRANSLATED) {
//		$status = '409 Conflict';
//	} elsif (-d dirname($PATH_TRANSLATED)) {
//		debug("_MKCOL: create $PATH_TRANSLATED");
//
//
//		if (mkdir($PATH_TRANSLATED)) {
//			my (%resp_200, %resp_403);
//			handlePropertyRequest($body, $dataRef, \%resp_200, \%resp_403);
//			## ignore errors from property request
//			inheritLock();
//			logger("MKCOL($PATH_TRANSLATED)");
//		} else {
//			$status = '403 Forbidden'; 
//		}
//	} else {	
//		debug("_MKCOL: parent direcory does not exists");
//		$status = '409 Conflict';
//	}
//	printHeaderAndContent($status, $type, $content);
//}
//sub _LOCK {
//	debug("_LOCK: $PATH_TRANSLATED");
//	
//	my $fn = $PATH_TRANSLATED;
//	my $ru = $REQUEST_URI;
//	my $depth = defined $cgi->http('Depth')?$cgi->http('Depth'):'infinity';
//	my $timeout = $cgi->http('Timeout');
//	my $status = '200 OK';
//	my $type = 'application/xml';
//	my $content = "";
//	my $addheader = undef;
//
//	my $xml = join('',<>);
//	my $xmldata = $xml ne "" ? simpleXMLParser($xml) : { };
//
//	my $token ="opaquelocktoken:".getuuid($fn);
//
//	if (!-e $fn && !-e dirname($fn)) {
//		$status='409 Conflict';
//		$type='text/plain';
//	} elsif (!isLockable($fn, $xmldata)) {
//		debug("_LOCK: not lockable ... but...");
//		if (isAllowed($fn)) {
//			$status='200 OK';
//			lockResource($fn, $ru, $xmldata, $depth, $timeout, $token);
//			$content = createXML({prop=>{lockdiscovery => getLockDiscovery($fn)}});	
//		} else {
//			$status='423 Locked';
//			$type='text/plain';
//		}
//	} elsif (!-e $fn) {
//		if (open(F,">$fn")) {
//			print F '';
//			close(F);
//			my $resp = lockResource($fn, $ru, $xmldata, $depth, $timeout,$token);
//			if (defined $$resp{multistatus}) {
//				$status = '207 Multi-Status'; 
//			} else {
//				$addheader="Lock-Token: $token";
//				$status='201 Created';
//			}
//			$content=createXML($resp);
//		} else {
//			$status='403 Forbidden';
//			$type='text/plain';
//		}
//	} else {
//		my $resp = lockResource($fn, $ru, $xmldata, $depth, $timeout, $token);
//		$addheader="Lock-Token: $token";
//		$content=createXML($resp);
//		$status = '207 Multi-Status' if defined $$resp{multistatus};
//	}
//	debug("_LOCK: REQUEST: $xml");
//	debug("_LOCK: RESPONSE: $content");
//	debug("_LOCK: status: $status, type=$type");
//	printHeaderAndContent($status,$type,$content,$addheader);	
//}
//sub _UNLOCK {
//	my $status = '403 Forbidden';
//	my $token = $cgi->http('Lock-Token');
//
//	$token=~s/[\<\>]//g;
//	debug("_UNLOCK: $PATH_TRANSLATED (token=$token)");
//	
//	if (!defined $token) {
//		$status = '400 Bad Request';
//	} elsif (isLocked($PATH_TRANSLATED)) {
//		if (unlockResource($PATH_TRANSLATED, $token)) {
//			$status = '204 No Content';
//		} else {
//			$status = '423 Locked';
//		}
//	} else {
//		$status = '409 Conflict';
//	}
//	printHeaderAndContent($status);
//}
//sub _ACL {
//	my $fn = $PATH_TRANSLATED;
//	my $status = '200 OK';
//	my $content = "";
//	my $type;
//	my %error;
//	debug("_ACL($fn)");
//	my $xml = join("",<>);
//	my $xmldata = "";
//	eval { $xmldata = simpleXMLParser($xml,1); };
//	if ($@) {
//		debug("_ACL: invalid XML request: $@");
//		$status='400 Bad Request';
//		$type='text/plain';
//		$content='400 Bad Request';
//	} elsif (!-e $fn) {
//		$status = '404 Not Found';
//		$type = 'text/plain';
//		$content='404 Not Found';
//	} elsif (!isAllowed($fn)) {
//		$status = '423 Locked';
//		$type = 'text/plain';
//		$content='423 Locked';
//	} elsif (!exists $$xmldata{'{DAV:}acl'}) {
//		$status='400 Bad Request';
//		$type='text/plain';
//		$content='400 Bad Request';
//	} else {
//		my @ace;
//		if (ref($$xmldata{'{DAV:}acl'}{'{DAV:}ace'}) eq 'HASH') {
//			push @ace, $$xmldata{'{DAV:}acl'}{'{DAV:}ace'};
//		} elsif (ref($$xmldata{'{DAV:}acl'}{'{DAV:}ace'}) eq 'ARRAY') {
//			push @ace, @{$$xmldata{'{DAV:}acl'}{'{DAV:}ace'}};
//		} else {
//			printHeaderAndContent('400 Bad Request');
//			return;
//		}
//		foreach my $ace (@ace) {
//			my $p;
//			my ($user,$group,$other) = (0,0,0);
//			if (defined ($p = $$ace{'{DAV:}principal'})) {
//				if (exists $$p{'{DAV:}property'}{'{DAV:}owner'}) { 
//					$user=1;
//				} elsif (exists $$p{'{DAV:}property'}{'{DAV:}group'}) {
//					$group=1;
//				} elsif (exists $$p{'{DAV:}all'}) {
//					$other=1;
//				} else {
//					printHeaderAndContent('400 Bad Request');
//					return;
//				}
//			} else {
//				printHeaderAndContent('400 Bad Request');
//				return;
//			}
//			my ($read,$write) = (0,0);
//			if (exists $$ace{'{DAV:}grant'}) {
//				$read=1 if exists $$ace{'{DAV:}grant'}{'{DAV:}privilege'}{'{DAV:}read'};
//				$write=1 if exists $$ace{'{DAV:}grant'}{'{DAV:}privilege'}{'{DAV:}write'};
//			} elsif (exists $$ace{'{DAV:}deny'}) {
//				$read=-1 if exists $$ace{'{DAV:}deny'}{'{DAV:}privilege'}{'{DAV:}read'};
//				$write=-1 if exists $$ace{'{DAV:}deny'}{'{DAV:}privilege'}{'{DAV:}write'};
//			} else {
//				printHeaderAndContent('400 Bad Request');
//				return;
//				
//			}
//			if ($read==0 && $write==0) {
//				printHeaderAndContent('400 Bad Request');
//				return;
//			}
//			my @stat = stat($fn);
//			my $mode = $stat[2];
//			$mode = $mode & 07777;
//			
//			my $newperm = $mode;
//			if ($read!=0) {
//				my $mask = $user? 0400 : $group ? 0040 : 0004;
//				$newperm = ($read>0) ? $newperm | $mask : $newperm & ~$mask
//			} 
//			if ($write!=0) {
//				my $mask = $user? 0200 : $group ? 0020 : 0002;
//				$newperm = ($write>0) ? $newperm | $mask : $newperm & ~$mask;
//			}
//			debug("_ACL: old perm=".sprintf('%4o',$mode).", new perm=".sprintf('%4o',$newperm));
//			if (!chmod($newperm, $fn)) {
//				$status='403 Forbidden';
//				$type='text/plain';
//				$content='403 Forbidden';
//			}
//
//		}
//		
//	}
//	printHeaderAndContent($status, $type, $content);
//}
//sub _REPORT {
//	my $fn = $PATH_TRANSLATED;
//	my $ru = $REQUEST_URI;
//	my $depth = defined $cgi->http('Depth')? $cgi->http('Depth') : 0;
//	$depth=-1 if $depth =~ /infinity/i;
//	debug("_REPORT($fn,$ru)");
//	my $status = '200 OK';
//	my $content = "";
//	my $type;
//	my %error;
//	my $xml = join("",<>);
//	my $xmldata = "";
//	eval { $xmldata = simpleXMLParser($xml,1); };
//	if ($@) {
//		debug("_REPORT: invalid XML request: $@");
//		debug("_REPORT: xml-request=$xml");
//		$status='400 Bad Request';
//		$type='text/plain';
//		$content='400 Bad Request';
//	} elsif (!-e $fn) {
//		$status = '404 Not Found';
//		$type = 'text/plain';
//		$content='404 Not Found';
//	} else {
//		# MUST CalDAV: DAV:expand-property
//		$status='207 Multi-Status';
//		$type='application/xml';
//		my @resps;
//		my @hrefs;
//		my $rn;
//		my @reports = keys %{$xmldata};
//		debug("_REPORT: report=".$reports[0]) if $#reports >-1;
//		if (defined $$xmldata{'{DAV:}acl-principal-prop-set'}) {
//			my @props;
//			handlePropElement($$xmldata{'{DAV:}acl-principal-prop-set'}{'{DAV:}prop'}, \@props);
//			push @resps, { href=>$ru, propstat=> getPropStat($fn,$ru,\@props) };
//		} elsif (defined $$xmldata{'{DAV:}principal-match'}) {
//			if ($depth!=0) {
//				printHeaderAndStatus('400 Bad Request');
//				return;
//			}
//			# response, href
//			my @props;
//			handlePropElement($$xmldata{'{DAV:}principal-match'}{'{DAV:}prop'}, \@props) if (exists $$xmldata{'{DAV:}principal-match'}{'{DAV:}prop'});
//			readDirRecursive($fn, $ru, \@resps, \@props, 0, 0, 1, 1);
//		} elsif (defined $$xmldata{'{DAV:}principal-property-search'}) {
//			if ($depth!=0) {
//				printHeaderAndStatus('400 Bad Request');
//				return;
//			}
//
//			my @props;
//			handlePropElement($$xmldata{'{DAV:}principal-property-search'}{'{DAV:}prop'}, \@props) if exists $$xmldata{'{DAV:}principal-property-search'}{'{DAV:}prop'};
//			readDirRecursive($fn, $ru, \@resps, \@props, 0, 0, 1, 1);
//			### XXX filter data
//			my @propertysearch;
//			if (ref($$xmldata{'{DAV:}principal-property-search'}{'{DAV:}property-search'}) eq 'HASH') {
//				push @propertysearch, $$xmldata{'{DAV:}principal-property-search'}{'{DAV:}property-search'};
//			} elsif (ref($$xmldata{'{DAV:}principal-property-search'}{'{DAV:}property-search'}) eq 'ARRAY') {
//				push @propertysearch, @{$$xmldata{'{DAV:}principal-property-search'}{'{DAV:}property-search'}};
//			}
//		} elsif (defined $$xmldata{'{DAV:}principal-search-property-set'}) {
//			my %resp;
//			$resp{'principal-search-property-set'} = { 
//				'principal-search-property' =>
//					[
//						{ prop => { displayname=>undef }, description => 'Full name' },
//					] 
//			};
//			$content = createXML(\%resp);
//			$status = '200 OK';
//			$type = 'text/xml';
//		} elsif (defined $$xmldata{'{urn:ietf:params:xml:ns:caldav}free-busy-query'}) {
//			($status,$type) = ('200 OK', 'text/calendar');
//			$content="BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Example Corp.//CalDAV Server//EN\r\nBEGIN:VFREEBUSY\r\nEND:VFREEBUSY\r\nEND:VCALENDAR";
//		} elsif (defined $$xmldata{'{urn:ietf:params:xml:ns:caldav}calendar-query'}) { ## missing filter
//			$rn = '{urn:ietf:params:xml:ns:caldav}calendar-query';
//			readDirBySuffix($fn, $ru, \@hrefs, 'ics', $depth);
//		} elsif (defined $$xmldata{'{urn:ietf:params:xml:ns:caldav}calendar-multiget'}) { ## OK - complete
//			$rn = '{urn:ietf:params:xml:ns:caldav}calendar-multiget';
//			if (!defined $$xmldata{$rn}{'{DAV:}href'} || !defined $$xmldata{$rn}{'{DAV:}prop'}) {
//				printHeaderAndContent('404 Bad Request');
//				return;
//			}
//			if (ref($$xmldata{$rn}{'{DAV:}href'}) eq 'ARRAY') {
//				@hrefs = @{$$xmldata{$rn}{'{DAV:}href'}};
//			} else {
//				push @hrefs,  $$xmldata{$rn}{'{DAV:}href'};
//			}
//						
//		} elsif (defined $$xmldata{'{urn:ietf:params:xml:ns:carddav}addressbook-query'}) {
//			$rn = '{urn:ietf:params:xml:ns:carddav}addressbook-query';
//			readDirBySuffix($fn, $ru, \@hrefs, 'vcf', $depth);
//		} elsif (defined $$xmldata{'{urn:ietf:params:xml:ns:carddav}addressbook-multiget'}) {
//			$rn = '{urn:ietf:params:xml:ns:carddav}addressbook-multiget';
//			if (!defined $$xmldata{$rn}{'{DAV:}href'} || !defined $$xmldata{$rn}{'{DAV:}prop'}) {
//				printHeaderAndContent('404 Bad Request');
//				return;
//			}
//			if (ref($$xmldata{$rn}{'{DAV:}href'}) eq 'ARRAY') {
//				@hrefs = @{$$xmldata{$rn}{'{DAV:}href'}};
//			} else {
//				push @hrefs,  $$xmldata{$rn}{'{DAV:}href'};
//			}
//		} else {
//			$status ='400 Bad Request';
//			$type = 'text/plain';
//			$content = '400 Bad Request';
//		}
//		if ($rn) {
//			foreach my $href (@hrefs) {
//				my(%resp_200, %resp_404);
//				$resp_200{status}='HTTP/1.1 200 OK';
//				$resp_404{status}='HTTP/1.1 404 Not Found';
//				my $nhref = $href;
//				$nhref=~s/$VIRTUAL_BASE//;
//				my $nfn.=$DOCUMENT_ROOT.$nhref;
//				debug("_REPORT: nfn=$nfn, href=$href");
//				if (!-e $nfn) {
//					push @resps, { href=>$href, status=>'HTTP/1.1 404 Not Found' };
//					next;
//				} elsif (-d $nfn) {
//					push @resps, { href=>$href, status=>'HTTP/1.1 403 Forbidden' };
//					next;
//				}
//				my @props;
//				handlePropElement($$xmldata{$rn}{'{DAV:}prop'}, \@props) if exists $$xmldata{$rn}{'{DAV:}prop'};
//				push @resps, { href=>$href, propstat=> getPropStat($nfn,$nhref,\@props) };
//			}
//			### push @resps, { } if ($#hrefs==-1);  ## empty multistatus response not supported
//		}
//		$content=createXML({multistatus => $#resps>-1 ? { response => \@resps } : undef }) if $#resps>-1;
//
//	}
//	debug("_REPORT: REQUEST: $xml");
//	debug("_REPORT: RESPONSE: $content");
//	printHeaderAndContent($status, $type, $content);
//}
//sub _SEARCH {
//	my @resps;
//	my $status = 'HTTP/1.1 207 Multistatus';
//	my $content = "";
//	my $type='application/xml';
//	my @errors;
//
//	my $xml = join("",<>);
//	my $xmldata = "";
//	eval { $xmldata = simpleXMLParser($xml,1); };
//	if ($@) {
//		debug("_SEARCH: invalid XML request: $@");
//		debug("_SEARCH: xml-request=$xml");
//		$status='400 Bad Request';
//		$type='text/plain';
//		$content='400 Bad Request';
//	} elsif (exists $$xmldata{'{DAV:}query-schema-discovery'}) {
//		debug("_SEARCH: found query-schema-discovery");
//		push @resps, { href=>$REQUEST_URI, status=>$status, 
//				'query-schema'=> { basicsearchschema=> { properties => { 
//					propdesc => [
//						{ 'any-other-property'=>undef, searchable=>undef, selectable=>undef, caseless=>undef, sortable=>undef }
//					]
//				}, operators => { 'opdesc allow-pcdata="yes"' => 
//								[ 
//									{ like => undef, 'operand-property'=>undef, 'operand-literal'=>undef },
//									{ contains => undef }
//								] 
//				}}}};
//	} elsif (exists $$xmldata{'{DAV:}searchrequest'}) {
//		foreach my $s (keys %{$$xmldata{'{DAV:}searchrequest'}}) {
//			if ($s =~ /{DAV:}basicsearch/) {
//				handleBasicSearch($$xmldata{'{DAV:}searchrequest'}{$s}, \@resps,\@errors);
//			}
//		}
//	}
//	if ($#errors>-1) {
//		$content = createXML({error=>\@errors});
//		$status='409 Conflict';
//	} elsif ($#resps > -1) {
//		$content = createXML({multistatus=>{ response=>\@resps }});
//	} else {
//		$content = createXML({multistatus=>{ response=> { href=>$REQUEST_URI, status=>'404 Not Found' }}});
//	}
//	printHeaderAndContent($status, $type, $content);
//}
//sub _BIND {
//	my ($status,$type,$content) = ('200 OK', undef, undef);
//	my $overwrite = defined $cgi->http('Overwrite')?$cgi->http('Overwrite') : "T";
//	my $xml = join("",<>);
//	my $xmldata = "";
//	my $host = $cgi->http('Host');
//	eval { $xmldata = simpleXMLParser($xml,0); };
//	if ($@) {
//		$status='400 Bad Request';
//		$type='text/plain';
//		$content='400 Bad Request';
//	} else {
//		my $segment = $$xmldata{'{DAV:}segment'};
//		my $href = $$xmldata{'{DAV:}href'};
//		$href=~s/^https?:\/\/\Q$host\E+$VIRTUAL_BASE//;
//		$href=uri_unescape(uri_unescape($href));
//		my $src = $DOCUMENT_ROOT.$href;
//		my $dst = $PATH_TRANSLATED.$segment;
//
//		my $ndst = $dst;
//		$ndst=~s /\/$//;
//
//		if (!-e $src) { 
//			$status ='404 Not Found';
//		} elsif ( -e $dst && ! -l $ndst) {
//			$status = '403 Forbidden';
//		} elsif (-e $dst && -l $ndst && $overwrite eq "F") {
//			$status = '403 Forbidden';
//		} else {
//			$status = -l $ndst ? '204 No Content' : '201 Created';
//			unlink($ndst) if -l $ndst;
//			$status = '403 Forbidden' if (!symlink($src, $dst));
//		}
//	}
//	printHeaderAndContent($status, $type, $content);
//}
//sub _UNBIND {
//	my ($status,$type,$content) = ('204 No Content', undef, undef);
//	my $xml = join("",<>);
//	my $xmldata = "";
//	eval { $xmldata = simpleXMLParser($xml,0); };
//	if ($@) {
//		$status='400 Bad Request';
//		$type='text/plain';
//		$content='400 Bad Request';
//	} else {
//		my $segment = $$xmldata{'{DAV:}segment'};
//		my $dst = $PATH_TRANSLATED.$segment;
//		if (!-e $dst ) {
//			$status = '404 Not Found';
//		} elsif (!-l $dst) {
//			$status = '403 Forbidden';
//		} elsif (!unlink($dst)) {
//			$status = '403 Forbidden';
//		}
//	}
//	printHeaderAndContent($status, $type, $content);
//}
//sub _REBIND {
//	my ($status,$type,$content) = ('200 OK', undef, undef);
//	my $overwrite = defined $cgi->http('Overwrite')?$cgi->http('Overwrite') : "T";
//	my $xml = join("",<>);
//	my $xmldata = "";
//	my $host = $cgi->http('Host');
//	eval { $xmldata = simpleXMLParser($xml,0); };
//	if ($@) {
//		$status='400 Bad Request';
//		$type='text/plain';
//		$content='400 Bad Request';
//	} else {
//		my $segment = $$xmldata{'{DAV:}segment'};
//		my $href = $$xmldata{'{DAV:}href'};
//		$href=~s/^https?:\/\/\Q$host\E+$VIRTUAL_BASE//;
//		$href=uri_unescape(uri_unescape($href));
//		my $src = $DOCUMENT_ROOT.$href;
//		my $dst = $PATH_TRANSLATED.$segment;
//
//		my $nsrc = $src; $nsrc =~ s/\/$//;
//		my $ndst = $dst; $ndst =~ s/\/$//;
//
//		if (!-e $src) {
//			$status = '404 Not Found';
//		} elsif (!-l $nsrc) { 
//			$status = '403 Forbidden';
//		} elsif (-e $dst && $overwrite ne 'T') {
//			$status = '403 Forbidden';
//		} elsif (-e $dst && !-l $ndst) {
//			$status = '403 Forbidden';
//		} else {
//			$status = -l $ndst ? '204 No Content' : '201 Created';
//			unlink($ndst) if -l $ndst;
//			if (!rename($nsrc, $ndst)) {
//				my $orig = readlink($nsrc);
//				$status = '403 Forbidden' unless symlink($orig, $dst) && unlink($nsrc);
//			}
//		}
//	}
//	printHeaderAndContent($status, $type, $content);
//}
//sub changeFilePermissions {
//	my ($fn, $mode, $type, $recurse, $visited) = @_;
//	if ($type eq 's') {
//		chmod($mode, $fn);
//	} else {
//		my @stat = stat($fn);
//		my $newmode;
//		$newmode = $stat[2] | $mode if $type eq 'a';
//		$newmode = $stat[2] ^ ($stat[2] & $mode ) if $type eq 'r';
//		chmod($newmode, $fn);
//	}
//	my $nfn = File::Spec::Link->full_resolve($fn);
//	return if exists $$visited{$nfn};
//	$$visited{$nfn}=1;
//
//	if ($recurse && -d $fn) {
//		if (opendir(my $dir, $fn)) {
//			foreach my $f ( grep { !/^\.{1,2}$/ } readdir($dir)) {
//				$f.='/' if -d "$fn$f" && $f!~/\/$/;
//				changeFilePermissions($fn.$f, $mode, $type, $recurse, $visited);
//			}
//			closedir($dir);
//		}
//	}
//}
//sub buildExprFromBasicSearchWhereClause {
//	my ($op, $xmlref, $superop) = @_;
//	my ($expr,$type) = ( '', '', undef);
//	my $ns = '{DAV:}';
//	if (!defined $op) {
//		my @ops = keys %{$xmlref};
//		return buildExprFromBasicSearchWhereClause($ops[0], $$xmlref{$ops[0]}); 
//	}
//
//	$op=~s/\Q$ns\E//;
//	$type='bool';
//
//	if (ref($xmlref) eq 'ARRAY') {	
//		foreach my $oo (@{$xmlref}) {
//			my ($ne,$nt) = buildExprFromBasicSearchWhereClause($op, $oo, $superop);
//			my ($nes,$nts) = buildExprFromBasicSearchWhereClause($superop, undef, $superop);
//			$expr.= $nes if $expr ne "";
//			$expr.= "($ne)";
//		}
//		return $expr;
//	}
//
//	study $op;
//	if ($op =~ /^(and|or)$/) {
//		if (ref($xmlref) eq 'HASH') {
//			foreach my $o (keys %{$xmlref}) {
//				$expr .= $op eq 'and' ? ' && ' : ' || ' if $expr ne "";
//				my ($ne, $nt) =  buildExprFromBasicSearchWhereClause($o, $$xmlref{$o}, $op);
//				$expr .= "($ne)";
//			}
//		} else {
//			return $op eq 'and' ? ' && ' : ' || ';
//		}
//	} elsif ($op eq 'not') {
//		my @k = keys %{$xmlref};
//		my ($ne,$nt) = buildExprFromBasicSearchWhereClause($k[0], $$xmlref{$k[0]});
//		$expr="!($ne)";
//	} elsif ($op eq 'is-collection') {
//		$expr="getPropValue('{DAV:}iscollection',\$filename,\$request_uri)==1";
//	} elsif ($op eq 'is-defined') {
//		my ($ne,$nt)=buildExprFromBasicSearchWhereClause('{DAV:}prop',$$xmlref{'{DAV:}prop'});
//		$expr="$ne ne '__undef__'";
//	} elsif ($op =~ /^(language-defined|language-matches)$/) {
//		$expr='0!=0';
//	} elsif ($op =~ /^(eq|lt|gt|lte|gte)$/) {
//		my $o = $op;
//		my ($ne1,$nt1) = buildExprFromBasicSearchWhereClause('{DAV:}prop',$$xmlref{'{DAV:}prop'});
//		my ($ne2,$nt2) = buildExprFromBasicSearchWhereClause('{DAV:}literal', $$xmlref{'{DAV:}literal'});
//		$ne2 =~ s/'/\\'/sg;
//		$ne2 = $SEARCH_SPECIALCONV{$nt1} ? $SEARCH_SPECIALCONV{$nt1}."('$ne2')" : "'$ne2'";
//		my $cl= $$xmlref{'caseless'} || $$xmlref{'{DAV:}caseless'} || 'yes';
//		$expr = (($nt1 =~ /(string|xml)/ && $cl ne 'no')?"lc($ne1)":$ne1)
//                      . ' '.($SEARCH_SPECIALOPS{$nt1}{$o} || $o).' '
//		      . (($nt1 =~ /(string|xml)/ && $cl ne 'no')?"lc($ne2)":$ne2);
//	} elsif ($op eq 'like') {
//		my ($ne1,$nt1) = buildExprFromBasicSearchWhereClause('{DAV:}prop',$$xmlref{'{DAV:}prop'});
//		my ($ne2,$nt2) = buildExprFromBasicSearchWhereClause('{DAV:}literal', $$xmlref{'{DAV:}literal'});
//		$ne2=~s/\//\\\//gs;     ## quote slashes 
//		$ne2=~s/(?<!\\)_/./gs;  ## handle unescaped wildcard _ -> .
//		$ne2=~s/(?<!\\)%/.*/gs; ## handle unescaped wildcard % -> .*
//		my $cl= $$xmlref{'caseless'} || $$xmlref{'{DAV:}caseless'} || 'yes';
//		$expr = "$ne1 =~ /$ne2/s" . ($cl eq 'no'?'':'i');
//	} elsif ($op eq 'contains') {
//		my $content = ref($xmlref) eq "" ? $xmlref : $$xmlref{content};
//		my $cl = ref($xmlref) eq "" ? 'yes' : ($$xmlref{caseless} || $$xmlref{'{DAV:}caseless'} || 'yes');
//		$content=~s/\//\\\//g;
//		$expr="getFileContent(\$filename) =~ /\\Q$content\\E/s".($cl eq 'no'?'':'i');
//	} elsif ($op eq 'prop') {
//		my @props = keys %{$xmlref};
//		$props[0] =~ s/'/\\'/sg;
//		$expr = "getPropValue('$props[0]',\$filename,\$request_uri)";
//		$type = $SEARCH_PROPTYPES{$props[0]} || $SEARCH_PROPTYPES{default};
//		$expr = $SEARCH_SPECIALCONV{$type}."($expr)" if exists $SEARCH_SPECIALCONV{$type};
//	} elsif ($op eq 'literal') {
//		$expr = ref($xmlref) ne "" ? convXML2Str($xmlref) : $xmlref;
//		$type = $op;
//	} else {
//		$expr= $xmlref;
//		$type= $op;
//	}
//
//	return ($expr, $type);
//}
//sub convXML2Str {
//	my ($xml) = @_;
//	return defined $xml ? lc(createXML($xml,1)) : $xml;
//}
//sub getPropValue {
//	my ($prop, $fn, $uri) = @_;
//	my (%stat,%r200,%r404);
//
//	return $CACHE{getPropValue}{$fn}{$prop} if exists $CACHE{getPropValue}{$fn}{$prop};
//
//	my $propname = $prop;
//	$propname=~s/^{[^}]*}//;
//
//	my $propval = grep(/^\Q$propname\E$/,@PROTECTED_PROPS)==0 ? db_getProperty($fn, $prop) : undef;
//
//	if (! defined $propval) {
//		getProperty($fn, $uri, $propname, undef, \%r200, \%r404) ;
//		$propval = $r200{prop}{$propname};
//	}
//
//	$propval = defined $propval ? $propval : '__undef__';
//
//	$CACHE{getPropValue}{$fn}{$prop} = $propval;
//
//	debug("getPropValue: $prop = $propval");
//
//	return $propval;
//}
//sub doBasicSearch {
//	my ($expr, $base, $href, $depth, $limit, $matches, $visited) = @_;
//	return if defined $limit && $limit > 0 && $#$matches + 1 >= $limit;
//
//	return if defined $depth && $depth ne 'infinity' && $depth < 0 ;
//
//	$base.='/' if -d $base && $base !~ /\/$/;
//	$href.='/' if -d $base && $href !~ /\/$/;
//
//	my $filename = $base;
//	my $request_uri = $href;
//
//	my $res = eval  $expr ;
//	if ($@) {
//		debug("doBasicSearch: problem in $expr: $@");
//	} elsif ($res) {
//		debug("doBasicSearch: $base MATCHED");
//		push @{$matches}, { fn=> $base, href=> $href };
//	}
//	my $nbase = File::Spec::Link->full_resolve($base);
//	return if exists $$visited{$nbase} && ($depth eq 'infinity' || $depth < 0);
//	$$visited{$nbase}=1;
//
//	if ((-d $base)&&(opendir(my $d, $base))) {
//		foreach my $sf (grep { !/^\.{1,2}$/ } readdir($d)) {
//			my $nbase = $base.$sf;
//			my $nhref = $href.$sf;
//			doBasicSearch($expr, $base.$sf, $href.$sf, defined $depth  && $depth ne 'infinity' ? $depth - 1 : $depth, $limit, $matches, $visited);
//		}
//		closedir($d);
//	}
//}
//sub handleBasicSearch {
//	my ($xmldata, $resps, $error) = @_;
//	# select > (allprop | prop)  
//	my ($propsref,  $all, $noval) = handlePropFindElement($$xmldata{'{DAV:}select'});
//	# where > op > (prop,literal) 
//	my ($expr,$type) =  buildExprFromBasicSearchWhereClause(undef, $$xmldata{'{DAV:}where'});
//	debug("_SEARCH: call buildExpr: expr=$expr");
//	# from > scope+ > (href, depth, include-versions?)
//	my @scopes;
//	if (ref($$xmldata{'{DAV:}from'}{'{DAV:}scope'}) eq 'HASH') {
//		push @scopes, $$xmldata{'{DAV:}from'}{'{DAV:}scope'}; 
//	} elsif (ref($$xmldata{'{DAV:}from'}{'{DAV:}scope'}) eq 'ARRAY') {
//		push @scopes, @{$$xmldata{'{DAV:}from'}{'{DAV:}scope'}};
//	} else { 
//		push @scopes, { '{DAV:}href'=>$REQUEST_URI, '{DAV:}depth'=>'infinity'};
//	}
//	# limit > nresults 
//	my $limit = $$xmldata{'{DAV:}limit'}{'{DAV:}nresults'};
//
//	my $host = $cgi->http('Host');
//	my @matches;
//	foreach my $scope (@scopes) {
//		my $depth = $$scope{'{DAV:}depth'};
//		my $href = $$scope{'{DAV:}href'};
//		my $base = $href;
//		$base =~ s@^(https?://([^\@]+\@)?\Q$host\E)?$VIRTUAL_BASE@@;
//		$base = $DOCUMENT_ROOT.uri_unescape(uri_unescape($base));
//		
//		debug("handleBasicSearch: base=$base (href=$href), depth=$depth, limit=$limit\n");
//
//		if (!-e $base) {
//			push @{$error}, { 'search-scope-valid'=> { response=> { href=>$href, status=>'HTTP/1.1 404 Not Found' } } };
//			return;
//		}
//		doBasicSearch($expr, $base, $href, $depth, $limit, \@matches);
//	}
//	# orderby > order+ (caseless=(yes|no))> (prop|score), (ascending|descending)? 
//	my $sortfunc="";
//	if (exists $$xmldata{'{DAV:}orderby'} && $#matches>0) {
//		my @orders;
//		if (ref($$xmldata{'{DAV:}orderby'}{'{DAV:}order'}) eq 'ARRAY') {
//			push @orders, @{$$xmldata{'{DAV:}orderby'}{'{DAV:}order'}};
//		} elsif (ref($$xmldata{'{DAV:}orderby'}{'{DAV:}order'}) eq 'HASH') {
//			push @orders, $$xmldata{'{DAV:}orderby'}{'{DAV:}order'};
//		}
//		foreach my $order (@orders) {
//			my @props = keys %{$$order{'{DAV:}prop'}};
//			my $prop = $props[0] || '{DAV:}displayname';
//			my $proptype = $SEARCH_PROPTYPES{$prop} || $SEARCH_PROPTYPES{default};
//			my $type = $$order{'{DAV:}descending'} ?  'descending' : 'ascending';
//			debug("orderby: prop=$prop, proptype=$proptype, type=$type");
//			my($ta,$tb,$cmp);
//			$ta = qq@getPropValue('$prop',\$\$a{fn},\$\$a{href})@;
//			$tb = qq@getPropValue('$prop',\$\$b{fn},\$\$b{href})@;
//			if ($SEARCH_SPECIALCONV{$proptype}) {
//				$ta = $SEARCH_SPECIALCONV{$proptype}."($ta)";
//				$tb = $SEARCH_SPECIALCONV{$proptype}."($tb)";
//			}
//			$cmp = $SEARCH_SPECIALOPS{$proptype}{cmp} || 'cmp';
//			$sortfunc.=" || " if $sortfunc ne "";
//			$sortfunc.="$ta $cmp $tb" if $type eq 'ascending';
//			$sortfunc.="$tb $cmp $ta" if $type eq 'descending';
//		}
//
//		debug("orderby: sortfunc=$sortfunc");
//	}
//
//	debug("handleBasicSearch: matches=$#matches");
//	foreach my $match ( sort { eval($sortfunc) } @matches ) {
//		push @{$resps}, { href=> $$match{href}, propstat=>getPropStat($$match{fn},$$match{href},$propsref,$all,$noval) };
//	}
//
//}
//sub removeProperty {
//	my ($propname, $elementParentRef, $resp_200, $resp_403) = @_;
//	debug("removeProperty: $PATH_TRANSLATED: $propname");
//	db_removeProperty($PATH_TRANSLATED, $propname);
//	$$resp_200{href}=$REQUEST_URI;
//	$$resp_200{propstat}{status}='HTTP/1.1 200 OK';
//	$$resp_200{propstat}{prop}{$propname} = undef;
//}
//sub readDirBySuffix {
//	my ($fn, $base, $hrefs, $suffix, $depth, $visited) = @_;
//	debug("readDirBySuffix($fn, ..., $suffix, $depth)");
//
//	my $nfn = File::Spec::Link->full_resolve($fn);
//	return if exists $$visited{$nfn} && ($depth eq 'infinity' || $depth < 0);
//	$$visited{$nfn}=1;
//
//	if (opendir(DIR,$fn)) {
//		foreach my $sf (grep { !/^\.{1,2}$/ } readdir(DIR)) {
//			$sf.='/' if -d $fn.$sf;
//			my $nbase=$base.$sf;
//			push @{$hrefs}, $nbase if -f $fn.$sf && $sf =~ /\.\Q$suffix\E/;
//			readDirBySuffix($fn.$sf, $nbase, $hrefs, $suffix, $depth - 1, $visited) if $depth!=0 && -d $fn.$sf;
//			## XXX add only files with requested components 
//			## XXX filter (comp-filter > comp-filter >)
//		}
//		closedir(DIR);
//	}
//}
//sub handlePropFindElement {
//	my ($xmldata) = @_;
//	my @props;
//	my $all;
//	my $noval;
//	foreach my $propfind (keys %{$xmldata} ) {
//		my $nons = $propfind;
//		my $ns ="";
//		if ($nons=~s/{([^}]*)}//) {
//			$ns = $1;
//		}
//		if (($nons =~ /(allprop|propname)/)&&($all)) {
//			printHeaderAndContent('400 Bad Request');
//			return;
//		} elsif ($nons =~ /^(allprop|propname)$/) {
//			$all = 1;
//			$noval = $1 eq 'propname';
//			push @props, @KNOWN_COLL_PROPS, @KNOWN_FILE_PROPS if $noval;
//			push @props, @ALLPROP_PROPS unless $noval;
//		} elsif ($nons =~ /^(prop|include)$/) {
//			handlePropElement($$xmldata{$propfind},\@props);
//		} elsif (grep (/\Q$nons\E/,@IGNORE_PROPS)) {
//			next;
//		} elsif (defined $NAMESPACES{$$xmldata{$propfind}} || defined $NAMESPACES{$ns} )  { # sometimes the namespace: ignore
//		} else {	
//			debug("Unknown element $propfind ($nons) in PROPFIND request");
//			debug($NAMESPACES{$$xmldata{$propfind}});
//			printHeaderAndContent('400 Bad Request');
//			exit;
//		}
//	}
//	return (\@props, $all, $noval);
//}
//sub handlePropElement {
//	my ($xmldata, $props) = @_;
//	foreach my $prop (keys %{$xmldata}) {
//		my $nons = $prop;
//		my $ns= "";
//		if ($nons=~s/{([^}]*)}//) {
//			$ns = $1;
//		}
//		if (ref($$xmldata{$prop}) !~/^(HASH|ARRAY)$/) { # ignore namespaces
//		} elsif ($ns eq "" && ! defined $$xmldata{$prop}{xmlns}) {
//			printHeaderAndContent('400 Bad Request');
//			exit;
//		} elsif (grep(/\Q$nons\E/, @KNOWN_FILE_PROPS, @KNOWN_COLL_PROPS)>0)  {
//			push @{$props}, $nons;
//		} elsif ($ns eq "") {
//			push @{$props}, '{}'.$prop;
//		} else {
//			push @{$props}, $prop;
//		}
//	}
//
//}
//sub getPropStat {
//	my ($fn,$uri,$props,$all,$noval) = @_;
//	my @propstat= ();
//
//	my $nfn = File::Spec::Link->full_resolve($fn);
//
//	my @stat = stat($fn);
//	my %resp_200 = (status=>'HTTP/1.1 200 OK');
//	my %resp_404 = (status=>'HTTP/1.1 404 Not Found');
//	foreach my $prop (@{$props}) {
//		my ($xmlnsuri,$propname) = ('DAV:',$prop);
//		if ($prop=~/^{([^}]*)}(.*)$/) {
//			($xmlnsuri, $propname) = ($1,$2);
//		} 
//		if (grep(/^\Q$propname\E$/,@UNSUPPORTED_PROPS) >0) {
//			debug("getPropStat: UNSUPPORTED: $propname");
//			$resp_404{prop}{$prop}=undef;
//			next;
//		} elsif (( !defined $NAMESPACES{$xmlnsuri} || grep(/^\Q$propname\E$/,-d $fn?@KNOWN_COLL_LIVE_PROPS:@KNOWN_FILE_LIVE_PROPS)>0 ) && grep(/^\Q$propname\E$/,@PROTECTED_PROPS)==0) { 
//			my $dbval = db_getProperty($fn, $prop=~/{[^}]*}/?$prop:'{'.getNameSpaceUri($prop)."}$prop");
//			if (defined $dbval) {
//				$resp_200{prop}{$prop}=$noval?undef:$dbval;
//				next;
//			} elsif (grep(/^\Q$propname\E$/,-d $nfn?@KNOWN_COLL_LIVE_PROPS:@KNOWN_FILE_LIVE_PROPS)==0) {
//				debug("getPropStat: #1 NOT FOUND: $prop ($propname, $xmlnsuri)");
//				$resp_404{prop}{$prop}=undef;
//			}
//		} 
//		if (grep(/^\Q$propname\E$/, -d $nfn ? @KNOWN_COLL_PROPS : @KNOWN_FILE_PROPS)>0) {
//			if ($noval) { 
//				$resp_200{prop}{$prop}=undef;
//			} else {
//				getProperty($fn, $uri, $prop, \@stat, \%resp_200,\%resp_404);
//			}
//		} elsif (!$all) {
//			debug("getPropStat: #2 NOT FOUND: $prop ($propname, $xmlnsuri)");
//			$resp_404{prop}{$prop} = undef;
//		}
//	} # foreach
//
//	push @propstat, \%resp_200 if exists $resp_200{prop};
//	push @propstat, \%resp_404 if exists $resp_404{prop};
//	return \@propstat;
//}
//sub getProperty {
//	my ($fn, $uri, $prop, $statRef, $resp_200, $resp_404) = @_;
//	debug("getProperty: fn=$fn, uri=$uri, prop=$prop");
//	my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks) = defined $statRef ? @{$statRef} : stat($fn);
//	$$resp_200{prop}{creationdate}=strftime('%Y-%m-%dT%H:%M:%SZ' ,gmtime($ctime)) if $prop eq 'creationdate';
//	$$resp_200{prop}{displayname}=$cgi->escape(basename($uri)) if $prop eq 'displayname' && !defined $$resp_200{prop}{displayname};
//	$$resp_200{prop}{getcontentlanguage}='en' if $prop eq 'getcontentlanguage';
//	$$resp_200{prop}{getcontentlength}= $size if $prop eq 'getcontentlength';
//	$$resp_200{prop}{getcontenttype}=(-d $fn?'httpd/unix-directory':getMIMEType($fn)) if $prop eq 'getcontenttype';
//	$$resp_200{prop}{getetag}=getETag($fn) if $prop eq 'getetag';
//	$$resp_200{prop}{getlastmodified}=strftime('%a, %d %b %Y %T GMT' ,gmtime($mtime)) if $prop eq 'getlastmodified';
//	$$resp_200{prop}{lockdiscovery}=getLockDiscovery($fn) if $prop eq 'lockdiscovery';
//	$$resp_200{prop}{resourcetype}=(-d $fn?{collection=>undef}:undef) if $prop eq 'resourcetype';
//	$$resp_200{prop}{resourcetype}{calendar}=undef if $prop eq 'resourcetype' && $ENABLE_CALDAV && -d $fn;
//	$$resp_200{prop}{resourcetype}{'schedule-inbox'}=undef if $prop eq 'resourcetype' && $ENABLE_CALDAV_SCHEDULE && -d $fn;
//	$$resp_200{prop}{resourcetype}{'schedule-outbox'}=undef if $prop eq 'resourcetype' && $ENABLE_CALDAV_SCHEDULE && -d $fn;
//	$$resp_200{prop}{resourcetype}{addressbook}=undef if $prop eq 'resourcetype' && $ENABLE_CARDDAV && -d $fn;
//	$$resp_200{prop}{resourcetype}{'vevent-collection'}=undef if $prop eq 'resourcetype' && $ENABLE_GROUPDAV && -d $fn;
//	$$resp_200{prop}{resourcetype}{'vtodo-collection'}=undef if $prop eq 'resourcetype' && $ENABLE_GROUPDAV && -d $fn;
//	$$resp_200{prop}{resourcetype}{'vcard-collection'}=undef if $prop eq 'resourcetype' && $ENABLE_GROUPDAV && -d $fn;
//	$$resp_200{prop}{'component-set'}='VEVENT,VTODO,VCARD' if $prop eq 'component-set';
//	if ($prop eq 'supportedlock') {
//		$$resp_200{prop}{supportedlock}{lockentry}[0]{lockscope}{exclusive}=undef;
//		$$resp_200{prop}{supportedlock}{lockentry}[0]{locktype}{write}=undef;
//		$$resp_200{prop}{supportedlock}{lockentry}[1]{lockscope}{shared}=undef;
//		$$resp_200{prop}{supportedlock}{lockentry}[1]{locktype}{write}=undef;
//	}
//	$$resp_200{prop}{executable}=(-x $fn )?'T':'F' if $prop eq 'executable';
//
//	$$resp_200{prop}{source}={ 'link'=> { 'src'=>$uri, 'dst'=>$uri }} if $prop eq 'source';
//
//	if ($prop eq 'quota-available-bytes' || $prop eq 'quota-used-bytes' || $prop eq 'quota' || $prop eq 'quotaused') {
//		my ($ql,$qu) = getQuota();
//		if (defined $ql && defined $qu) {
//			$$resp_200{prop}{'quota-available-bytes'} = $ql - $qu if $prop eq 'quota-available-bytes';
//			$$resp_200{prop}{'quota-used-bytes'} = $qu if $prop eq 'quota-used-bytes';
//			$$resp_200{prop}{'quota'} = $ql if $prop eq 'quota';
//			$$resp_200{prop}{'quotaused'}= $qu if $prop eq 'quotaused';
//		} else {
//			$$resp_404{prop}{'quota-available-bytes'} = undef if $prop eq 'quota-available-bytes';
//			$$resp_404{prop}{'quota-used-bytes'} = undef if $prop eq 'quota-used-bytes';
//		}
//		next;
//	}
//	$$resp_200{prop}{childcount}=(-d $fn?getDirInfo($fn,$prop):0) if $prop eq 'childcount';
//	$$resp_200{prop}{id}=$uri if $prop eq 'id';
//	$$resp_200{prop}{isfolder}=(-d $fn?1:0) if $prop eq 'isfolder';
//	$$resp_200{prop}{ishidden}=(basename($fn)=~/^\./?1:0) if $prop eq 'ishidden';
//	$$resp_200{prop}{isstructureddocument}=0 if $prop eq 'isstructureddocument';
//	$$resp_200{prop}{hassubs}=(-d $fn ?getDirInfo($fn,$prop):0) if $prop eq 'hassubs';
//	$$resp_200{prop}{nosubs}=(-d $fn?(-w $fn?1:0):1) if $prop eq 'nosubs';
//	$$resp_200{prop}{objectcount}=(-d $fn?getDirInfo($fn,$prop):0) if $prop eq 'objectcount';
//	$$resp_200{prop}{reserved}=0 if $prop eq 'reserved';
//	$$resp_200{prop}{visiblecount}=(-d $fn?getDirInfo($fn,$prop):0) if $prop eq 'visiblecount';
//
//	$$resp_200{prop}{iscollection}=(-d $fn?1:0) if $prop eq 'iscollection';
//	$$resp_200{prop}{isFolder}=(-d $fn?1:0) if $prop eq 'isFolder';
//	$$resp_200{prop}{'authoritative-directory'}=(-d $fn?'t':'f') if $prop eq 'authoritative-directory';
//	$$resp_200{prop}{resourcetag}=$REQUEST_URI if $prop eq 'resourcetag';
//	$$resp_200{prop}{'repl-uid'}=getuuid($fn) if $prop eq 'repl-uid';
//	$$resp_200{prop}{modifiedby}=$ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER} if $prop eq 'modifiedby';
//	$$resp_200{prop}{Win32CreationTime}=strftime('%a, %d %b %Y %T GMT' ,gmtime($ctime)) if $prop eq 'Win32CreationTime';
//	if ($prop eq 'Win32FileAttributes') {
//		my $fileattr = 128 + 32; # 128 - Normal, 32 - Archive, 4 - System, 2 - Hidden, 1 - Read-Only
//		$fileattr+=1 unless -w $fn;
//		$fileattr+=2 if basename($fn)=~/^\./;
//		$$resp_200{prop}{Win32FileAttributes}=sprintf("%08x",$fileattr);
//	}
//	$$resp_200{prop}{Win32LastAccessTime}=strftime('%a, %d %b %Y %T GMT' ,gmtime($atime)) if $prop eq 'Win32LastAccessTime';
//	$$resp_200{prop}{Win32LastModifiedTime}=strftime('%a, %d %b %Y %T GMT' ,gmtime($mtime)) if $prop eq 'Win32LastModifiedTime';
//	$$resp_200{prop}{name}=$cgi->escape(basename($fn)) if $prop eq 'name';
//	$$resp_200{prop}{href}=$uri if $prop eq 'href';
//	$$resp_200{prop}{parentname}=$cgi->escape(basename(dirname($uri))) if $prop eq 'parentname';
//	$$resp_200{prop}{isreadonly}=(!-w $fn?1:0) if $prop eq 'isreadonly';
//	$$resp_200{prop}{isroot}=($fn eq $DOCUMENT_ROOT?1:0) if $prop eq 'isroot';
//	$$resp_200{prop}{getcontentclass}=(-d $fn?'urn:content-classes:folder':'urn:content-classes:document') if $prop eq 'getcontentclass';
//	$$resp_200{prop}{contentclass}=(-d $fn?'urn:content-classes:folder':'urn:content-classes:document') if $prop eq 'contentclass';
//	$$resp_200{prop}{lastaccessed}=strftime('%m/%d/%Y %I:%M:%S %p' ,gmtime($atime)) if $prop eq 'lastaccessed';
//
//	$$resp_200{prop}{owner} = { href=>$uri } if $prop eq 'owner';
//	$$resp_200{prop}{group} = { href=>$uri } if $prop eq 'group';
//	$$resp_200{prop}{'supported-privilege-set'}= getACLSupportedPrivilegeSet($fn) if $prop eq 'supported-privilege-set';
//	$$resp_200{prop}{'current-user-privilege-set'} = getACLCurrentUserPrivilegeSet($fn) if $prop eq 'current-user-privilege-set';
//	$$resp_200{prop}{acl} = getACLProp($mode) if $prop eq 'acl';
//	$$resp_200{prop}{'acl-restrictions'} = {'no-invert'=>undef,'required-principal'=>{all=>undef,property=>[{owner=>undef},{group=>undef}]}} if $prop eq 'acl-restrictions';
//	$$resp_200{prop}{'inherited-acl-set'} = undef if $prop eq 'inherited-acl-set';
//	$$resp_200{prop}{'principal-collection-set'} = { href=> $PRINCIPAL_COLLECTION_SET }, if $prop eq 'principal-collection-set';
//
//	$$resp_200{prop}{'calendar-description'} = undef if $prop eq 'calendar-description';
//	$$resp_200{prop}{'calendar-timezone'} = undef if $prop eq 'calendar-timezone';
//	$$resp_200{prop}{'supported-calendar-component-set'} = '<C:comp name="VEVENT"/><C:comp name="VTODO"/><C:comp name="VJOURNAL"/><C:comp name="VTIMEZONE"/>' if $prop eq 'supported-calendar-component-set';
//	$$resp_200{prop}{'supported-calendar-data'}='<C:calendar-data content-type="text/calendar" version="2.0"/>' if $prop eq 'supported-calendar-data';
//	$$resp_200{prop}{'max-resource-size'}=20000000 if $prop eq 'max-resource-size';
//	$$resp_200{prop}{'min-date-time'}='19000101T000000Z' if $prop eq 'min-date-time';
//	$$resp_200{prop}{'max-date-time'}='20491231T235959Z' if $prop eq 'max-date-time';
//	$$resp_200{prop}{'max-instances'}=100 if $prop eq 'max-instances';
//	$$resp_200{prop}{'max-attendees-per-instance'}=100 if $prop eq 'max-attendees-per-instance';
//	##$$resp_200{prop}{'calendar-data'}='<![CDATA['.getFileContent($fn).']]>' if $prop eq 'calendar-data';
//	if ($prop eq 'calendar-data') {
//		if ($fn=~/\.ics$/i) {
//			$$resp_200{prop}{'calendar-data'}=$cgi->escapeHTML(getFileContent($fn));
//		} else {
//			$$resp_404{prop}{'calendar-data'}=undef;
//		}
//	}
//	$$resp_200{prop}{'getctag'}=getETag($fn)  if $prop eq 'getctag';
//	$$resp_200{prop}{'current-user-principal'}{href}=$CURRENT_USER_PRINCIPAL if $prop eq 'current-user-principal';
//	$$resp_200{prop}{'principal-URL'}{href}=$CURRENT_USER_PRINCIPAL if $prop eq 'principal-URL';
//	$$resp_200{prop}{'calendar-home-set'}{href}=getCalendarHomeSet($uri) if $prop eq 'calendar-home-set';
//	$$resp_200{prop}{'calendar-user-address-set'}{href}= $CURRENT_USER_PRINCIPAL if $prop eq 'calendar-user-address-set';
//	$$resp_200{prop}{'schedule-inbox-URL'}{href} = getCalendarHomeSet($uri) if $prop eq 'schedule-inbox-URL';
//	$$resp_200{prop}{'schedule-outbox-URL'}{href} = getCalendarHomeSet($uri) if $prop eq 'schedule-outbox-URL';
//	$$resp_200{prop}{'calendar-user-type'}='INDIVIDUAL' if $prop eq 'calendar-user-type';
//	$$resp_200{prop}{'schedule-calendar-transp'}{transparent} = undef if $prop eq 'schedule-calendar-transp';
//	$$resp_200{prop}{'schedule-default-calendar-URL'}=getCalendarHomeSet($uri) if $prop eq 'schedule-default-calendar-URL';
//	$$resp_200{prop}{'schedule-tag'}=getETag($fn) if $prop eq 'schedule-tag';
//
//	if ($prop eq 'address-data') {
//		if ($fn =~ /\.vcf$/i) {
//			$$resp_200{prop}{'address-data'}=$cgi->escapeHTML(getFileContent($fn));
//		} else {
//			$$resp_404{prop}{'address-data'}=undef;
//		}
//	}
//	$$resp_200{prop}{'addressbook-description'} = $cgi->escape(basename($fn)) if $prop eq 'addressbook-description';
//	$$resp_200{prop}{'supported-address-data'}='<A:address-data-type content-type="text/vcard" version="3.0"/>' if $prop eq 'supported-address-data';
//	$$resp_200{prop}{'{urn:ietf:params:xml:ns:carddav}max-resource-size'}=20000000 if $prop eq 'max-resource-size' && $ENABLE_CARDDAV;
//	$$resp_200{prop}{'addressbook-home-set'}{href}=getAddressbookHomeSet($uri) if $prop eq 'addressbook-home-set';
//	$$resp_200{prop}{'principal-address'}{href}=$uri if $prop eq 'principal-address';
//	
//	
//	$$resp_200{prop}{'supported-report-set'} = 
//				{ 'supported-report' => 
//					[ 	
//						{ report=>{ 'acl-principal-prop-set'=>undef } },
//						{ report=>{ 'principal-match'=>undef } },
//						{ report=>{ 'principal-property-search'=>undef } }, 
//						{ report=>{ 'calendar-multiget'=>undef } },  
//						{ report=>{ 'calendar-query'=>undef } },
//						{ report=>{ 'free-busy-query'=>undef } },
//						{ report=>{ 'addressbook-query'=>undef} },
//						{ report=>{ 'addressbook-multiget'=>undef} },
//					]
//				} if $prop eq 'supported-report-set';
//
//	if ($prop eq 'supported-method-set') {
//		$$resp_200{prop}{'supported-method-set'} = '';
//		foreach my $method (@{getSupportedMethods($fn)}) {
//			$$resp_200{prop}{'supported-method-set'} .= '<D:supported-method name="'.$method.'"/>';
//		}
//	}
//
//	if ($prop eq 'resource-id') {
//		my $e = getETag(File::Spec::Link->full_resolve($fn));
//		$e=~s/"//g;
//		$$resp_200{prop}{'resource-id'} = 'urn:uuid:'.$e;
//	}
//}
//
//sub cmp_elements {
//	my $aa = defined $ELEMENTORDER{$a} ? $ELEMENTORDER{$a} : $ELEMENTORDER{default};
//	my $bb = defined $ELEMENTORDER{$b} ? $ELEMENTORDER{$b} : $ELEMENTORDER{default};
//	if (defined $ELEMENTORDER{$a} || defined $ELEMENTORDER{$b} ) {
//		return $aa <=> $bb;
//	} 
//	return $a cmp $b;
//}
//sub createXMLData {
//        my ($w,$d,$xmlns) =@_;
//        if (ref($d) eq 'HASH') {
//                foreach my $e (sort cmp_elements keys %{$d}) {
//			my $el = $e;
//                        my $euns = "";
//                        my $uns;
//                        my $ns = getNameSpace($e);
//			my $attr = "";
//			if (defined $DATATYPES{$e}) {
//				$attr.=" ".$DATATYPES{$e};
//				if ($DATATYPES{$e}=~/(\w+):dt/) {
//					$$xmlns{$1}=1 if defined $NAMESPACEABBR{$1};
//				}
//			}
//                        if ($e=~/{([^}]*)}/) {
//                                $ns = $1;
//                                if (defined $NAMESPACES{$ns})  {
//                                        $el=~s/{[^}]*}//;
//                                        $ns = $NAMESPACES{$ns};
//                                } else {
//                                        $uns = $ns;
//                                        $euns = $e;
//                                        $euns=~s/{[^}]*}//;
//                                }
//                        }
//			my $el_end = $el;
//			$el_end=~s/ .*$//;
//			my $euns_end = $euns;
//			$euns_end=~s/ .*$//;
//			$$xmlns{$ns}=1 unless defined $uns;
//			my $nsd="";
//			if ($e eq 'xmlns') { # ignore namespace defs
//			} elsif ($e eq 'content') { #
//					$$w.=$$d{$e};	
//                        } elsif ( ! defined $$d{$e} ) {
//                                if (defined $uns) {
//                                        $$w.="<${euns} xmlns=\"$uns\"/>";
//                                } else {
//                                        $$w.="<${ns}:${el}${nsd}${attr}/>";
//                                }
//                        } elsif (ref($$d{$e}) eq 'ARRAY') {
//                                foreach my $e1 (@{$$d{$e}}) {
//					my $tmpw="";
//                                        createXMLData(\$tmpw,$e1,$xmlns);
//					if ($NAMESPACEELEMENTS{$el}) {
//						foreach my $abbr (keys %{$xmlns}) {
//							$nsd.=qq@ xmlns:$abbr="$NAMESPACEABBR{$abbr}"@;
//							delete $$xmlns{$abbr};
//						}
//					}
//                                        $$w.=qq@<${ns}:${el}${nsd}${attr}>@;
//					$$w.=$tmpw;
//                                        $$w.="</${ns}:${el_end}>";
//                                }
//                        } else {
//                                if (defined $uns) {
//                                        $$w.=qq@<${euns} xmlns="$uns">@;
//                                        createXMLData($w, $$d{$e}, $xmlns);
//                                        $$w.=qq@</${euns_end}>@;
//                                } else {
//					my $tmpw="";
//                                        createXMLData(\$tmpw, $$d{$e}, $xmlns);
//					if ($NAMESPACEELEMENTS{$el}) {
//						foreach my $abbr (keys %{$xmlns}) {
//							$nsd.=qq@ xmlns:$abbr="$NAMESPACEABBR{$abbr}"@;
//							delete $$xmlns{$abbr};
//						}
//					}
//                                        $$w.=qq@<${ns}:${el}${nsd}${attr}>@;
//					$$w.=$tmpw;
//                                        $$w.="</${ns}:${el_end}>";
//                                }
//                        }
//                }
//        } elsif (ref($d) eq 'ARRAY') {
//                foreach my $e (@{$d}) {
//                        createXMLData($w, $e, $xmlns);
//                }
//        } elsif (ref($d) eq 'SCALAR') {
//                $$w.=qq@$d@;
//        } elsif (ref($d) eq 'REF') {
//                createXMLData($w, $$d, $xmlns);
//        } else {
//                $$w.=qq@$d@;
//        }
//}
//
//sub createXML {
//        my ($dataRef, $withoutp) = @_;
//
//        my $data = "";
//	$data=q@<?xml version="1.0" encoding="@.$CHARSET.q@"?>@ unless defined $withoutp;
//	createXMLData(\$data,$dataRef);
//        return $data;
//}
//
//sub getMIMEType {
//	my ($filename) = @_;
//	my $extension= "default";
//	if ($filename=~/\.([^\.]+)$/) {
//		$extension=$1;
//	}
//	my @t = grep /\b\Q$extension\E\b/i, keys %MIMETYPES;
//	return $#t>-1 ? $MIMETYPES{$t[0]} : $MIMETYPES{default};
//}
//sub cmp_files {
//	my $fp_a = $PATH_TRANSLATED.$a;
//	my $fp_b = $PATH_TRANSLATED.$b;
//	return -1 if -d $fp_a && !-d $fp_b;
//	return 1 if !-d $fp_a && -d $fp_b;
//	my $order = $cgi->param('order');
//	if (defined $order) {
//		study $order;
//		if ($order =~ /^(lastmodified|size|mode)/) {
//			my @a_stats = stat $fp_a;
//			my @b_stats = stat $fp_b;
//			my $idx = $order=~/lastmodified/? 9 : $order=~/mode/? 2 : 7;
//			if ($a_stats[$idx] != $b_stats[$idx]) {
//				return $b_stats[$idx] <=> $a_stats[$idx] || lc($b) cmp lc($a) if $order =~/_desc$/;
//				return $a_stats[$idx] <=> $b_stats[$idx] || lc($a) cmp lc($b);
//			}
//		} elsif ($order =~ /mimetype/) {
//			my $a_mime = getMIMEType($a);
//			my $b_mime = getMIMEType($b);
//			if ($a_mime ne $b_mime) {
//				return $b_mime cmp $a_mime || lc($b) cmp lc($a) if $order =~ /_desc$/;
//				return $a_mime cmp $b_mime || lc($a) cmp lc($b);
//			}
//			
//		}
//		return lc($b) cmp lc($a) if $order =~ /_desc$/;
//	}
//	return lc($a) cmp lc($b);
//}
//sub getfancyfilename {
//	my ($full,$s,$m,$fn) = @_;
//	my $ret = $s;
//	my $q = getQueryParams();
//
//	$full = '/' if $full eq '//'; # fixes root folder navigation bug
//
//	$full.="?$q" if defined $q && defined $fn && -d $fn;
//	my $fntext = length($s)>$MAXFILENAMESIZE ? substr($s,0,$MAXFILENAMESIZE-3) : $s;
//
//
//	$ret = $IGNOREFILEPERMISSIONS || (!-d $fn && -r $fn) || -x $fn  ? $cgi->a({href=>$full,title=>$s,style=>'padding:1px'},$cgi->escapeHTML($fntext)) : $cgi->escapeHTML($fntext);
//	$ret .=  length($s)>$MAXFILENAMESIZE ? '...' : (' 'x($MAXFILENAMESIZE-length($s)));
//
//	$full=~/([^\.]+)$/;
//	my $suffix = $1 || $m;
//	my $icon = defined $ICONS{$m}?$ICONS{$m}:$ICONS{default};
//	my $width = $ICON_WIDTH || 22;
//	my $onmouseover="";
//	my $onmouseout="";
//	my $align="";
//	my $id=getETag($fn);
//	$id=~s/\"//g;
//	
//	if ($ENABLE_THUMBNAIL && -r $fn && getMIMEType($fn) =~/^image\//) {
//		$icon=$full.($full=~/\?.*/?';':'?').'action=thumb';
//		if ($THUMBNAIL_WIDTH && $ICON_WIDTH < $THUMBNAIL_WIDTH) {
//			$align="vertical-align:top;padding: 1px 0px 1px 0px;";
//			$onmouseover = qq@javascript:this.intervalFunc=function() { if (this.width<$THUMBNAIL_WIDTH) this.width+=@.(($THUMBNAIL_WIDTH-$ICON_WIDTH)/15).qq@; else window.clearInterval(this.intervalObj);}; this.intervalObj = window.setInterval("document.getElementById('$id').intervalFunc();", 10);@;
//			$onmouseout = qq@javascript:window.clearInterval(this.intervalObj);this.width=$ICON_WIDTH;@;
//		}
//	}
//	$full.= ($full=~/\?/ ? ';' : '?').'action=props';
//	$ret = $cgi->a(  {href=>$full,title=>_tl('showproperties')},
//			 $cgi->img({id=>$id, src=>$icon,alt=>'['.$suffix.']', -style=>"$align;border:0;", -width=>$width, -onmouseover=>$onmouseover,-onmouseout=>$onmouseout})
//			).' '.$ret;
//	return $ret;
//}
//sub deltree {
//	my ($f,$errRef) = @_;
//	$errRef=[] unless defined $errRef;
//	my $count = 0;
//	my $nf = $f; $nf=~s/\/$//;
//	if (!isAllowed($f,1)) {
//		debug("Cannot delete $f: not allowed");
//		push(@$errRef, { $f => "Cannot delete $f" });
//	} elsif (-l $nf) {
//		if (unlink($nf)) {
//			$count++;
//			db_deleteProperties($f);
//			db_delete($f);
//		} else {
//			push(@$errRef, { $f => "Cannot delete '$f': $!" });
//		}
//	} elsif (-d $f) {
//		if (opendir(DIR,$f)) {
//			foreach my $sf (grep { !/^\.{1,2}$/ } readdir(DIR)) {
//				my $full = $f.$sf;
//				$full.='/' if -d $full && $full!~/\/$/;
//				$count+=deltree($full,$errRef);
//			}
//			closedir(DIR);
//			if (rmdir $f) {
//				$count++;
//				$f.='/' if $f!~/\/$/;
//				db_deleteProperties($f);
//				db_delete($f);
//			} else {
//				push(@$errRef, { $f => "Cannot delete '$f': $!" });
//			}
//		} else {
//			push(@$errRef, { $f => "Cannot open '$f': $!" });
//		}
//	} elsif (-e $f) {
//		if (unlink($f)) {	
//			$count++;
//			db_deleteProperties($f);
//			db_delete($f);
//		} else {
//			push(@$errRef, { $f  => "Cannot delete '$f' : $!" }) ;
//		}
//	} else {
//		push(@$errRef, { $f => "File/Folder '$f' not found" });
//	}
//	return $count;
//}
//sub getETag {
//	my ($file) = @_;
//	$file = $PATH_TRANSLATED unless defined $file;
//	my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks) = stat($file);
//	my $digest = new Digest::MD5;
//	$digest->add($file);
//	$digest->add($size);
//	$digest->add($mtime);
//	return '"'.$digest->hexdigest().'"';
//}
//sub printHeaderAndContent {
//	my ($status, $type, $content, $addHeader) = @_;
//
//	$status='403 Forbidden' unless defined $status;
//	$type='text/plain' unless defined $type;
//	$content="" unless defined $content;
//
//	my $header =$cgi->header(-status=>$status, -type=>$type, -Content_length=>length($content), -ETag=>getETag(), -charset=>$CHARSET, -cookie=>$cgi->cookie(-name=>'lang',-value=>$LANG,-expires=>'+10y'));
//
//	$header = "MS-Author-Via: DAV\r\n$header";
//	$header = "DAV: $DAV\r\n$header";
//	$header="$addHeader\r\n$header" if defined $addHeader;
//	$header="Translate: f\r\n$header" if defined $cgi->http('Translate');
//
//	print $header;
//	binmode(STDOUT);
//	print $content;
//}
//sub printFileHeader {
//	my ($fn) = @_;
//	my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks) = stat($fn);
//	my $header = $cgi->header(-status=>'200 OK',-type=>getMIMEType($fn),
//				-Content_Length=>$size,	
//				-ETag=>getETag($fn),
//				-Last_Modified=>strftime("%a, %d %b %Y %T GMT" ,gmtime($mtime)),
//				-charset=>$CHARSET);
//
//	$header = "MS-Author-Via: DAV\r\n$header";
//	$header = "DAV: $DAV\r\n$header";
//	$header = "Translate: f\r\n$header" if defined $cgi->http('Translate');
//	print $header;
//
//}
//sub is_hidden {
//	my ($fn) = @_;
//	if ($#HIDDEN>-1) {
//		my $regex = '('.join('|',@HIDDEN).')';
//		return $fn=~/$regex/?1:0;
//	} else {
//		return 0;
//	}
//}
//sub simpleXMLParser {
//	my ($text,$keepRoot) = @_;
//	my %param;
//	$param{NSExpand}=1;
//	$param{KeepRoot}=1 if $keepRoot;
//	return XMLin($text,%param);
//}
//sub isLockedRecurse {
//	my ($fn) = @_;
//	$fn = $PATH_TRANSLATED unless defined $fn;
//
//	my $rows = db_getLike("$fn\%");
//
//	return $#{$rows} >-1;
//
//}
//sub isLocked {
//	my ($fn) = @_;
//	$fn.='/' if -d $fn && $fn !~/\/$/;
//	my $rows = db_get($fn);
//	return ($#{$rows}>-1)?1:0;
//}
//sub isLockable  { # check lock and exclusive
//	my ($fn,$xmldata) = @_;
//	my @lockscopes = keys %{$$xmldata{'{DAV:}lockscope'}};
//	my $lockscope = @lockscopes && $#lockscopes >-1 ? $lockscopes[0] : 'exclusive';
//
//	my $rowsRef;
//	if (! -e $fn) {
//		$rowsRef = db_get(dirname($fn).'/');
//	} elsif (-d $fn) {
//		$rowsRef = db_getLike("$fn\%");
//	} else {
//		$rowsRef = db_get($fn);
//	}
//	my $ret = 0;
//	debug("isLockable: $#{$rowsRef}, lockscope=$lockscope");
//	if ($#{$rowsRef}>-1) {
//		my $row = $$rowsRef[0];
//		$ret =  lc($$row[3]) ne 'exclusive' && $lockscope ne '{DAV:}exclusive'?1:0;
//	} else {
//		$ret = 1;
//	}
//	return $ret;
//}
//sub getLockDiscovery {
//	my ($fn) = @_;
//
//	my $rowsRef = db_get($fn);
//	my @resp = ();
//	if ($#$rowsRef > -1) {
//		debug("getLockDiscovery: rowcount=".$#{$rowsRef});
//		foreach my $row (@{$rowsRef}) { # basefn,fn,type,scope,token,depth,timeout,owner
//			my %lock;
//			$lock{locktype}{$$row[2]}=undef;
//			$lock{lockscope}{$$row[3]}=undef;
//			$lock{locktoken}{href}=$$row[4];
//			$lock{depth}=$$row[5];
//			$lock{timeout}= defined $$row[6] ? $$row[6] : 'Infinite';
//			$lock{owner}=$$row[7] if defined $$row[7];
//
//			push @resp, {activelock=>\%lock};
//		}
//
//	}
//	debug("getLockDiscovery: resp count=".$#resp);
//	
//	return $#resp >-1 ? \@resp : undef;
//}
//sub lockResource {
//	my ($fn, $ru, $xmldata, $depth, $timeout, $token, $base, $visited) =@_;
//	my %resp = ();
//	my @prop= ();
//
//	debug("lockResource(fn=$fn,ru=$ru,depth=$depth,timeout=$timeout,token=$token,base=$base)");
//
//	my %activelock = ();
//	my @locktypes = keys %{$$xmldata{'{DAV:}locktype'}};
//	my @lockscopes = keys %{$$xmldata{'{DAV:}lockscope'}};
//	my $locktype= $#locktypes>-1 ? $locktypes[0] : undef;
//	my $lockscope = $#lockscopes>-1 ? $lockscopes[0] : undef;
//	my $owner = createXML(defined $$xmldata{'{DAV:}owner'} ?  $$xmldata{'{DAV:}owner'} : $DEFAULT_LOCK_OWNER, 0, 1);
//	$locktype=~s/{[^}]+}//;
//	$lockscope=~s/{[^}]+}//;
//
//	$activelock{locktype}{$locktype}=undef;
//	$activelock{lockscope}{$lockscope}=undef;
//	$activelock{locktoken}{href}=$token;
//	$activelock{depth}=$depth;
//	$activelock{lockroot}=$ru;
//
//	# save lock to database (structure: basefn, fn, type, scope, token, timeout(null), owner(null)):
//	if (db_insert(defined $base?$base:$fn,$fn,$locktype,$lockscope,$token,$depth,$timeout, $owner))  {
//		push @prop, { activelock=> \%activelock };
//	} elsif (db_update(defined $base?$base:$fn,$fn,$timeout)) {
//		push @prop, { activelock=> \%activelock };
//	} else {
//		my $n = $#{$resp{multistatus}{response}} +1;
//		$resp{multistatus}{response}[$n]{href}=$ru;
//		$resp{multistatus}{response}[$n]{status}='HTTP/1.1 403 Forbidden';
//	}
//	my $nfn = File::Spec::Link->full_resolve($fn);
//	return \%resp if exists $$visited{$nfn};
//	$$visited{$nfn}=1;
//
//	if (-d $fn && (lc($depth) eq 'infinity' || $depth>0)) {
//		debug("lockResource: depth=$depth");
//		if (opendir(DIR,$fn)) {
//
//			foreach my $f ( grep { !/^(\.|\.\.)$/ } readdir(DIR)) {
//				my $nru = $ru.$f;
//				my $nfn = $fn.$f;
//				$nru.='/' if -d $nfn;
//				$nfn.='/' if -d $nfn;
//				debug("lockResource: $nfn, $nru");
//				my $subreqresp = lockResource($nfn, $nru, $xmldata, lc($depth) eq 'infinity'?$depth:$depth-1, $timeout, $token, defined $base?$base:$fn, $visited);
//				if (defined $$subreqresp{multistatus}) {
//					push @{$resp{multistatus}{response}}, @{$$subreqresp{multistatus}{response}};
//				} else {
//					push @prop, @{$$subreqresp{prop}{lockdiscovery}} if exists $$subreqresp{prop};
//				}
//			}
//			closedir(DIR);
//		} else {
//			my $n = $#{$resp{multistatus}{response}} +1;
//			$resp{multistatus}{response}[$n]{href}=$ru;
//			$resp{multistatus}{response}[$n]{status}='HTTP/1.1 403 Forbidden';
//		}
//	}
//	$resp{multistatus}{response}[$#{$resp{multistatus}{response}} +1]{propstat}{prop}{lockdiscovery}=\@prop if defined $resp{multistatus} && $#prop>-1;
//	$resp{prop}{lockdiscovery}=\@prop unless defined $resp{multistatus};
//	
//	return \%resp;
//}
//sub unlockResource {
//	my ($fn, $token) = @_;
//	return db_isRootFolder($fn, $token) && db_delete($fn,$token);
//}
//sub preConditionFailed {
//	my ($fn) = @_;
//	$fn = dirname($fn).'/' if ! -e $fn;
//	my $ifheader = getIfHeaderComponents($cgi->http('If'));
//	my $rowsRef = db_get( $fn );
//	my $t =0; # token found
//	my $nnl = 0; # not no-lock found
//	my $nl = 0; # no-lock found
//	my $e = 0; # wrong etag found
//	my $etag = getETag($fn);
//	foreach my $ie (@{$$ifheader{list}}) {
//		debug(" - ie{token}=".$$ie{token});
//		if ($$ie{token} =~ /Not\s+<DAV:no-lock>/i) {
//			$nnl = 1;
//		}elsif ($$ie{token} =~ /<DAV:no-lock>/i) {
//			$nl = 1;
//		}elsif ($$ie{token} =~ /opaquelocktoken/i) {
//			$t = 1;
//		}
//		if (defined $$ie{etag}) { 
//			$e= ($$ie{etag} ne $etag)?1:0;
//		}
//	}
//	debug("checkPreCondition: t=$t, nnl=$nnl, e=$e, nl=$nl");
//	return  ($t & $nnl & $e) | $nl;
//
//}
//sub isAllowed {
//	my ($fn, $recurse) = @_;
//	
//	$fn = dirname($fn).'/' if ! -e $fn;
//	debug("isAllowed($fn,$recurse) called.");
//
//	return 1 unless $ENABLE_LOCK;
//	
//	my $ifheader = getIfHeaderComponents($cgi->http('If'));
//	my $rowsRef = $recurse ? db_getLike("$fn%") : db_get( $fn );
//
//	return 0 if -e $fn &&  ! -w $fn; # not writeable
//	return 1 if $#{$rowsRef}==-1; # no lock
//	return 0 unless defined $ifheader;
//	my $ret = 0;
//	for (my $i=0; $i<=$#{$rowsRef}; $i++) {
//		for (my $j=0; $j<=$#{$$ifheader{list}}; $j++) {
//			my $iftoken = $$ifheader{list}[$j]{token};
//			$iftoken="" unless defined $iftoken;
//			$iftoken=~s/[\<\>\s]+//g; 
//			debug("isAllowed: $iftoken send, needed for $$rowsRef[$i][4]: ". ($iftoken eq $$rowsRef[$i][4]?"OK":"FAILED") );
//			if ($$rowsRef[$i][4] eq $iftoken) {
//				$ret = 1;
//				last;
//			}
//		}
//	}
//	return $ret;
//}
//sub inheritLock {
//	my ($fn,$checkContent, $visited) = @_;
//	$fn =  $PATH_TRANSLATED unless defined $fn;
//
//	my $nfn = File::Spec::Link->full_resolve($fn);
//	return if exists $$visited{$nfn};
//	$$visited{$nfn}=1;
//
//	my $bfn = dirname($fn).'/';
//
//	debug("inheritLock: check lock for $bfn ($fn)");
//	my $rows = db_get($bfn);
//	return if $#{$rows} == -1 and !$checkContent;
//	debug("inheritLock: $bfn is locked") if $#{$rows}>-1;
//	if ($checkContent) {
//		$rows = db_get($fn);
//		return if $#{$rows} == -1;
//		debug("inheritLock: $fn is locked");
//	}
//	my $row = $$rows[0];
//	if (-d $fn) {
//		debug("inheritLock: $fn is a collection");
//		db_insert($$row[0],$fn,$$row[2],$$row[3],$$row[4],$$row[5],$$row[6],$$row[7]);
//		if (opendir(DIR,$fn)) {
//			foreach my $f (grep { !/^(\.|\.\.)$/ } readdir(DIR)) {
//				my $full = $fn.$f;
//				$full .='/' if -d $full && $full !~/\/$/;
//				db_insert($$row[0],$full,$$row[2],$$row[3],$$row[4],$$row[5],$$row[6],$$row[7]);
//				inheritLock($full,undef,$visited);
//			}
//			closedir(DIR);
//		}
//	} else {
//		db_insert($$row[0],$fn,$$row[2],$$row[3],$$row[4],$$row[5],$$row[6],$$row[7]);
//	}
//}
//sub getIfHeaderComponents {
//        my($if) = @_;
//        my($rtag,@tokens);
//	if (defined $if) {
//		if ($if =~ s/^<([^>]+)>\s*//) {
//			$rtag=$1;
//		}
//		while ($if =~ s/^\((Not\s*)?([^\[\)]+\s*)?\s*(\[([^\]\)]+)\])?\)\s*//i) {
//			push @tokens, { token=>"$1$2", etag=>$4 };
//		}
//		return {rtag=>$rtag, list=>\@tokens};
//	}
//	return undef;
//}
//sub readDirRecursive {
//	my ($fn, $ru, $respsRef, $props, $all, $noval, $depth, $noroot, $visited) = @_;
//	return if is_hidden($fn);
//	my $nfn = File::Spec::Link->full_resolve($fn);
//	unless ($noroot) {
//		my %response = ( href=>$ru );
//		$response{href}=$ru;
//		$response{propstat}=getPropStat($nfn,$ru,$props,$all,$noval);
//		if ($#{$response{propstat}}==-1) {
//			$response{status} = 'HTTP/1.1 200 OK';
//			delete $response{propstat};
//		} else {
//			$response{propstat}[0]{status} = 'HTTP/1.1 208 Already Reported' if $ENABLE_BIND && $depth<0 && exists $$visited{$nfn};
//		}
//		push @{$respsRef}, \%response;
//	}
//	return if exists $$visited{$nfn} && !$noroot && ($depth eq 'infinity' || $depth<0);
//	$$visited{$nfn} = 1;
//	if ($depth!=0 &&  -d $nfn ) {
//		if ((!defined $FILECOUNTPERDIRLIMIT{$fn} || $FILECOUNTPERDIRLIMIT{$fn}>0) && opendir(DIR, $nfn)) {
//			my $count = 0;
//			my @rfiles;
//			while ( my $f = readdir(DIR) ) {
//				next if is_hidden("$fn/$f");
//				next if $f =~ /^(\.|\.\.)$/;
//				next if defined $FILEFILTERPERDIR{$fn} && "$fn/$f" !~ $FILEFILTERPERDIR{$fn};
//				last if (defined $FILECOUNTPERDIRLIMIT{$fn} && $count >= $FILECOUNTPERDIRLIMIT{$fn}) || (!defined $FILECOUNTPERDIRLIMIT{$fn} && defined $FILECOUNTLIMIT && $count >= $FILECOUNTLIMIT);
//				$count++;
//				push @rfiles, $f;
//			}
//			foreach my $f ( sort cmp_files @rfiles ) {
//				next if is_hidden("$nfn/$f");
//				my $fru=$ru.$cgi->escape($f);
//				$fru.='/' if -d "$nfn/$f" && $fru!~/\/$/;
//				my $nnfn = File::Spec::Link->full_resolve("$nfn/$f");
//				readDirRecursive($nnfn, $fru, $respsRef, $props, $all, $noval, $depth>0?$depth-1:$depth, 0, $visited);
//			}
//			closedir(DIR);
//		}
//	}
//}
//sub db_isRootFolder {
//	my ($fn, $token) = @_;
//	my $rows =  [];
//	my $dbh = db_init();
//	my $sth = $dbh->prepare('SELECT basefn,fn,type,scope,token,depth,timeout,owner FROM webdav_locks WHERE fn = ? AND basefn = ? AND token = ?');
//	if (defined $sth) {
//		$sth->execute($fn, $fn, $token);
//		$rows = $sth->fetchall_arrayref();
//	}
//	return $#{$rows}>-1;
//}
//sub db_getLike {
//	my ($fn) = @_;
//	my $rows;
//	my $dbh = db_init();
//	my $sth = $dbh->prepare('SELECT basefn,fn,type,scope,token,depth,timeout,owner FROM webdav_locks WHERE fn like ?');
//	if (defined $sth) {
//		$sth->execute($fn);
//		$rows = $sth->fetchall_arrayref();
//	}
//	return $rows;
//}
//sub db_get {
//	my ($fn,$token) = @_;
//	my $rows;
//	my $dbh = db_init();
//	my $sel = 'SELECT basefn,fn,type,scope,token,depth,timeout,owner FROM webdav_locks WHERE fn = ?';
//	my @params;
//	push @params, $fn;
//	if (defined $token) {
//		$sel .= ' AND token = ?';
//		push @params, $token;
//	}
//	
//	my $sth = $dbh->prepare($sel);
//	if (defined $sth) {
//		$sth->execute(@params);
//		$rows = $sth->fetchall_arrayref();
//	}
//	return $rows;
//}
//sub db_insertProperty {
//	my ($fn, $propname, $value) = @_;
//	my $ret = 0;
//	debug("db_insertProperty($fn, $propname, $value)");
//	my $dbh = db_init();
//	my $sth = $dbh->prepare('INSERT INTO webdav_props (fn, propname, value) VALUES ( ?,?,?)');
//	if (defined  $sth) {
//		$sth->execute($fn, $propname, $value);
//		$ret = ($sth->rows >0)?1:0;
//		$dbh->commit();
//		$CACHE{Properties}{$fn}{$propname}=$value;
//	}
//	return $ret;
//}
//sub db_updateProperty {
//	my ($fn, $propname, $value) = @_;
//	my $ret = 0;
//	debug("db_updateProperty($fn, $propname, $value)");
//	my $dbh = db_init();
//	my $sth = $dbh->prepare('UPDATE webdav_props SET value = ? WHERE fn = ? AND propname = ?');
//	if (defined  $sth) {
//		$sth->execute($value, $fn, $propname);
//		$ret=($sth->rows>0)?1:0;
//		$dbh->commit();
//		$CACHE{Properties}{$fn}{$propname}=$value;
//	}
//	return $ret;
//}
//sub db_moveProperties {
//	my($src,$dst) = @_;
//	my $dbh = db_init();
//	my $sth = $dbh->prepare('UPDATE webdav_props SET fn = ? WHERE fn = ?');
//	my $ret = 0;
//	if (defined $sth) {
//		$sth->execute($dst,$src);
//		$ret = ($sth->rows>0)?1:0;
//		$dbh->commit();
//		delete $CACHE{Properties}{$src};
//	}
//	return $ret;
//}
//sub db_copyProperties {
//	my($src,$dst) = @_;
//	my $dbh = db_init();
//	my $sth = $dbh->prepare('INSERT INTO webdav_props (fn,propname,value) SELECT ?, propname, value FROM webdav_props WHERE fn = ?');
//	my $ret = 0;
//	if (defined $sth) {
//		$sth->execute($dst,$src);
//		$ret = ($sth->rows>0)?1:0;
//		$dbh->commit();
//	}
//	return $ret;
//}
//sub db_deleteProperties {
//	my($fn) = @_;
//	my $dbh = db_init();
//	my $sth = $dbh->prepare('DELETE FROM webdav_props WHERE fn = ?');
//	my $ret = 0;
//	if (defined $sth) {
//		$sth->execute($fn);
//		$ret = ($sth->rows>0)?1:0;
//		$dbh->commit();
//		delete $CACHE{Properties}{$fn};
//	}
//	return $ret;
//	
//}
//sub db_getProperties {
//	my ($fn) = @_;
//	return $CACHE{Properties}{$fn} if exists $CACHE{Properties}{$fn} || $CACHE{Properties_flag}{$fn}; 
//	my $dbh = db_init();
//	my $sth = $dbh->prepare('SELECT fn, propname, value FROM webdav_props WHERE fn like ?');
//	if (defined $sth) {
//		$sth->execute("$fn\%");
//		if (!$sth->err) {
//			my $rows = $sth->fetchall_arrayref();
//			foreach my $row (@{$rows}) {
//				$CACHE{Properties}{$$row[0]}{$$row[1]}=$$row[2];
//			}
//			$CACHE{Properties_flag}{$fn}=1;
//		}
//	}
//	return $CACHE{Properties}{$fn};
//}
//sub db_getProperty {
//	my ($fn,$propname) = @_;
//	debug("db_getProperty($fn, $propname)");
//	my $props = db_getProperties($fn);
//	return $$props{$propname};
//}
//sub db_removeProperty {
//	my ($fn, $propname) = @_;
//	debug("db_removeProperty($fn,$propname)");
//	my $dbh = db_init();
//	my $sth = $dbh->prepare('DELETE FROM webdav_props WHERE fn = ? AND propname = ?');
//	my $ret = 0;
//	if (defined $sth) {
//		$sth->execute($fn, $propname);
//		$ret = ($sth->rows >0)?1:0;
//		$dbh->commit();
//		delete $CACHE{Properties}{$fn}{$propname};
//	}
//	return $ret;
//}
//sub db_insert {
//	my ($basefn, $fn, $type, $scope, $token, $depth, $timeout, $owner) = @_;
//	debug("db_insert($basefn,$fn,$type,$scope,$token,$depth,$timeout,$owner)");
//	my $ret = 0;
//	my $dbh = db_init();
//	my $sth = $dbh->prepare('INSERT INTO webdav_locks (basefn, fn, type, scope, token, depth, timeout, owner) VALUES ( ?,?,?,?,?,?,?,?)');
//	if (defined $sth) {
//		$sth->execute($basefn,$fn,$type,$scope,$token,$depth,$timeout,$owner);
//		$ret=($sth->rows>0)?1:0;
//		$dbh->commit();
//	}
//	return $ret;
//}
//sub db_update {
//	my ($basefn, $fn, $timeout) = @_;
//	debug("db_update($basefn,$fn,$timeout)");
//	my $ret = 0;
//	my $dbh = db_init();
//	my $sth = $dbh->prepare('UPDATE webdav_locks SET timeout=? WHERE basefn = ? AND fn = ?' );
//	if (defined $sth) {
//		$sth->execute($timeout, $basefn, $fn);
//		$ret = ($sth->rows>0)?1:0;
//		$dbh->commit();
//	}
//	return $ret;
//}
//sub db_delete {
//	my ($fn,$token) = @_;
//	my $ret = 0;
//	my $dbh = db_init();
//	debug("db_delete($fn,$token)");
//	my $sel = 'DELETE FROM webdav_locks WHERE ( basefn = ? OR fn = ? )';
//	my @params = ($fn, $fn);
//	if (defined $token) {
//		$sel.=' AND token = ?';
//		push @params, $token;
//	}
//	my $sth = $dbh->prepare($sel);
//	if (defined $sth) {
//		$sth->execute(@params);
//		debug("db_delete: rows=".$sth->rows);
//		$ret = $sth->rows>0?1:0;
//		$dbh->commit();
//	}
//	
//	return $ret;
//}
//sub db_init {
//	return $DBI_INIT if defined $DBI_INIT;
//
//	my $dbh = DBI->connect($DBI_SRC, $DBI_USER, $DBI_PASS, { RaiseError=>0, PrintError=>0, AutoCommit=>0 });
//	if (defined $dbh && $CREATE_DB) {
//		debug("db_init: CREATE TABLE/INDEX...");
//
//		foreach my $query (@DB_SCHEMA) {
//			my $sth = $dbh->prepare($query);
//			if (defined $sth) {
//				$sth->execute();
//				if ($sth->err) {
//					debug("db_init: '$query' execution failed!");
//					$dbh=undef;
//				} else {
//					$dbh->commit();
//					debug("db_init: '$query' done.");
//				}	
//			} else {
//				debug("db_init: '$query' preparation failed!");
//			}
//		}
//	}
//	$DBI_INIT = $dbh;
//	return $dbh;
//}
//sub db_rollback($) {
//	my ($dbh) = @_;
//	$dbh->rollback();
//}
//sub db_commit($) {
//	my ($dbh) = @_;
//	$dbh->commit();
//}
//sub handlePropertyRequest {
//	my ($xml, $dataRef, $resp_200, $resp_403) = @_;
//
//	if (ref($$dataRef{'{DAV:}remove'}) eq 'ARRAY') {
//		foreach my $remove (@{$$dataRef{'{DAV:}remove'}}) {
//			foreach my $propname (keys %{$$remove{'{DAV:}prop'}}) {
//				removeProperty($propname, $$remove{'{DAV:}prop'}, $resp_200, $resp_403);
//			}
//		}
//	} elsif (ref($$dataRef{'{DAV:}remove'}) eq 'HASH') {
//		foreach my $propname (keys %{$$dataRef{'{DAV:}remove'}{'{DAV:}prop'}}) {
//			removeProperty($propname, $$dataRef{'{DAV:}remove'}{'{DAV:}prop'}, $resp_200, $resp_403);
//		}
//	}
//	if ( ref($$dataRef{'{DAV:}set'}) eq 'ARRAY' )  {
//		foreach my $set (@{$$dataRef{'{DAV:}set'}}) {
//			foreach my $propname (keys %{$$set{'{DAV:}prop'}}) {
//				setProperty($propname, $$set{'{DAV:}prop'}, $resp_200, $resp_403);
//			}
//		}
//	} elsif (ref($$dataRef{'{DAV:}set'}) eq 'HASH') {
//		my $lastmodifiedprocessed = 0;
//		foreach my $propname (keys %{$$dataRef{'{DAV:}set'}{'{DAV:}prop'}}) {
//			if ($propname eq '{DAV:}getlastmodified' || $propname eq '{urn:schemas-microsoft-com:}Win32LastModifiedTime' ) {
//				next if $lastmodifiedprocessed;
//				$lastmodifiedprocessed = 1;
//			}
//			setProperty($propname, $$dataRef{'{DAV:}set'}{'{DAV:}prop'},$resp_200, $resp_403);
//		}
//	} 
//	if ($xml =~ /<([^:]+:)?set[\s>]+.*<([^:]+:)?remove[\s>]+/s) { ## fix parser bug: set/remove|remove/set of the same prop
//		if (ref($$dataRef{'{DAV:}remove'}) eq 'ARRAY') {
//			foreach my $remove (@{$$dataRef{'{DAV:}remove'}}) {
//				foreach my $propname (keys %{$$remove{'{DAV:}prop'}}) {
//					removeProperty($propname, $$remove{'{DAV:}prop'}, $resp_200, $resp_403);
//				}
//			}
//		} elsif (ref($$dataRef{'{DAV:}remove'}) eq 'HASH') {
//			foreach my $propname (keys %{$$dataRef{'{DAV:}remove'}{'{DAV:}prop'}}) {
//				removeProperty($propname, $$dataRef{'{DAV:}remove'}{'{DAV:}prop'}, $resp_200, $resp_403);
//			}
//		}
//	}
//}
//sub setProperty {
//	my ($propname, $elementParentRef, $resp_200, $resp_403) = @_;
//	my $fn = $PATH_TRANSLATED;
//	my $ru = $REQUEST_URI;
//	$propname=~/^{([^}]+)}(.*)$/;
//	my ($ns,$pn) = ($1,$2);
//	debug("setProperty: $propname (ns=$ns, pn=$pn)");
//	
//	if ($propname eq '{http://apache.org/dav/props/}executable') {
//		my $executable = $$elementParentRef{$propname}{'content'};
//		if (defined $executable) {
//			my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks) = stat($fn);
//			chmod( ($executable=~/F/) ? $mode & 0666 : $mode | 0111, $fn);
//			$$resp_200{href}=$ru;
//			$$resp_200{propstat}{prop}{executable}=$executable;
//			$$resp_200{propstat}{status}='HTTP/1.1 200 OK';
//		}
//	} elsif (($propname eq '{DAV:}getlastmodified')||($propname eq '{urn:schemas-microsoft-com:}Win32LastModifiedTime')
//			||($propname eq '{urn:schemas-microsoft-com:}Win32LastAccessTime')
//			||($propname eq '{urn:schemas-microsoft-com:}Win32CreationTime')) {
//		my $getlastmodified = $$elementParentRef{'{DAV:}getlastmodified'};
//		$getlastmodified = $$elementParentRef{'{urn:schemas-microsoft-com:}Win32LastModifiedTime'} if !defined $getlastmodified;
//		my $lastaccesstime =$$elementParentRef{'{urn:schemas-microsoft-com:}Win32LastAccessTime'};
//		if (defined $getlastmodified) {
//			my $mtime = str2time($getlastmodified);
//			my $atime = defined $lastaccesstime ? str2time($lastaccesstime) : $mtime;
//			utime($atime,$mtime,$fn);
//			$$resp_200{href}=$ru;
//			$$resp_200{propstat}{prop}{getlastmodified}=$getlastmodified if defined  $$elementParentRef{'{DAV:}getlastmodified'};
//			$$resp_200{propstat}{prop}{Win32LastModifiedTime}=$getlastmodified if $$elementParentRef{'{urn:schemas-microsoft-com:}Win32LastModifiedTime'};
//			$$resp_200{propstat}{prop}{Win32LastAccessTime}=$lastaccesstime if $$elementParentRef{'{urn:schemas-microsoft-com:}Win32LastAccessTime'};
//			$$resp_200{propstat}{prop}{Win32CreationTime}=$$elementParentRef{'{urn:schemas-microsoft-com:}Win32CreationTime'} if defined $$elementParentRef{'{urn:schemas-microsoft-com:}Win32CreationTime'};
//			$$resp_200{propstat}{status}='HTTP/1.1 200 OK';
//		} 
//	} elsif ($propname eq '{urn:schemas-microsoft-com:}Win32FileAttributes') {
//		$$resp_200{href}=$ru;
//		$$resp_200{propstat}{prop}{Win32FileAttributes}=undef;
//		$$resp_200{propstat}{status}='HTTP/1.1 200 OK';
//	} elsif (defined $NAMESPACES{$ns} && grep(/^\Q$pn\E$/,@PROTECTED_PROPS)>0) {
//		$$resp_403{href}=$ru;
//		$$resp_403{propstat}{prop}{$propname}=undef;
//		$$resp_403{propstat}{status}='HTTP/1.1 403 Forbidden';
//	} else {
//		my $n = $propname;
//		$n='{}'.$n if (ref($$elementParentRef{$propname}) eq 'HASH' && $$elementParentRef{$propname}{xmlns} eq "" && $n!~/^{[^}]*}/);
//		my $dbval = db_getProperty($fn, $n);
//		my $value = createXML($$elementParentRef{$propname},0);
//		my $ret = defined $dbval ? db_updateProperty($fn, $n, $value) : db_insertProperty($fn, $n, $value);
//		if ($ret) {
//			$$resp_200{href}=$ru;
//			$$resp_200{propstat}{prop}{$propname}=undef;
//			$$resp_200{propstat}{status}='HTTP/1.1 200 OK';
//		} else {
//			debug("Cannot set property '$propname'");
//			$$resp_403{href}=$ru;
//			$$resp_403{propstat}{prop}{$propname}=undef;
//			$$resp_403{propstat}{status}='HTTP/1.1 403 Forbidden';
//			
//		}
//	}
//}
//sub rcopy {
//	my ($src,$dst,$move) = @_;
//	# src exists and readable?
//	return 0 if ! -e $src || ! -r $src;
//
//	# dst writeable?
//	return 0 if -e $dst && ! -w $dst;
//
//	my $nsrc = $src;
//	$nsrc =~ s/\/$//; ## remove trailing slash for link test (-l)
//	
//	if ( -l $nsrc) { # link
//		if (!$move || !rename($nsrc, $dst)) {
//			my $orig = readlink($nsrc);
//			return 0 if ( !$move || unlink($nsrc) ) && !symlink($orig,$dst); 
//		}
//	} elsif ( -f $src ) { # file
//		if (-d $dst) {
//			$dst.='/' if $dst !~/\/$/;
//			$dst.=basename($src);
//		}
//		if (!$move || !rename($src,$dst)) {
//			return 0 unless open(SRC,"<$src");
//			return 0 unless open(DST,">$dst");
//			my $buffer;
//			while (read(SRC,$buffer,$BUFSIZE)>0) {
//				print DST $buffer;
//			}
//
//			close(SRC);
//			close(DST);
//			if ($move) {
//				return 0 if !-w $src;
//				return 0 unless unlink($src);
//			}
//		}
//	} elsif ( -d $src ) {
//		# cannot write folders to files:
//		return 0 if -f $dst;
//
//		$dst.='/' if $dst !~ /\/$/;
//		$src.='/' if $src !~ /\/$/;
//
//		if (!$move || getDirInfo($src,'realchildcount')>0 || !rename($src,$dst)) {
//			mkdir $dst unless -e $dst;
//
//			return 0 unless opendir(SRC,$src);
//			foreach my $filename (grep { !/^\.{1,2}$/ } readdir(SRC)) {
//				rcopy($src.$filename, $dst.$filename, $move);
//			}
//			closedir(SRC);
//			if ($move) {
//				return 0 if !-w $src;
//				return 0 unless rmdir($src);
//			}
//		}
//	} else {
//		return 0;
//	}
//	db_deleteProperties($dst);
//	db_copyProperties($src,$dst);
//	db_deleteProperties($src) if $move;
//	
//	return 1;
//}
//sub rmove {
//	my ($src, $dst) = @_;
//	return rcopy($src, $dst, 1);
//}
//sub getQuota {
//	my ($fn) = @_;
//	$fn = $PATH_TRANSLATED unless defined $fn;
//	return ($CACHE{getQuota}{$fn}{block_hard}, $CACHE{getQuota}{$fn}{block_curr}) if defined $CACHE{getQuota}{$fn}{block_hard};
//
//	my ($block_curr, $block_soft, $block_hard, $block_timelimit,
//            $inode_curr, $inode_soft, $inode_hard, $inode_timelimit);
//	if (defined $GFSQUOTA && open(QCMD,"$GFSQUOTA \"$fn\"|")) {
//		my @lines = <QCMD>;
//		my @vals = split(/\s+/,$lines[0]);
//		($block_hard,$block_curr) = ($vals[3] * 1048576, $vals[7] * 1048576);
//		close(QCMD);
//	} else {
//		($block_curr, $block_soft, $block_hard, $block_timelimit,
//		    $inode_curr, $inode_soft, $inode_hard, $inode_timelimit) = Quota::query(Quota::getqcarg($fn));
//	}
//	$CACHE{getQuota}{$fn}{block_hard}=$block_hard;
//	$CACHE{getQuota}{$fn}{block_curr}=$block_curr;
//	return ($block_hard,$block_curr);
//}
//sub getuuid {
//	my ($fn) = @_;
//	my $uuid = new OSSP::uuid;
//	my $uuid_ns = new OSSP::uuid;
//	$uuid_ns->load("opaquelocktoken:$fn");
//	$uuid->make("v3", $uuid_ns, "$fn".time());
//	return $uuid->export("str");
//}
//sub getDirInfo {
//	my ($fn, $prop) = @_;
//	return $CACHE{getDirInfo}{$fn}{$prop} if defined $CACHE{getDirInfo}{$fn}{$prop};
//	my %counter = ( childcount=>0, visiblecount=>0, objectcount=>0, hassubs=>0 );
//	if (opendir(DIR,$fn)) {
//		foreach my $f ( grep { !/^\.{1,2}$/ } readdir(DIR)) {
//			$counter{realchildcount}++;
//			if (!is_hidden("$fn/$f")) {
//				$counter{childcount}++;
//				$counter{visiblecount}++ if !-d "$fn/$f" && $f !~/^\./;
//				$counter{objectcount}++ if !-d "$fn/$f";
//			}
//		}
//		closedir(DIR);
//	}
//	$counter{hassubs} = ($counter{childcount}-$counter{objectcount} > 0 )? 1:0;
//
//	foreach my $k (keys %counter) {
//		$CACHE{getDirInfo}{$fn}{$k}=$counter{$k};
//	}
//	return $counter{$prop};
//}
//sub getACLSupportedPrivilegeSet {
//	return { 'supported-privilege' =>
//			{ 
//				privilege => { all => undef }, 
//				abstract => undef,
//				description=>'Any operation',
//				'supported-privilege' => [ 
//					{
//						privilege => { read =>  undef },
//						description => 'Read any object',
//						'supported-privilege' => [
//							{
//								privilege => { 'read-acl' => undef },
//								absract => undef,
//								description => 'Read ACL',
//							},
//							{
//								privilege => { 'read-current-user-privilege-set' => undef },
//								absract => undef,
//								description => 'Read current user privilege set property',
//							},
//							{	privilege => { 'read-free-busy' },
//								abstract => undef,
//								description => 'Read busy time information'
//							},
//						],
//					},
//					{
//						privilege => { write => undef },
//						description => 'Write any object',
//						'supported-privilege' => [
//							{
//								privilege => { 'write-acl' => undef },
//								abstract => undef,
//								description => 'Write ACL',
//							},
//							{
//								privilege => { 'write-properties' => undef },
//								abstract => undef,
//								description => 'Write properties',
//							},
//							{
//								privilege => { 'write-content' => undef },
//								abstract => undef,
//								description => 'Write resource content',
//							},
//						],
//
//					},
//					{
//						privilege => {unlock => undef},
//						abstract => undef,
//						description => 'Unlock resource',
//					},
//					{
//						privilege => {bind => undef},
//						abstract => undef,
//						description => 'Add new files/folders',
//					},
//					{
//						privilege => {unbind => undef},
//						abstract => undef,
//						description => 'Delete or move files/folders',
//					},
//				],
//			}
//	};
//}
//sub getACLCurrentUserPrivilegeSet {
//	my ($fn) = @_;
//
//	my $usergrant;
//	if (-r $fn) {
//		push @{$$usergrant{privilege}},{read  => undef };
//		push @{$$usergrant{privilege}},{'read-acl'  => undef };
//		push @{$$usergrant{privilege}},{'read-current-user-privilege-set'  => undef };
//		if (-w $fn) {
//			push @{$$usergrant{privilege}},{write => undef };
//			push @{$$usergrant{privilege}},{'write-acl' => undef };
//			push @{$$usergrant{privilege}},{'write-content'  => undef };
//			push @{$$usergrant{privilege}},{'write-properties'  => undef };
//			push @{$$usergrant{privilege}},{bind=> undef };
//			push @{$$usergrant{privilege}},{unbind=> undef };
//		}
//	}
//
//	return $usergrant;
//}
//sub getACLProp {
//	my ($mode) = @_;
//	my @ace;
//
//	my $ownergrant;
//	my $groupgrant;
//	my $othergrant;
//
//	$mode = $mode & 07777;
//
//	push @{$$ownergrant{privilege}},{read  => undef } if ($mode & 0400) == 0400;
//	push @{$$ownergrant{privilege}},{write => undef } if ($mode & 0200) == 0200;
//	push @{$$ownergrant{privilege}},{bind => undef } if ($mode & 0200) == 0200;
//	push @{$$ownergrant{privilege}},{unbind => undef } if ($mode & 0200) == 0200;
//	push @{$$groupgrant{privilege}},{read  => undef } if ($mode & 0040) == 0040;
//	push @{$$groupgrant{privilege}},{write => undef } if ($mode & 0020) == 0020;
//	push @{$$groupgrant{privilege}},{bind => undef } if ($mode & 0020) == 0020;
//	push @{$$groupgrant{privilege}},{unbind => undef } if ($mode & 0020) == 0020;
//	push @{$$othergrant{privilege}},{read  => undef } if ($mode & 0004) == 0004;
//	push @{$$othergrant{privilege}},{write => undef } if ($mode & 0002) == 0002;
//	push @{$$othergrant{privilege}},{bind => undef } if ($mode & 0002) == 0002;
//	push @{$$othergrant{privilege}},{unbind => undef } if ($mode & 0002) == 0002;
//	
//	push @ace, { principal => { property => { owner => undef } },
//		     grant => $ownergrant
//                   };
//	push @ace, { principal => { property => { owner => undef } },
//	             deny => { privilege => { all => undef } }
//	           };
//
//	push @ace, { principal => { property => { group => undef } },
//		     grant => $groupgrant
//                   };
//	push @ace, { principal => { property => { group => undef } },
//	             deny => { privilege => { all => undef } }
//	           };
//
//	push @ace, { principal => { all => undef },
//		     grant => $othergrant
//                   };
//
//	return { ace => \@ace };
//}
//sub getCalendarHomeSet {
//	my ($uri) = @_;
//	return $uri unless defined %CALENDAR_HOME_SET;
//	my $rmuser = $ENV{REDIRECT_REMOTE_USER} || $ENV{REMOTE_USER};
//	$rmuser = $< unless exists $CALENDAR_HOME_SET{$rmuser};
//	return  ( exists $CALENDAR_HOME_SET{$rmuser} ? $CALENDAR_HOME_SET{$rmuser} : $CALENDAR_HOME_SET{default} );
//}
//sub getAddressbookHomeSet {
//	my ($uri) = @_;
//	return $uri unless defined %ADDRESSBOOK_HOME_SET;
//	my $rmuser = $ENV{REDIRECT_REMOTE_USER} || $ENV{REMOTE_USER};
//	$rmuser = $< unless exists $ADDRESSBOOK_HOME_SET{$rmuser};
//	return ( exists $ADDRESSBOOK_HOME_SET{$rmuser} ? $ADDRESSBOOK_HOME_SET{$rmuser} : $ADDRESSBOOK_HOME_SET{default} );
//}
//sub getNameSpace {
//	my ($prop) = @_;
//	return defined $ELEMENTS{$prop}?$ELEMENTS{$prop}:$ELEMENTS{default};
//}
//sub getNameSpaceUri {
//	my  ($prop) = @_;
//	return $NAMESPACEABBR{getNameSpace($prop)};
//}
//sub getFileContent {
//	my ($fn) = @_;
//	debug("getFileContent($fn)");
//	my $content="";
//	if (-e $fn && !-d $fn && open(F,"<$fn")) {
//		$content = join("",<F>);
//		close(F);
//	}
//	return $content;
//}
//sub moveToTrash  {
//	my ($fn) = @_;
//
//	my $ret = 0;
//	my $etag = getETag($fn); ## get a unique name for trash folder
//	$etag=~s/\"//g;
//	my $trash = "$TRASH_FOLDER$etag/";
//
//	if ($fn =~ /^\Q$TRASH_FOLDER\E/) { ## delete within trash
//		my @err;
//		deltree($fn, \@err);
//		$ret = 1 if $#err == -1;
//		debug("moveToTrash($fn)->/dev/null = $ret");
//	} elsif (-e $TRASH_FOLDER || mkdir($TRASH_FOLDER)) {
//		if (-e $trash) {
//			my $i=0;
//			while (-e $trash) { ## find unused trash folder
//				$trash="$TRASH_FOLDER$etag".($i++).'/';
//			}
//		}
//		$ret = 1 if mkdir($trash) && rmove($fn, $trash.basename($fn));
//		debug("moveToTrash($fn)->$trash = $ret");
//	}
//	return $ret;
//}
//sub getQuickNavPath {
//	my ($ru, $query) = @_;
//	$ru = uri_unescape($ru);
//	my $content = "";
//	my $path = "";
//	foreach my $pe (split(/\//, $ru)) {
//		$path .= uri_escape($pe) . '/';
//		$path = '/' if $path eq '//';
//		$content .= $cgi->a({-href=>$path.(defined $query?"?$query":""), -title=>$path},"$pe/");
//	}
//	$content .= $cgi->a({-href=>'/', -title=>'/'}, '/') if $content eq '';
//	return $content;
//}
//sub getPageNavBar {
//	my ($ru, $count) = @_;
//	my $limit = $PAGE_LIMIT || -1;
//	my $showall = $cgi->param('showall') || 0;
//	my $order = $cgi->param('order') || 'name';
//	my $page = $cgi->param('page') || 1;
//
//	my $content = "";
//	return $content if $limit <1 || $count < $limit;
//
//	if ($showall) {
//		return $cgi->div({-style=>'font-weight: bold; font-size:0.9em;padding: 10px 0px 10px 0px'}, 
//					$cgi->a({href=>$ru."?order=$order", -style=>'text-decoration:none;'}, _tl('navpageview'))
//				);
//	}
//
//
//	my $maxpages = int($count / $limit);
//	$maxpages++ if $count % $limit > 0;
//
//	$content .= _tl('navpage')."$page/$maxpages: ";
//
//	$content .= ($page > 1 ) 
//			? $cgi->a({-href=>$ru."?order=$order;page=1", -title=>_tl('navfirsttooltip'), -style=>'text-decoration:none'}, _tl('navfirst')) 
//			: _tl('navfirstblind');
//	$content .= ($page > 1 ) 
//			? $cgi->a({-href=>$ru."?order=$order;page=".($page-1), -title=>_tl('navprevtooltip'), -style=>'text-decoration:none'}, _tl('navprev')) 
//			: _tl('navprevblind');
//
//	$content .= sprintf("%02d-%02d/%d",(($limit * ($page - 1)) + 1) , ( $page < $maxpages || $count % $limit == 0 ? $limit * $page : ($limit*($page-1)) + $count % $limit), $count);
//	
//	$content .= ($page < $maxpages) 
//			? $cgi->a({-href=>$ru."?order=$order;page=".($page+1), -title=>_tl('navnexttooltip'), -style=>'text-decoration:none'},_tl('navnext')) 
//			: _tl('navnextblind');
//
//	$content .= ($page < $maxpages) 
//			? $cgi->a({-href=>$ru."?order=$order;page=$maxpages", title=>_tl('navlasttooltip'), -style=>'text-decoration:none'},_tl('navlast')) 
//			: _tl('navlastblind');
//
//	$content .= $cgi->a({-href=>$ru."?order=$order;showall=1", -style=>'text-decoration:none', -title=>_tl('navalltooltip')}, _tl('navall'));
//
//
//	return $cgi->div({-style=>'font-weight: bold; font-size:0.9em;padding: 10px 0px 10px 0px'},$content);
//}
//sub getQueryParams {
//	# preserve query parameters
//	my @query;
//	foreach my $param (('order','showall')) {
//		push @query, $param.'='.$cgi->param($param) if defined $cgi->param($param);
//	}
//	return $#query>-1 ? join(';',@query) : undef;
//}
//sub getFolderList {
//	my ($fn,$ru,$filter) = @_;
//	my ($content,$list,$count,$filecount,$foldercount,$filesizes) = ("",0,0,0,0);
//
//	$content .= $cgi->h2( { -style=>'border:0; padding:0; margin:0'},
//				$cgi->a({-href=>"$ru?action=props"}, 
//						$cgi->img({-src=>$ICONS{'<folder>'} || $ICONS{default},-style=>'border:0',-title=>_tl('showproperties'), -alt=>'folder'})
//					)
//				.'&nbsp;'.$cgi->a({-href=>'?action=davmount',-style=>'font-size:0.8em;color:black',-title=>_tl('mounttooltip')},_tl('mount'))
//				.' '
//				.getQuickNavPath($ru, getQueryParams())
//			);
//	if ($SHOW_QUOTA) {
//		my ($ql, $qu) = getQuota($fn);
//		if (defined $ql && defined $qu) {
//			$ql=$ql/1048576; $qu=$qu/1048576;
//			$content .= $cgi->div({style=>'padding-left:30px;font-size:0.8em;'},
//							_tl('quotalimit')."${ql} MB,"
//							._tl('quotaused')."${qu} MB,"
//							._tl('quotaavailable').($ql-$qu)." MB");
//		}
//	}
//	my $row = "";
//	$list="";
//	
//	$row.=$cgi->td({-class=>'th_sel',-style=>'width:1em;'},$cgi->checkbox(-onclick=>'javascript:this.checked=false; var ea = document.getElementsByName("file"); for (var i=0; i<ea.length; i++) ea[i].checked=!ea[i].checked;', -name=>'selectall',-value=>"",-label=>"", -title=>_tl('togglealltooltip'))) if $ALLOW_FILE_MANAGEMENT;
//
//	my $order = $cgi->param('order') || 'name';
//	my $dir = $order=~/_desc$/ ? '' : '_desc';
//	my $query = "showall=".$cgi->param('showall') if $cgi->param('showall');
//	my $hlstyle = "background-color:$COLORS{headhighlightcolor};";
//	$row.= $cgi->td({-class=>'th_fn', style=>'min-width:'.$MAXFILENAMESIZE.'em;'.($order=~/^name/?$hlstyle:''),-onclick=>"window.location.href='$ru?order=name$dir;$query'"}, $cgi->a({-href=>"$ru?order=name$dir;$query",-style=>'color:black'},_tl('names')))
//		.$cgi->td({-class=>'th_lm', -style=>$order=~/^lastmodified/?$hlstyle:'',-onclick=>"window.location.href='$ru?order=lastmodified$dir;$query'"}, $cgi->a({-href=>"$ru?order=lastmodified$dir;$query",-style=>'color:black'},_tl('lastmodified')))
//		.$cgi->td({-class=>'th_size', -style=>'text-align:right;'.($order=~/^size/i?$hlstyle:''),-onclick=>"window.location.href='$ru?order=size$dir;$query'"},$cgi->a({-href=>"$ru?order=size$dir;$query",-style=>'color:black'},_tl('size')))
//		.($SHOW_PERM? $cgi->td({-class=>'th_perm', -style=>'text-align:right;'.($order=~/^mode/?$hlstyle:''),-onclick=>"window.location.href='$ru?order=mode$dir;$query'"}, $cgi->a({-href=>"$ru?order=mode$dir;$query",-style=>'color:black'},sprintf("%-11s",_tl('permissions')))):'')
//		.$cgi->td({-class=>'th_mime', -style=>($order=~/^mime/?$hlstyle:''),-onclick=>"window.location.href='$ru?order=mime$dir;$query'"},'&nbsp;'.$cgi->a({-href=>"$ru?order=mime$dir;$query",-style=>'color:black'},_tl('mimetype')));
//	$list .= $cgi->Tr({-class=>'th',-style=>"font-weight: bold; background-color: $COLORS{headbgcolor};"}, $row);
//			
//
//	$list.=$cgi->Tr({-class=>'tr_up',-style=>'background-color:'.$COLORS{rowbgcolors}[0],-onmouseover=>'this.tmpbg="'.$COLORS{rowbgcolors}[0].'";this.style.background="'.$COLORS{rowhighlightcolor}.'";', -onmouseout=>'this.style.background=this.tmpbg;'},
//				$cgi->td({-class=>'tc_checkbox'},$cgi->checkbox(-name=>'hidden',-value=>"",-label=>"", -disabled=>'disabled', -style=>'visibility:hidden')) 
//			      . $cgi->td({-class=>'tc_fn'},getfancyfilename(dirname($ru).'/','..','< .. >',dirname($fn)))
//			      . $cgi->td('').$cgi->td('').($SHOW_PERM?$cgi->td(''):'').$cgi->td('')
//		) unless $fn eq $DOCUMENT_ROOT || $ru eq '/' || $filter;
//
//	my @files;
//	if ((!defined $FILECOUNTPERDIRLIMIT{$fn} || $FILECOUNTPERDIRLIMIT{$fn} >0 ) && opendir(DIR,$fn)) {
//		my @rfiles;
//		my $count = 0;
//		while (my $file = readdir(DIR)) {
//			next if $file =~ /^(\.|\.\.)$/;
//			next if defined $FILEFILTERPERDIR{$fn} && "$fn/$file" !~ $FILEFILTERPERDIR{$fn};
//			last if (defined $FILECOUNTPERDIRLIMIT{$fn} && $count >= $FILECOUNTPERDIRLIMIT{$fn}) || (!defined $FILECOUNTPERDIRLIMIT{$fn} && defined $FILECOUNTLIMIT && $count > $FILECOUNTLIMIT);
//			$count++;
//			push @rfiles, $file;
//		}
//		@files = sort cmp_files @rfiles;
//		closedir(DIR);
//	}
//	my $page = $cgi->param('page') ? $cgi->param('page') - 1 : 0;
//	my $fullcount = $#files + 1;
//	if (!defined $filter && defined $PAGE_LIMIT && !defined $cgi->param('showall')) {
//		splice(@files, $PAGE_LIMIT * ($page+1) );
//		splice(@files, 0, $PAGE_LIMIT * $page) if $page>0;
//	}
//
//	eval qq@/$filter/;@;
//	$filter="\Q$filter\E" if ($@);
//
//	my @rowbgcolor = @{$COLORS{rowbgcolors}};
//	my $odd = 0;
//	foreach my $filename (@files) {
//		my $full = $fn.$filename;
//		next if is_hidden($full);
//		my $mimetype = -d $full ? '<folder>' : getMIMEType($filename);
//		my $nru = $ru.uri_escape($filename);
//		$filename.="/" if -d $full;
//		$nru.="/" if -d $full;
//
//		next if $filter && $filename !~/$filter/i;
//
//		my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks) = stat($full);
//		
//		my $row = "";
//		$row.= $cgi->td({-class=>'tc_checkbox'},$cgi->checkbox(-name=>'file', -value=>$filename, -label=>'')) if $ALLOW_FILE_MANAGEMENT;
//		
//
//		my $lmf = strftime(_tl('lastmodifiedformat'), localtime($mtime));
//		my $ctf = strftime(_tl('lastmodifiedformat'), localtime($ctime));
//		$row.= $cgi->td({-class=>'tc_fn'}, getfancyfilename($nru,$filename,$mimetype, $full));
//		$row.= $cgi->td({-class=>'tc_lm', -title=>_tl('created').' '.$ctf}, $lmf);
//		$row.= $cgi->td({-class=>'tc_size',-style=>'text-align:right;', -title=>sprintf("%.2fKB ~ %.2fMB ~ %.2fGB",$size/1024, $size/1048576, $size/1073741824)}, $size);
//		$row.= $cgi->td({-class=>'tc_perm',-style=>'text-align:right;'}, $cgi->span({-style=>getmodecolors($full,$mode),-title=>sprintf("mode: %04o, uid: %s (%s), gid: %s (%s)",$mode & 07777,"".getpwuid($uid), $uid, "".getgrgid($gid), $gid)},sprintf("%-11s",mode2str($full,$mode)))) if $SHOW_PERM;
//		$row.= $cgi->td({-class=>'tc_mime'},'&nbsp;'. $cgi->escapeHTML($mimetype));
//		push @rowbgcolor,shift @rowbgcolor;
//		$list.=$cgi->Tr({-class=>$odd?'tr_odd':'tr_even', style=>'background-color:'.$rowbgcolor[0], -onmouseover=>"this.tmpbg='$rowbgcolor[0]'; this.style.background='$COLORS{rowhighlightcolor}';",-onmouseout=>"this.style.background=this.tmpbg;"}, $row);
//		$odd = ! $odd;
//
//		$count++;
//		$foldercount++ if -d $full;
//		$filecount++ if -f $full;
//		$filesizes+=$size if -f $full;
//	}
//	my $pagenav = $filter ? '' : getPageNavBar($ru, $fullcount);
//	$content.=$cgi->start_multipart_form(-onsubmit=>'return window.confirm("'._tl('confirm').'");') if $ALLOW_FILE_MANAGEMENT;
//	$content .= $pagenav;
//	$content .= $cgi->start_table({-class=>'t_filelist',-style=>'width:100%;font-family:monospace;border:0; border-spacing:0; padding:2px;'}).$list.$cgi->end_table();
//	$content .= $cgi->div({-style=>'font-size:0.8em'},sprintf("%s %d, %s %d, %s %d, %s %d Bytes (= %.2f KB = %.2f MB = %.2f GB)", _tl('statfiles'), $filecount, _tl('statfolders'), $foldercount, _tl('statsum'), $count, _tl('statsize'), $filesizes, $filesizes/1024, $filesizes/1048576, $filesizes/1073741824)) if ($SHOW_STAT); 
//
//	$content .= $pagenav;
//	return ($content, $count);
//}
//sub getmodecolors {
//	my ($fn, $m) = @_;
//	my $style = "";
//	$style='color: darkred' if ($m & 0020) == 0020;
//	$style='color: red' if ($m & 0002) == 0002 && !-k $fn;
//
//	return $style;
//}
//sub mode2str {
//	my ($fn,$m) = @_;
//
//	$m = (lstat($fn))[2] if -l $fn;
//	my @ret = split(//,'-' x 10);
//
//	$ret[0] = 'd' if -d $fn;
//	$ret[0] = 'b' if -b $fn;
//	$ret[0] = 'c' if -c $fn;
//	$ret[0] = 'l' if -l $fn;
//
//	$ret[1] = 'r' if ($m & 0400) == 0400;
//	$ret[2] = 'w' if ($m & 0200) == 0200;
//	$ret[3] = 'x' if ($m & 0100) == 0100;
//	$ret[3] = 's' if -u $fn;
//	
//	$ret[4] = 'r' if ($m & 0040) == 0040;
//	$ret[5] = 'w' if ($m & 0020) == 0020;
//	$ret[6] = 'x' if ($m & 0010) == 0010;
//	$ret[6] = 's' if -g $fn;
//
//	$ret[7] = 'r' if ($m & 0004) == 0004;
//	$ret[8] = 'w' if ($m & 0002) == 0002;
//	$ret[9] = 'x' if ($m & 0001) == 0001;
//	$ret[9] = 't' if -k $fn;
//	
//
//	return join('',@ret);
//}
//sub getSearchResult {
//	my ($search,$fn,$ru,$isRecursive, $fullcount, $visited) = @_;
//	my $content = "";
//	$ALLOW_FILE_MANAGEMENT=0;
//
//	## link loop detection:
//	my $nfn = File::Spec::Link->full_resolve($fn);
//	return $content if $$visited{$nfn};
//	$$visited{$nfn}=1;
//
//	my ($list,$count)=getFolderList($fn,$ru,$search);
//	$content.=$cgi->hr().$cgi->div({-style=>'font-size:0.8em'},$count._tl($count>1?'searchresults':'searchresult')).$list if $count>0 && $isRecursive;
//	$$fullcount+=$count;
//	my $fh;
//	opendir($fh,$fn);
//	foreach my $filename (sort cmp_files grep {  !/^(\.|\.\.)$/ } readdir($fh)) {
//		my $full = $fn.$filename;
//		next if is_hidden($full);
//		my $nru = $ru.uri_escape($filename);
//		$full.="/" if -d $full;
//		$nru.="/" if -d $full;
//		$content.=getSearchResult($search,$full,$nru,1,$fullcount,$visited) if -d $full;
//	}
//	closedir($fh);
//	if (!$isRecursive) {
//		if ($$fullcount==0) {
//			$content.=$cgi->h2(_tl('searchnothingfound') . "'" .$cgi->escapeHTML($search)."'"._tl('searchgoback').getQuickNavPath($ru));
//		} else {
//			$content=$cgi->h2("$$fullcount "._tl($$fullcount>1?'searchresultsfor':'searchresultfor')."'".$cgi->escapeHTML($search)."'"._tl('searchgoback').getQuickNavPath($ru)) 
//				. ($count>0 ?  $cgi->hr().$cgi->div({-style=>'font-size:0.8em'},$count._tl($count>1?'searchresults':'searchresult')).$list : '' )
//				. $content;
//		}
//	}
//	return $content;
//}
//sub getSupportedMethods {
//	my ($path) = @_;
//	my @methods;
//	my @rmethods = ('OPTIONS', 'TRACE', 'GET', 'HEAD', 'PROPFIND', 'PROPPATCH', 'COPY', 'GETLIB');
//	my @wmethods = ('POST', 'PUT', 'MKCOL', 'MOVE', 'DELETE');
//	push @rmethods, ('LOCK', 'UNLOCK') if $ENABLE_LOCK;
//	push @rmethods, 'REPORT' if $ENABLE_ACL || $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE || $ENABLE_CARDDAV;
//	push @rmethods, 'SEARCH' if $ENABLE_SEARCH;
//	push @wmethods, 'ACL' if $ENABLE_ACL || $ENABLE_CALDAV || $ENABLE_CARDDAV;
//	push @wmethods, 'MKCALENDAR' if $ENABLE_CALDAV || $ENABLE_CALDAV_SCHEDULE;
//	push @wmethods, 'BIND', 'UNBIND', 'REBIND' if $ENABLE_BIND;
//	@methods = @rmethods;
//	push @methods, @wmethods if !defined $path || -w $path;
//	return \@methods;
//}
//sub nonamespace {
//	my ($prop) = @_;
//	$prop=~s/^{[^}]*}//;
//	return $prop;
//}
//sub logger {
//	if (defined $LOGFILE && open(LOG,">>$LOGFILE")) {
//		my $ru = $ENV{REDIRECT_REMOTE_USER} || $ENV{REMOET_USR};
//		print LOG localtime()." - $<($ru)\@$ENV{REMOTE_ADDR}: @_\n";
//		close(LOG);
//	} else {
//		print STDERR "$0: @_\n";
//	}
//}
//sub _tl {
//	return $TRANSLATION{$LANG}{$_[0]} || $TRANSLATION{default}{$_[0]} || $_[0];
//}
//sub createMsgQuery {
//	my ($msg,$msgparam,$errmsg,$errmsgparam) = @_;
//	my $query ="";
//	$query.=";msg=$msg" if $msg;
//	$query.=";$msgparam" if $msg && $msgparam;
//	$query.=";errmsg=$errmsg" if $errmsg;
//	$query.=";$errmsgparam" if $errmsg && $errmsgparam;
//
//	return "?t=".time().$query;
//}
//sub start_html {
//	my ($title) = @_;
//	my $content ="";
//	$content.="<!DOCTYPE html>\n";
//	$content.='<head><title>'.$cgi->escapeHTML($title).'</title>';
//	$content.=qq@<meta http-equiv="Content-Type" content="text/html; charset=$CHARSET"/>@;
//	$content.=qq@<meta name="author" content="Daniel Rohde"/>@;
//	$content.=qq@</head><body>@;
//
//	return $content;
//}
//sub readMIMETypes {
//	my ($mimefile) = @_;
//	if (open(my $f, "<$mimefile")) {
//		while (my $e = <$f>) {
//			next if $e =~ /^\s*(\#.*)?$/;
//			my ($type,$suffixes) = split(/\s+/, $e, 2);
//			$MIMETYPES{$suffixes}=$type;
//		}
//		close($f);
//	} else {
//		warn "Cannot open $mimefile";
//	}
//	$MIMETYPES{default}='application/octet-stream';
//}
//sub debug {
//	print STDERR "$0: @_\n" if $DEBUG;
//}