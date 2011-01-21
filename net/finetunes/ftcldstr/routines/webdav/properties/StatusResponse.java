package net.finetunes.ftcldstr.routines.webdav.properties;

import java.util.HashMap;

public class StatusResponse {
    
    String status;
    String href;
    HashMap<String, Object> prop;
    StatusResponse propstat;
    
//    public StatusResponse() {
//        
//    }
    
/*
 * 
 *  usage example   
    
    $$resp_200{propstat}{status}='HTTP/1.1 200 OK';
    $$resp_200{propstat}{prop}{$propname} = undef;
    $$resp_200{propstat}{prop}{executable}=$executable;
    $$resp_200{propstat}{status}='HTTP/1.1 200 OK';
    
    $resp_404{prop}{$prop}=undef;
    
    $$resp_200{prop}{creationdate}=strftime('%Y-%m-%dT%H:%M:%SZ' ,gmtime($ctime)) if $prop eq 'creationdate';
    $$resp_200{prop}{displayname}=$cgi->escape(basename($uri)) if $prop eq 'displayname' && !defined $$resp_200{prop}{displayname};
    $$resp_200{prop}{getcontentlanguage}='en' if $prop eq 'getcontentlanguage';
    $$resp_200{prop}{getcontentlength}= $size if $prop eq 'getcontentlength';
    $$resp_200{prop}{getcontenttype}=(-d $fn?'httpd/unix-directory':getMIMEType($fn)) if $prop eq 'getcontenttype';
    $$resp_200{prop}{getetag}=getETag($fn) if $prop eq 'getetag';
    $$resp_200{prop}{getlastmodified}=strftime('%a, %d %b %Y %T GMT' ,gmtime($mtime)) if $prop eq 'getlastmodified';
    $$resp_200{prop}{lockdiscovery}=getLockDiscovery($fn) if $prop eq 'lockdiscovery';
    $$resp_200{prop}{resourcetype}=(-d $fn?{collection=>undef}:undef) if $prop eq 'resourcetype';
    $$resp_200{prop}{resourcetype}{calendar}=undef if $prop eq 'resourcetype' && $ENABLE_CALDAV && -d $fn;
    $$resp_200{prop}{resourcetype}{'schedule-inbox'}=undef if $prop eq 'resourcetype' && $ENABLE_CALDAV_SCHEDULE && -d $fn;
    $$resp_200{prop}{resourcetype}{'schedule-outbox'}=undef if $prop eq 'resourcetype' && $ENABLE_CALDAV_SCHEDULE && -d $fn;
    $$resp_200{prop}{resourcetype}{addressbook}=undef if $prop eq 'resourcetype' && $ENABLE_CARDDAV && -d $fn;
    $$resp_200{prop}{resourcetype}{'vevent-collection'}=undef if $prop eq 'resourcetype' && $ENABLE_GROUPDAV && -d $fn;
    $$resp_200{prop}{resourcetype}{'vtodo-collection'}=undef if $prop eq 'resourcetype' && $ENABLE_GROUPDAV && -d $fn;
    $$resp_200{prop}{resourcetype}{'vcard-collection'}=undef if $prop eq 'resourcetype' && $ENABLE_GROUPDAV && -d $fn;
    $$resp_200{prop}{'component-set'}='VEVENT,VTODO,VCARD' if $prop eq 'component-set';

    $$resp_200{prop}{supportedlock}{lockentry}[0]{lockscope}{exclusive}=undef;
    $$resp_200{prop}{supportedlock}{lockentry}[0]{locktype}{write}=undef;
    $$resp_200{prop}{supportedlock}{lockentry}[1]{lockscope}{shared}=undef;
    $$resp_200{prop}{supportedlock}{lockentry}[1]{locktype}{write}=undef;
*/    
    
    

}
