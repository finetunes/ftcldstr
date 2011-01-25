package net.finetunes.ftcldstr.routines.webdav;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import net.finetunes.ftcldstr.helper.Logger;

// PZ: TODO: make all the methods synchronized?

public class WebDAVLocks implements Serializable {
    
    /*
     * For Serialization purposes
     */
    private static final long serialVersionUID = -6591814866812883182L;
    List<WebDAVLock> locks;
    
    public WebDAVLocks() {
        ArrayList<WebDAVLock> nosynclocks = new ArrayList<WebDAVLock>();
        locks = Collections.synchronizedList(nosynclocks);
    }
    
    public boolean fnStartingLocksExist(String fn) {
        Iterator<WebDAVLock> it = locks.iterator();
        
        while (it.hasNext()) {
            String lockFn = it.next().getFn();
            if (lockFn != null) {
                if (lockFn.startsWith(fn)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean fnLocksExists(String fn) {
        Iterator<WebDAVLock> it = locks.iterator();
        
        while (it.hasNext()) {
            String lockFn = it.next().getFn();
            if (lockFn != null) {
                if (lockFn.equals(fn)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // db_get
    public ArrayList<WebDAVLock> getLocks(String fn, String token) {

        // PZ: TODO: check this for multi-threading, as the method
        // return references to locks, not new instances
        // otherwise implement cloning
        
        ArrayList<WebDAVLock> ref = new ArrayList<WebDAVLock>();
        Iterator<WebDAVLock> it = locks.iterator();
        
        while (it.hasNext()) {
            WebDAVLock l = it.next();
            if (l.getFn() != null) {
                if (l.getFn().equals(fn)) {
                    if (token != null) {
                        if (token.equals(l.getToken())) {
                            ref.add(l);
                        }
                    }
                    else {
                        ref.add(l);
                    }
                }
            }
        }
        
        return ref;        
    }
    
    public ArrayList<WebDAVLock> getLocks(String fn) {
        
        return getLocks(fn, null);
    }    
    
    
    public WebDAVLock getLock(String fn, String basefn) {

        Iterator<WebDAVLock> it = locks.iterator();
        
        while (it.hasNext()) {
            WebDAVLock l = it.next();
            if (l.getFn() != null) {
                if (l.getFn().equals(fn)) {
                    if (l.getBasefn() != null) {
                        if (l.getBasefn().equals(basefn)) {
                            return l;
                        }
                    }
                }
            }
        }
        
        return null;        
    }    
    
    public boolean lockExists(String fn, String basefn, String token) {
        
        Iterator<WebDAVLock> it = locks.iterator();
        
        while (it.hasNext()) {
            WebDAVLock l = it.next();
            if (l.getFn() != null) {
                if (l.getFn().equals(fn)) {
                    if (l.getBasefn() != null) {
                        if (l.getBasefn().equals(basefn)) {
                            if (l.getToken() != null) {
                                if (l.getToken().equals(token)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return false;        
    }
    
    public ArrayList<WebDAVLock> getLocksStartingFrom(String fn) {

        ArrayList<WebDAVLock> ref = new ArrayList<WebDAVLock>();
        Iterator<WebDAVLock> it = locks.iterator();
        
        while (it.hasNext()) {
            WebDAVLock l = it.next();
            if (l.getFn() != null) {
                if (l.getFn().startsWith(fn)) {
                    ref.add(l);
                }
            }
        }
        
        return ref;   
    }

    // db_insert
    public boolean insertLock(String basefn, String fn, 
            String type, String scope, String token, 
            String depth, String timeout, 
            String owner) {
        
        Logger.debug("insertLock(" + basefn + "," + fn + "," + type + "," + scope + "," + token + "," + depth + "," + timeout + "," + owner + ")");
        
        WebDAVLock lock = new WebDAVLock();
        lock.setBasefn(basefn);
        lock.setFn(fn);
        lock.setType(type);
        lock.setScope(scope);
        lock.setToken(token);
        lock.setDepth(depth);
        lock.setTimeout(timeout);
        lock.setOwner(owner);
        
        return locks.add(lock);
    }    

    // db_update
    public boolean updateLock(String basefn, String fn, String timeout) {
        
        Logger.debug("updateLock(" + basefn + "," + fn + "," + timeout + ")");
        
        WebDAVLock lock = getLock(fn, basefn);
        if (lock != null) {
            lock.setTimeout(timeout);
            return true;
        }
        
        return false;
    }      
    
    // db_delete
    public boolean deleteLock(String fn, String token) {
        
        Logger.debug("deleteLock(" + fn + "," + token + ")");
        
        Iterator<WebDAVLock> it = locks.iterator();
        
        while (it.hasNext()) {
            WebDAVLock l = it.next();
            String f = l.getFn();
            String bf = l.getBasefn();
            
            if (f == null) {
                f = "";
            }
            
            if (bf == null) {
                bf = "";
            }
            
            if (f.equals(fn) || bf.equals(fn)) {
                if (token != null) {
                    if (l.getToken() != null && l.getToken().equals(token)) {
                        it.remove();
                    }
                }
                else {
                    it.remove();
                }
            }
        }
        
        return true;
    }     
    
    public boolean deleteLock(String fn) {
        return deleteLock(fn, null);
    }
    
    public static boolean serialize(String filename, WebDAVLocks locks) {
        
        if (locks != null && filename != null) {
            try {
                FileOutputStream fout = new FileOutputStream(filename);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(locks);
                oos.close();
                return true;
            }
            catch (FileNotFoundException e) {
                Logger.log("Exception on searization: " + e.getMessage());
                e.printStackTrace();
            }
            catch (IOException e) {
                Logger.log("Exception on searization: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return false;
    }
    
    public static WebDAVLocks deserialize(String filename) {
        
        try {
            WebDAVLocks l = new WebDAVLocks();
            File f = new File(filename);

            if (f.exists()) {
                FileInputStream fin = new FileInputStream(filename);
                ObjectInputStream ois = new ObjectInputStream(fin);
                l = (WebDAVLocks) ois.readObject();
                ois.close();
                return l;
            }
            else {
                l = new WebDAVLocks();
            }
            
            return l;

        }
        catch (FileNotFoundException e) {
            Logger.log("Exception on searization: " + e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e) {
            Logger.log("Exception on searization: " + e.getMessage());
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            Logger.log("Exception on searization: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    
    
    public class WebDAVLock implements Serializable {
        
        /*
         * For Serialization purposes
         */
        private static final long serialVersionUID = -6005819485703054171L;
        
        private String basefn;
        private String fn;
        private String type;
        private String scope;
        private String token;
        private String depth; // indeed string?
        private String timeout; // indeed string?
        private String owner;
        // Date timestamp;

        public WebDAVLock() {
            basefn = null;
            fn = null;
            type = null;
            scope = null;
            token = null;
            depth = null;
            timeout = null;
            owner = null;
            // timestamp = new Date();            
        }

        public String getBasefn() {
            return basefn;
        }

        public void setBasefn(String basefn) {
            this.basefn = basefn;
        }

        public String getFn() {
            return fn;
        }

        public void setFn(String fn) {
            this.fn = fn;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getDepth() {
            return depth;
        }

        public void setDepth(String depth) {
            this.depth = depth;
        }

        public String getTimeout() {
            return timeout;
        }

        public void setTimeout(String timeout) {
            this.timeout = timeout;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

//        public Date getTimestamp() {
//            return timestamp;
//        }
//
//        public void setTimestamp(Date timestamp) {
//            this.timestamp = timestamp;
//        }
        
        
    }
}
