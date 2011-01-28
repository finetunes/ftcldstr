package net.finetunes.ftcldstr.routines.webdav.properties;

import java.util.ArrayList;
import java.util.HashMap;

public class StatusResponse {
    
    // FIXME: create access methods if required
    private String status = null;
    private String href = null;
    private HashMap<String, Object> prop = null;
    private StatusResponse propstat = null;
    
    public Object putProp(String key, Object value) {
        
        if (prop == null) {
            prop = new HashMap<String, Object>();
        }
        
        return prop.put(key, value);
    }
    
    public Object getProp(String key) {
        
        if (prop != null) {
            return prop.get(key);
        }
        
        return null;
    }    
    
    public boolean propsExist() {
        
        return prop != null;
    }
    
//    public StatusResponse getPropStat() {
//        return propstat;
//    }
    
    public boolean propstatExists() {
        return propstat != null;
    }

    public boolean propstatPropsExist() {
        return (propstat != null) && (propstat.prop != null);
    }
    
    public Object putPropstatProp(String key, Object value) {
        
        if (propstat == null) {
            propstat = new StatusResponse();
        }
        
        if (propstat.prop == null) {
            propstat.prop = new HashMap<String, Object>();
        }
        
        return propstat.prop.put(key, value);
    }    
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }    
    

    public String getPropstatStatus() {
        if (propstat != null) {
            return propstat.status;
        }
        
        return null;
    }

    public void setPropstatStatus(String status) {
        if (propstat == null) {
            propstat = new StatusResponse();
        }
        
        propstat.status = status;
    }

    public String getPropstatHref() {
        
        if (propstat != null) {
            return propstat.href;
        }
        
        return null;
    }

    public void setPropstatHref(String href) {
        
        if (propstat == null) {
            propstat = new StatusResponse();
        }
        
        propstat.href = href;
    }
    
    public HashMap<String, Object> getProps() {
        return prop;
    }
    
    public void setPropstat(StatusResponse propstat) {
        this.propstat = propstat;
    }
    
    
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
