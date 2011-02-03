package net.finetunes.ftcldstr.routines.webdav.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.rendering.RenderingHelper;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService.StatData;
import net.finetunes.ftcldstr.routines.webdav.LockingService;

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
	        resp_200.putProp("creationdate", String.format("%tY-%tm-%tdT%tT%tz", stat.getCtimeDate(),
	                stat.getCtimeDate(), stat.getCtimeDate(), stat.getCtimeDate(), stat.getCtimeDate()));
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
            resp_200.putProp("getlastmodified", String.format("%ta, %td %tb %tY %tT GMT", stat.getMtimeDate(), stat.getMtimeDate(), 
                    stat.getMtimeDate(), stat.getMtimeDate(), stat.getMtimeDate()));
        }       
        
        if (prop.equals("lockdiscovery")) {
            resp_200.putProp("lockdiscovery", LockingService.getLockDiscovery(fn));
        }       
	    
/*
 * TODO: the rest	    
    $$resp_200{prop}{resourcetype}=(-d $fn?{collection=>undef}:undef) if $prop eq 'resourcetype';
    $$resp_200{prop}{resourcetype}{calendar}=undef if $prop eq 'resourcetype' && $ENABLE_CALDAV && -d $fn;
    $$resp_200{prop}{resourcetype}{'schedule-inbox'}=undef if $prop eq 'resourcetype' && $ENABLE_CALDAV_SCHEDULE && -d $fn;
    $$resp_200{prop}{resourcetype}{'schedule-outbox'}=undef if $prop eq 'resourcetype' && $ENABLE_CALDAV_SCHEDULE && -d $fn;
    $$resp_200{prop}{resourcetype}{addressbook}=undef if $prop eq 'resourcetype' && $ENABLE_CARDDAV && -d $fn;
    $$resp_200{prop}{resourcetype}{'vevent-collection'}=undef if $prop eq 'resourcetype' && $ENABLE_GROUPDAV && -d $fn;
    $$resp_200{prop}{resourcetype}{'vtodo-collection'}=undef if $prop eq 'resourcetype' && $ENABLE_GROUPDAV && -d $fn;
    $$resp_200{prop}{resourcetype}{'vcard-collection'}=undef if $prop eq 'resourcetype' && $ENABLE_GROUPDAV && -d $fn;
    $$resp_200{prop}{'component-set'}='VEVENT,VTODO,VCARD' if $prop eq 'component-set';
    if ($prop eq 'supportedlock') {
        $$resp_200{prop}{supportedlock}{lockentry}[0]{lockscope}{exclusive}=undef;
        $$resp_200{prop}{supportedlock}{lockentry}[0]{locktype}{write}=undef;
        $$resp_200{prop}{supportedlock}{lockentry}[1]{lockscope}{shared}=undef;
        $$resp_200{prop}{supportedlock}{lockentry}[1]{locktype}{write}=undef;
    }
    $$resp_200{prop}{executable}=(-x $fn )?'T':'F' if $prop eq 'executable';

    $$resp_200{prop}{source}={ 'link'=> { 'src'=>$uri, 'dst'=>$uri }} if $prop eq 'source';

    if ($prop eq 'quota-available-bytes' || $prop eq 'quota-used-bytes' || $prop eq 'quota' || $prop eq 'quotaused') {
        my ($ql,$qu) = getQuota();
        if (defined $ql && defined $qu) {
            $$resp_200{prop}{'quota-available-bytes'} = $ql - $qu if $prop eq 'quota-available-bytes';
            $$resp_200{prop}{'quota-used-bytes'} = $qu if $prop eq 'quota-used-bytes';
            $$resp_200{prop}{'quota'} = $ql if $prop eq 'quota';
            $$resp_200{prop}{'quotaused'}= $qu if $prop eq 'quotaused';
        } else {
            $$resp_404{prop}{'quota-available-bytes'} = undef if $prop eq 'quota-available-bytes';
            $$resp_404{prop}{'quota-used-bytes'} = undef if $prop eq 'quota-used-bytes';
        }
        next;
    }
    $$resp_200{prop}{childcount}=(-d $fn?getDirInfo($fn,$prop):0) if $prop eq 'childcount';
    $$resp_200{prop}{id}=$uri if $prop eq 'id';
    $$resp_200{prop}{isfolder}=(-d $fn?1:0) if $prop eq 'isfolder';
    $$resp_200{prop}{ishidden}=(basename($fn)=~/^\./?1:0) if $prop eq 'ishidden';
    $$resp_200{prop}{isstructureddocument}=0 if $prop eq 'isstructureddocument';
    $$resp_200{prop}{hassubs}=(-d $fn ?getDirInfo($fn,$prop):0) if $prop eq 'hassubs';
    $$resp_200{prop}{nosubs}=(-d $fn?(-w $fn?1:0):1) if $prop eq 'nosubs';
    $$resp_200{prop}{objectcount}=(-d $fn?getDirInfo($fn,$prop):0) if $prop eq 'objectcount';
    $$resp_200{prop}{reserved}=0 if $prop eq 'reserved';
    $$resp_200{prop}{visiblecount}=(-d $fn?getDirInfo($fn,$prop):0) if $prop eq 'visiblecount';

    $$resp_200{prop}{iscollection}=(-d $fn?1:0) if $prop eq 'iscollection';
    $$resp_200{prop}{isFolder}=(-d $fn?1:0) if $prop eq 'isFolder';
    $$resp_200{prop}{'authoritative-directory'}=(-d $fn?'t':'f') if $prop eq 'authoritative-directory';
    $$resp_200{prop}{resourcetag}=$REQUEST_URI if $prop eq 'resourcetag';
    $$resp_200{prop}{'repl-uid'}=getuuid($fn) if $prop eq 'repl-uid';
    $$resp_200{prop}{modifiedby}=$ENV{REDIRECT_REMOTE_USER}||$ENV{REMOTE_USER} if $prop eq 'modifiedby';
    $$resp_200{prop}{Win32CreationTime}=strftime('%a, %d %b %Y %T GMT' ,gmtime($ctime)) if $prop eq 'Win32CreationTime';
    if ($prop eq 'Win32FileAttributes') {
        my $fileattr = 128 + 32; # 128 - Normal, 32 - Archive, 4 - System, 2 - Hidden, 1 - Read-Only
        $fileattr+=1 unless -w $fn;
        $fileattr+=2 if basename($fn)=~/^\./;
        $$resp_200{prop}{Win32FileAttributes}=sprintf("%08x",$fileattr);
    }
    $$resp_200{prop}{Win32LastAccessTime}=strftime('%a, %d %b %Y %T GMT' ,gmtime($atime)) if $prop eq 'Win32LastAccessTime';
    $$resp_200{prop}{Win32LastModifiedTime}=strftime('%a, %d %b %Y %T GMT' ,gmtime($mtime)) if $prop eq 'Win32LastModifiedTime';
    $$resp_200{prop}{name}=$cgi->escape(basename($fn)) if $prop eq 'name';
    $$resp_200{prop}{href}=$uri if $prop eq 'href';
    $$resp_200{prop}{parentname}=$cgi->escape(basename(dirname($uri))) if $prop eq 'parentname';
    $$resp_200{prop}{isreadonly}=(!-w $fn?1:0) if $prop eq 'isreadonly';
    $$resp_200{prop}{isroot}=($fn eq $DOCUMENT_ROOT?1:0) if $prop eq 'isroot';
    $$resp_200{prop}{getcontentclass}=(-d $fn?'urn:content-classes:folder':'urn:content-classes:document') if $prop eq 'getcontentclass';
    $$resp_200{prop}{contentclass}=(-d $fn?'urn:content-classes:folder':'urn:content-classes:document') if $prop eq 'contentclass';
    $$resp_200{prop}{lastaccessed}=strftime('%m/%d/%Y %I:%M:%S %p' ,gmtime($atime)) if $prop eq 'lastaccessed';

    $$resp_200{prop}{owner} = { href=>$uri } if $prop eq 'owner';
    $$resp_200{prop}{group} = { href=>$uri } if $prop eq 'group';
    $$resp_200{prop}{'supported-privilege-set'}= getACLSupportedPrivilegeSet($fn) if $prop eq 'supported-privilege-set';
    $$resp_200{prop}{'current-user-privilege-set'} = getACLCurrentUserPrivilegeSet($fn) if $prop eq 'current-user-privilege-set';
    $$resp_200{prop}{acl} = getACLProp($mode) if $prop eq 'acl';
    $$resp_200{prop}{'acl-restrictions'} = {'no-invert'=>undef,'required-principal'=>{all=>undef,property=>[{owner=>undef},{group=>undef}]}} if $prop eq 'acl-restrictions';
    $$resp_200{prop}{'inherited-acl-set'} = undef if $prop eq 'inherited-acl-set';
    $$resp_200{prop}{'principal-collection-set'} = { href=> $PRINCIPAL_COLLECTION_SET }, if $prop eq 'principal-collection-set';

    $$resp_200{prop}{'calendar-description'} = undef if $prop eq 'calendar-description';
    $$resp_200{prop}{'calendar-timezone'} = undef if $prop eq 'calendar-timezone';
    $$resp_200{prop}{'supported-calendar-component-set'} = '<C:comp name="VEVENT"/><C:comp name="VTODO"/><C:comp name="VJOURNAL"/><C:comp name="VTIMEZONE"/>' if $prop eq 'supported-calendar-component-set';
    $$resp_200{prop}{'supported-calendar-data'}='<C:calendar-data content-type="text/calendar" version="2.0"/>' if $prop eq 'supported-calendar-data';
    $$resp_200{prop}{'max-resource-size'}=20000000 if $prop eq 'max-resource-size';
    $$resp_200{prop}{'min-date-time'}='19000101T000000Z' if $prop eq 'min-date-time';
    $$resp_200{prop}{'max-date-time'}='20491231T235959Z' if $prop eq 'max-date-time';
    $$resp_200{prop}{'max-instances'}=100 if $prop eq 'max-instances';
    $$resp_200{prop}{'max-attendees-per-instance'}=100 if $prop eq 'max-attendees-per-instance';
    ##$$resp_200{prop}{'calendar-data'}='<![CDATA['.getFileContent($fn).']]>' if $prop eq 'calendar-data';
    if ($prop eq 'calendar-data') {
        if ($fn=~/\.ics$/i) {
            $$resp_200{prop}{'calendar-data'}=$cgi->escapeHTML(getFileContent($fn));
        } else {
            $$resp_404{prop}{'calendar-data'}=undef;
        }
    }
    $$resp_200{prop}{'getctag'}=getETag($fn)  if $prop eq 'getctag';
    $$resp_200{prop}{'current-user-principal'}{href}=$CURRENT_USER_PRINCIPAL if $prop eq 'current-user-principal';
    $$resp_200{prop}{'principal-URL'}{href}=$CURRENT_USER_PRINCIPAL if $prop eq 'principal-URL';
    $$resp_200{prop}{'calendar-home-set'}{href}=getCalendarHomeSet($uri) if $prop eq 'calendar-home-set';
    $$resp_200{prop}{'calendar-user-address-set'}{href}= $CURRENT_USER_PRINCIPAL if $prop eq 'calendar-user-address-set';
    $$resp_200{prop}{'schedule-inbox-URL'}{href} = getCalendarHomeSet($uri) if $prop eq 'schedule-inbox-URL';
    $$resp_200{prop}{'schedule-outbox-URL'}{href} = getCalendarHomeSet($uri) if $prop eq 'schedule-outbox-URL';
    $$resp_200{prop}{'calendar-user-type'}='INDIVIDUAL' if $prop eq 'calendar-user-type';
    $$resp_200{prop}{'schedule-calendar-transp'}{transparent} = undef if $prop eq 'schedule-calendar-transp';
    $$resp_200{prop}{'schedule-default-calendar-URL'}=getCalendarHomeSet($uri) if $prop eq 'schedule-default-calendar-URL';
    $$resp_200{prop}{'schedule-tag'}=getETag($fn) if $prop eq 'schedule-tag';

    if ($prop eq 'address-data') {
        if ($fn =~ /\.vcf$/i) {
            $$resp_200{prop}{'address-data'}=$cgi->escapeHTML(getFileContent($fn));
        } else {
            $$resp_404{prop}{'address-data'}=undef;
        }
    }
    $$resp_200{prop}{'addressbook-description'} = $cgi->escape(basename($fn)) if $prop eq 'addressbook-description';
    $$resp_200{prop}{'supported-address-data'}='<A:address-data-type content-type="text/vcard" version="3.0"/>' if $prop eq 'supported-address-data';
    $$resp_200{prop}{'{urn:ietf:params:xml:ns:carddav}max-resource-size'}=20000000 if $prop eq 'max-resource-size' && $ENABLE_CARDDAV;
    $$resp_200{prop}{'addressbook-home-set'}{href}=getAddressbookHomeSet($uri) if $prop eq 'addressbook-home-set';
    $$resp_200{prop}{'principal-address'}{href}=$uri if $prop eq 'principal-address';
    
    
    $$resp_200{prop}{'supported-report-set'} = 
                { 'supported-report' => 
                    [   
                        { report=>{ 'acl-principal-prop-set'=>undef } },
                        { report=>{ 'principal-match'=>undef } },
                        { report=>{ 'principal-property-search'=>undef } }, 
                        { report=>{ 'calendar-multiget'=>undef } },  
                        { report=>{ 'calendar-query'=>undef } },
                        { report=>{ 'free-busy-query'=>undef } },
                        { report=>{ 'addressbook-query'=>undef} },
                        { report=>{ 'addressbook-multiget'=>undef} },
                    ]
                } if $prop eq 'supported-report-set';

    if ($prop eq 'supported-method-set') {
        $$resp_200{prop}{'supported-method-set'} = '';
        foreach my $method (@{getSupportedMethods($fn)}) {
            $$resp_200{prop}{'supported-method-set'} .= '<D:supported-method name="'.$method.'"/>';
        }
    }

    if ($prop eq 'resource-id') {
        my $e = getETag(File::Spec::Link->full_resolve($fn));
        $e=~s/"//g;
        $$resp_200{prop}{'resource-id'} = 'urn:uuid:'.$e;
    }	    
	    
*/		
	}
	
	// TODO: params and return type
	public static void setProperty(RequestParams requestParams, String propname, HashMap<String, Object> elementParentRef, 
	        StatusResponse resp_200, StatusResponse resp_403) {
	    
	    String fn = requestParams.getPathTranslated();
	    String ru = requestParams.getRequestURI();
/*		
	TODO:
	
    $propname=~/^{([^}]+)}(.*)$/;
    my ($ns,$pn) = ($1,$2);
    debug("setProperty: $propname (ns=$ns, pn=$pn)");
    
    if ($propname eq '{http://apache.org/dav/props/}executable') {
        my $executable = $$elementParentRef{$propname}{'content'};
        if (defined $executable) {
            my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks) = stat($fn);
            chmod( ($executable=~/F/) ? $mode & 0666 : $mode | 0111, $fn);
            $$resp_200{href}=$ru;
            $$resp_200{propstat}{prop}{executable}=$executable;
            $$resp_200{propstat}{status}='HTTP/1.1 200 OK';
        }
    } elsif (($propname eq '{DAV:}getlastmodified')||($propname eq '{urn:schemas-microsoft-com:}Win32LastModifiedTime')
            ||($propname eq '{urn:schemas-microsoft-com:}Win32LastAccessTime')
            ||($propname eq '{urn:schemas-microsoft-com:}Win32CreationTime')) {
        my $getlastmodified = $$elementParentRef{'{DAV:}getlastmodified'};
        $getlastmodified = $$elementParentRef{'{urn:schemas-microsoft-com:}Win32LastModifiedTime'} if !defined $getlastmodified;
        my $lastaccesstime =$$elementParentRef{'{urn:schemas-microsoft-com:}Win32LastAccessTime'};
        if (defined $getlastmodified) {
            my $mtime = str2time($getlastmodified);
            my $atime = defined $lastaccesstime ? str2time($lastaccesstime) : $mtime;
            utime($atime,$mtime,$fn);
            $$resp_200{href}=$ru;
            $$resp_200{propstat}{prop}{getlastmodified}=$getlastmodified if defined  $$elementParentRef{'{DAV:}getlastmodified'};
            $$resp_200{propstat}{prop}{Win32LastModifiedTime}=$getlastmodified if $$elementParentRef{'{urn:schemas-microsoft-com:}Win32LastModifiedTime'};
            $$resp_200{propstat}{prop}{Win32LastAccessTime}=$lastaccesstime if $$elementParentRef{'{urn:schemas-microsoft-com:}Win32LastAccessTime'};
            $$resp_200{propstat}{prop}{Win32CreationTime}=$$elementParentRef{'{urn:schemas-microsoft-com:}Win32CreationTime'} if defined $$elementParentRef{'{urn:schemas-microsoft-com:}Win32CreationTime'};
            $$resp_200{propstat}{status}='HTTP/1.1 200 OK';
        } 
    } elsif ($propname eq '{urn:schemas-microsoft-com:}Win32FileAttributes') {
        $$resp_200{href}=$ru;
        $$resp_200{propstat}{prop}{Win32FileAttributes}=undef;
        $$resp_200{propstat}{status}='HTTP/1.1 200 OK';
    } elsif (defined $NAMESPACES{$ns} && grep(/^\Q$pn\E$/,@PROTECTED_PROPS)>0) {
        $$resp_403{href}=$ru;
        $$resp_403{propstat}{prop}{$propname}=undef;
        $$resp_403{propstat}{status}='HTTP/1.1 403 Forbidden';
    } else {
        my $n = $propname;
        $n='{}'.$n if (ref($$elementParentRef{$propname}) eq 'HASH' && $$elementParentRef{$propname}{xmlns} eq "" && $n!~/^{[^}]*}/);
        my $dbval = db_getProperty($fn, $n);
        my $value = createXML($$elementParentRef{$propname},0);
        my $ret = defined $dbval ? db_updateProperty($fn, $n, $value) : db_insertProperty($fn, $n, $value);
        if ($ret) {
            $$resp_200{href}=$ru;
            $$resp_200{propstat}{prop}{$propname}=undef;
            $$resp_200{propstat}{status}='HTTP/1.1 200 OK';
        } else {
            debug("Cannot set property '$propname'");
            $$resp_403{href}=$ru;
            $$resp_403{propstat}{prop}{$propname}=undef;
            $$resp_403{propstat}{status}='HTTP/1.1 403 Forbidden';
            
        }
    }
	    
	    
*/	    
	}
	
}
