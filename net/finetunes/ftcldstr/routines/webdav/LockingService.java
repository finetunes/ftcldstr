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
	
	// TODO: second parameter should be an XML structure array
	public static boolean isLockable(String fn, Object xmldata) {
	    // check lock and exclusive
	    
	    // TODO: check types and data
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
                
                type.put(row.getType(), null); // TODO: check whether null is a valid value; otherwise use "" 
                scope.put(row.getScope(), null); // TODO: check whether null is a valid value; otherwise use "" 
                href.put("href", row.getToken()); // TODO: check whether null is a valid value; otherwise use ""
                
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
	
	// TODO: parameters and return type
	public static Object lockResource() {
		return null;
	}
	
	
	public static boolean unlockResource(String filename, String token) {
		
		// TODO: implement
		return false;
		
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
            // TODO: insert lock
            // db_insert($$row[0],$fn,$$row[2],$$row[3],$$row[4],$$row[5],$$row[6],$$row[7]);
            
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
                
                if (f.matches("^(\\.|\\.\\.)$")) {
                    continue;
                }
                
                String full = fn + f;
                if (FileOperationsService.is_directory(full) && !full.endsWith("/")) {
                    full += "/";
                    // TODO: insert lock
                    // db_insert($$row[0],$full,$$row[2],$$row[3],$$row[4],$$row[5],$$row[6],$$row[7]);
                    LockingService.inheritLock(requestParams, full, false, visited);
                }
            }            
        }
        else {
            // TODO: insert lock
            // db_insert($$row[0],$fn,$$row[2],$$row[3],$$row[4],$$row[5],$$row[6],$$row[7]);
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
	
}
