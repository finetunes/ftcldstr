package net.finetunes.ftcldstr.routines.webdav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;
import net.finetunes.ftcldstr.routines.webdav.WebDAVLocks.WebDAVLock;
import net.finetunes.ftcldstr.routines.xml.XMLService;
import net.finetunes.ftcldstr.wrappers.ReadDirectoryContentWrapper;
import net.finetunes.ftcldstr.wrappers.ReadDirectoryResult;

public class LockingService {
	
	public static boolean isLockedRecurse(RequestParams requestParams, String fn) {
	    
	    if (fn == null) {
	        fn = requestParams.getPathTranslated(); 
	    }
	    
	    return ConfigService.locks.fnStartingLocksExist(fn + "\\");
	}
	
	public static boolean isLocked(String fn) {
	    
	    if (fn != null) {
    	    if (FileOperationsService.is_directory(fn) && !fn.endsWith("/")) {
    	        fn += "/";
    	    }
    	    return ConfigService.locks.fnLocksExists(fn);
	    }
	    
		return false;
	}
	
	public static boolean isLockable(String fn, HashMap<String, Object> xmldata) {
	    // check lock and exclusive
	    
	    // FIXME: check types and data
	    HashMap<String, Object> data = (HashMap<String, Object>)xmldata;
	    HashMap<String, Object> lockdata = (HashMap<String, Object>)data.get("{DAV:}lockscope");
	    Set<String> lockscopes = lockdata.keySet();
	    String lockscope = "exclusive";
	    if (lockscopes != null && lockscopes.size() > 0) {
	        lockscope = (String)lockscopes.toArray()[0];
	    }
	    
	    List<WebDAVLock> rowsRef = null;

	    if (!FileOperationsService.file_exits(fn)) {
	        rowsRef = ConfigService.locks.getLocks(FileOperationsService.dirname(fn) + "/");
	    }
	    else if (FileOperationsService.is_directory(fn)) {
            rowsRef = ConfigService.locks.getLocksStartingFrom(fn);
	    }
	    else {
            rowsRef = ConfigService.locks.getLocks(fn);
	    }
	    
	    boolean ret = false;
	    int rowcount = 0;
	    if (rowsRef != null) {
	        rowcount = rowsRef.size();
	    }
	    Logger.debug("isLockable: " + rowcount +", lockscope=" + lockscope);
	    
	    if (rowcount > 0) {
	        WebDAVLock lock = rowsRef.get(0);
	        // $ret =  lc($$row[3]) ne 'exclusive' && $lockscope ne '{DAV:}exclusive'?1:0;
	        ret = !lock.getScope().equalsIgnoreCase("exclusive") && !lockscope.equals("{DAV:}exclusive");
	    }
	    else {
	        ret = true;
	    }
	    
	    return ret;
	    
	
		/*      
		 * original code (for reference)
        # check lock and exclusive
        my ($fn,$xmldata) = @_;
        my @lockscopes = keys %{$$xmldata{'{DAV:}lockscope'}};
        my $lockscope = @lockscopes && $#lockscopes >-1 ? $lockscopes[0] : 'exclusive';

        my $rowsRef;
        if (! -e $fn) {
            $rowsRef = db_get(dirname($fn).'/');
        } elsif (-d $fn) {
            $rowsRef = db_getLike("$fn\%");
        } else {
            $rowsRef = db_get($fn);
        }
        my $ret = 0;
        debug("isLockable: $#{$rowsRef}, lockscope=$lockscope");
        if ($#{$rowsRef}>-1) {
            my $row = $$rowsRef[0];
            $ret =  lc($$row[3]) ne 'exclusive' && $lockscope ne '{DAV:}exclusive'?1:0;
        } else {
            $ret = 1;
        }
        return $ret;
*/     		
		
	}
	
	// TODO: note return type
	public static ArrayList<HashMap<String, Object>> getLockDiscovery(String fn) {
		
	    List<WebDAVLock> rowsRef = ConfigService.locks.getLocks(fn);
	    ArrayList<HashMap<String, Object>> resp = new ArrayList<HashMap<String,Object>>();
	    if (rowsRef != null && rowsRef.size() > 0) {
	        Logger.debug("getLockDiscovery: rowcount=" + rowsRef.size());
	        Iterator<WebDAVLock> it = rowsRef.iterator();
	        
	        while (it.hasNext()) {
	            // # basefn,fn,type,scope,token,depth,timeout,owner
	            WebDAVLock row = it.next();
                HashMap<String, Object> lock = new HashMap<String, Object>();
                HashMap<String, Object> type = new HashMap<String, Object>();
                HashMap<String, Object> scope = new HashMap<String, Object>();
                HashMap<String, Object> href = new HashMap<String, Object>();
                
                type.put(row.getType(), null); 
                scope.put(row.getScope(), null); 
                href.put("href", row.getToken());
                
                String timeout = "Infinite";
                if (row.getTimeout() != null) {
                    timeout = row.getTimeout();
                }

                lock.put("locktype", type);
                lock.put("lockscope", scope);
                lock.put("locktoken", href);
                
                
                lock.put("depth", row.getDepth());
                lock.put("timeout", timeout);
                
                if (row.getOwner() != null) {
                    lock.put("owner", row.getOwner());
                }
                
                
                HashMap<String, Object> l = new HashMap<String, Object>();
                l.put("activelock", lock);
                resp.add(l);
            }
	    }
	    
	    Logger.debug("getLockDiscovery: resp count=" + resp.size());
	    
	    if (resp.size() > 0) {
	        return resp;
	    }
	    
	    return null;
	}

    public static HashMap<String, Object> lockResource(String fn, String ru, HashMap<String, Object> xmldata, 
            int depth, int timeout, String token) {
        return lockResource(fn, ru, xmldata, depth, timeout, token, null, null);
    }
	
	
	// TODO: parameters and return type
	public static HashMap<String, Object> lockResource(String fn, String ru, HashMap<String, Object> xmldata, 
	        int depth, int timeout, String token, String base, ArrayList<String> visited) {
	    
	    HashMap<String, Object> resp = new HashMap<String, Object>();
        HashMap<String, Object> prop = new HashMap<String, Object>();
	    
	    Logger.debug("lockResource(fn=" + fn + ",ru=" + ru + ",depth=" + depth + ",timeout=" + timeout + ",token=" + token + ",base=" + base + ")");

	    HashMap<String, Object> activelock = new HashMap<String, Object>();
	    
        Set<String> locktypes = null;
	    if (xmldata.get("{DAV:}locktype") != null && xmldata.get("{DAV:}locktype") instanceof HashMap<?, ?>) {
	        locktypes = ((HashMap<String, Object>)xmldata.get("{DAV:}locktype")).keySet();
	    }
             
        Set<String> lockscopes = null;
        if (xmldata.get("{DAV:}lockscope") != null && xmldata.get("{DAV:}lockscope") instanceof HashMap<?, ?>) {
            lockscopes = ((HashMap<String, Object>)xmldata.get("{DAV:}lockscope")).keySet();
        }
        
        String locktype = null;
        if (locktypes != null && locktypes.size() > 0) {
            locktype = (String)locktypes.toArray()[0];
        }
        
        String lockscope = null;
        if (lockscopes != null && lockscopes.size() > 0) {
            lockscope = (String)lockscopes.toArray()[0];
        }
        
        HashMap<String, Object> dataRef = new HashMap<String, Object>();
        if (xmldata.containsKey("{DAV:}owner")) {
            dataRef = (HashMap<String, Object>)xmldata.get("{DAV:}owner");
        }
        else {
            dataRef = new HashMap<String, Object>(ConfigService.DEFAULT_LOCK_OWNER); 
        }
        
        String owner = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, dataRef, false);
        
        locktype = locktype.replaceFirst("{[^}]+}", "");
        lockscope = lockscope.replaceFirst("{[^}]+}", "");
        
        HashMap<String, Object> locktypekey = new HashMap<String, Object>();
        locktypekey.put(locktype, null);
        HashMap<String, Object> lockscopekey = new HashMap<String, Object>();
        lockscopekey.put(lockscope, null);
        HashMap<String, Object> locktokenkey = new HashMap<String, Object>();
        locktokenkey.put("href", token);
        activelock.put("locktype", locktypekey);
        activelock.put("lockscope", lockscopekey);
        activelock.put("locktoken", locktokenkey);
        activelock.put("depth", depth);
        activelock.put("lockroot", ru);
        
        String basefn = fn;
        if (base != null) {
            basefn = base;
        }
        if (ConfigService.locks.insertLock(basefn, fn, locktype, lockscope, token, String.valueOf(depth), String.valueOf(timeout), owner)) {
            prop.put("activelock", activelock);
        }
        else if (ConfigService.locks.updateLock(basefn, fn, String.valueOf(timeout))) {
            prop.put("activelock", activelock);
        }
        else {
            HashMap<String, Object> multistatus = null; 
            if (!resp.containsKey("multistatus")) {
                multistatus = new HashMap<String, Object>();
                resp.put("multistatus", multistatus);
            }
            else {
                multistatus = (HashMap<String, Object>)resp.get("multistatus");
            }
            
            ArrayList<Object> response = null;
            if (!multistatus.containsKey("response") && multistatus.get("response") != null) {
                 response = new ArrayList<Object>();
                 multistatus.put("response", response);
            }
            else {
                response = (ArrayList<Object>)multistatus.get("response");
            }
            
            HashMap<String, Object> r = new HashMap<String, Object>();
            r.put("href", ru);
            r.put("status", "HTTP/1.1 403 Forbidden");
            response.add(r);
        }
        
        String nfn = FileOperationsService.full_resolve(fn);
        
        if (visited == null) {
            visited = new ArrayList<String>();
        }
        
        if (visited.contains(nfn)) {
            return resp;
        }
        
        visited.add(nfn);
        
        if (FileOperationsService.is_directory(fn) && (depth == Integer.MAX_VALUE || depth > 0)) {
            Logger.debug("lockResource: depth=" + depth);
            
            ArrayList<String> files = ReadDirectoryContentWrapper.getFileList(fn);
            if (files != null && !files.isEmpty()) {
                Iterator<String> it = files.iterator();
                
                while (it.hasNext()) {
                    String f = it.next();
                    if (!f.matches("(\\.|\\.\\.)")) {
                        String nru = ru + f;
                        String nfnn = fn + f;
                        
                        if (FileOperationsService.is_directory(nfnn)) {
                            nru += "/";
                            nfnn += "/";
                        }
                        
                        Logger.debug("lockResource: " + nfnn + ", " + nru);
                        
                        int d = depth;
                        if (depth != Integer.MAX_VALUE) {
                            d = depth - 1;
                        }
                        HashMap<String, Object> subreqresp = lockResource(nfnn, nru, xmldata, d,
                                timeout, token, basefn, visited);
                        
                        if (subreqresp != null && subreqresp.containsKey("multistatus")) {
                            HashMap<String, Object> ms = null;
                            if (resp.containsKey("multistatus") && resp.get("multistatus") != null) { 
                                ms = (HashMap<String, Object>)resp.get("multistatus");
                            }
                            if (ms == null) {
                                ms = new HashMap<String, Object>();
                                resp.put("multistatus", ms);
                            }
                            
                            ArrayList<HashMap<String, Object>> response = null;
                            if (ms.containsKey("response")) {
                                response = (ArrayList<HashMap<String, Object>>)ms.get("response");
                            }
                            if (response == null) {
                                response = new ArrayList<HashMap<String,Object>>();
                                ms.put("response", response);
                            }
                            
                            HashMap<String, Object> submultistatus = null;
                            ArrayList<HashMap<String, Object>> subresponse = null;
                            if (subreqresp.get("multistatus") != null) {
                                submultistatus = (HashMap<String, Object>)subreqresp.get("multistatus"); 
                            }
                            
                            if (submultistatus.containsKey("response") && submultistatus.get("response") != null) {
                                subresponse = (ArrayList<HashMap<String, Object>>)submultistatus.get("response");
                            }
                            
                            if (subresponse != null) {
                                response.addAll(subresponse);
                            }
                        }
                        else {
                            if (subreqresp != null && subreqresp.containsKey("prop") && subreqresp.get("prop") != null) {
                                prop.putAll(((HashMap<String, Object>)((HashMap<String, Object>)subreqresp.get("prop")).get("lockdiscovery")));
                            }
                        }
                        
                    }
                }
            }
            else {
                HashMap<String, Object> multistatus = null; 
                if (!resp.containsKey("multistatus")) {
                    multistatus = new HashMap<String, Object>();
                    resp.put("multistatus", multistatus);
                }
                else {
                    multistatus = (HashMap<String, Object>)resp.get("multistatus");
                }
                
                ArrayList<Object> response = null;
                if (!multistatus.containsKey("response") && multistatus.get("response") != null) {
                     response = new ArrayList<Object>();
                     multistatus.put("response", response);
                }
                else {
                    response = (ArrayList<Object>)multistatus.get("response");
                }
                
                HashMap<String, Object> r = new HashMap<String, Object>();
                r.put("href", ru);
                r.put("status", "HTTP/1.1 403 Forbidden");
                response.add(r);
            }
            
        }
        
        if (resp.containsKey("multistatus") && prop.size() > 0) {
            HashMap<String, Object> multistatus = (HashMap<String, Object>)resp.get("multistatus"); 
            ArrayList<Object> response = null;
            if (!multistatus.containsKey("response") && multistatus.get("response") != null) {
                 response = new ArrayList<Object>();
                 multistatus.put("response", response);
            }
            else {
                response = (ArrayList<Object>)multistatus.get("response");
            }
            
            HashMap<String, Object> propp = new HashMap<String, Object>();
            propp.put("lockdiscovery", prop);
            HashMap<String, Object> propstatp = new HashMap<String, Object>();
            propstatp.put("prop", propp);
            HashMap<String, Object> responsep = new HashMap<String, Object>();
            responsep.put("propstat", propstatp);
            response.add(responsep);
            
            if (resp.get("multistatus") == null) {
                HashMap<String, Object> propr = null;
                if (resp.get("prop") != null) {
                    propr = (HashMap<String, Object>)resp.get("prop");
                }
                else {
                    propr = new HashMap<String, Object>();
                    resp.put("prop", propr);
                }
                
                propr.put("lockdiscovery", prop);
            }
        }
        
        return resp;
	}
	
	
	public static boolean unlockResource(String fn, String token) {
		
	    return isRootFolder(fn, token) && ConfigService.locks.deleteLock(fn, token);
	}
	
	public static boolean isAllowed(RequestParams requestParams, String fn, boolean recurse) {
	    
	    if (!FileOperationsService.file_exits(fn)) {
	        fn = FileOperationsService.dirname(fn) + "/";
	    }
	    
	    Logger.debug("isAllowed(" + fn + "," + recurse + ") called.");
	    
	    if (!ConfigService.ENABLE_LOCK) {
	        return true;
	    }
	    
	    HashMap<String, Object> ifheader = QueryService.getIfHeaderComponents(requestParams.getHeader("If"));
	    
	    ArrayList<WebDAVLock> rowsRef;
	    if (recurse) {
	        rowsRef = ConfigService.locks.getLocksStartingFrom(fn);
	    }
	    else {
	        rowsRef = ConfigService.locks.getLocks(fn);
	    }
	    
	    if (FileOperationsService.file_exits(fn) && !FileOperationsService.is_file_writable(fn)) {
	        // not writeable
	        return false;
	    }
	    
	    if (rowsRef.size() == 0) {
	        // no lock
	        return true;
	    }
	    
	    if (ifheader == null) {
	        return false;
	    }
	    
	    boolean ret = false;
	    
	    X:
	    for (int i = 0; i < rowsRef.size(); i++) {
	        for (int j = 0; j < ((ArrayList<HashMap<String, String>>)ifheader.get("list")).size(); j++) {
	            String iftoken = ((ArrayList<HashMap<String, String>>)ifheader.get("list")).get(j).get("token");
	            if (iftoken == null) {
	                iftoken = "";
	            }
	            
	            iftoken = iftoken.replaceAll("[\\<\\>\\s]+", "");
	            
	            String status = "OK";
	            if (!rowsRef.get(i).getToken().equals(iftoken)) {
	                status = "FAILED";
	            }
	            Logger.debug("isAllowed: " + iftoken + " send, needed for " + rowsRef.get(i).getToken() + ": " + status);
	            
	            if (rowsRef.get(i).getToken().equals(iftoken)) {
	                ret = true;
	                break X;
	            }
	        }
	    }
	    
	    return ret;
	}
	
    public static boolean isAllowed(RequestParams requestParams, String filename) {
        
        return isAllowed(requestParams, filename, false);
    }	
	
	public static boolean inheritLock(RequestParams requestParams, String fn, boolean checkContent, ArrayList<String> visited) {
	    
	    if (fn == null) {
	        fn = requestParams.getPathTranslated();
	    }
	    
	    if (visited == null) {
	        visited = new ArrayList<String>();
	    }
	    
	    String nfn = FileOperationsService.full_resolve(fn);
	    if (visited.contains(nfn)) {
	        return true;
	    }
	    
	    visited.add(nfn);
	    
	    String bfn = FileOperationsService.dirname(fn) + "/";
	    Logger.debug("inheritLock: check lock for " + bfn + " (" + fn + ")");
	    
        ArrayList<WebDAVLock> rows = ConfigService.locks.getLocks(bfn);
        
        if (rows.size() == 0 && !checkContent) {
            return false;
        }
        
        if (rows.size() > 0) {
            Logger.debug("inheritLock: " + bfn + " is locked");
        }
        
        if (checkContent) {
            rows = ConfigService.locks.getLocks(fn);
            if (rows.size() == 0) {
                return false;
            }
            
            Logger.debug("inheritLock: " + fn + " is locked");
        }
        
        WebDAVLock row = rows.get(0);
        if (FileOperationsService.is_directory(fn)) {
            
            Logger.debug("inheritLock: " + fn + " is a collection");
            
            ConfigService.locks.insertLock(row.getBasefn(), fn, row.getType(), 
                    row.getScope(), row.getToken(), row.getDepth(), row.getTimeout(), row.getOwner());
            
            List<String> files = new ArrayList<String>();
            ReadDirectoryContentWrapper rdw = new ReadDirectoryContentWrapper();
            ReadDirectoryResult d = rdw.readDirectory(fn);
            if (d.getExitCode() != 0) {
                Logger.log("Error reading directory content. Dir: " + fn + "; Error: " + d.getErrorMessage());
            }
            else {
                files = d.getContent();
            }
            
            Iterator<String> it = files.iterator();
            while (it.hasNext()) {
                String f = it.next();
                
                if (f.matches("(\\.|\\.\\.)")) {
                    continue;
                }
                
                String full = fn + f;
                if (FileOperationsService.is_directory(full) && !full.endsWith("/")) {
                    full += "/";
                    
                    ConfigService.locks.insertLock(row.getBasefn(), full, row.getType(), 
                            row.getScope(), row.getToken(), row.getDepth(), row.getTimeout(), row.getOwner());
                    LockingService.inheritLock(requestParams, full, false, visited);
                }
            }            
        }
        else {
            ConfigService.locks.insertLock(row.getBasefn(), fn, row.getType(), 
                    row.getScope(), row.getToken(), row.getDepth(), row.getTimeout(), row.getOwner());
        }
	    
		return false;
		
	}

    public static boolean inheritLock(RequestParams requestParams, String filename, boolean checkContent) {
        
        return inheritLock(requestParams, filename, checkContent, null);
    }   	
	
	
    public static boolean inheritLock(RequestParams requestParams, String filename) {

        return inheritLock(requestParams, filename, false);
    }	
    
    public static boolean inheritLock(RequestParams requestParams) {

        return inheritLock(requestParams, null);
    }
    
    // db_isRootFolder
    public static boolean isRootFolder(String fn, String token) {
        
        return ConfigService.locks.lockExists(fn, fn, token);
    }    
	
}
