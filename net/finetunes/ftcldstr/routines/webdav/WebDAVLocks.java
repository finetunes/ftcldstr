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
import java.util.Date;
import java.util.Iterator;

import net.finetunes.ftcldstr.helper.Logger;

public class WebDAVLocks implements Serializable {
    
    /*
     * For Serialization purposes
     */
    private static final long serialVersionUID = -6591814866812883182L;
    ArrayList<WebDAVLock> locks;
    
    public WebDAVLocks() {
        locks = new ArrayList<WebDAVLock>();
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
    
    public ArrayList<WebDAVLock> getLocks(String fn) {
        
        ArrayList<WebDAVLock> ref = new ArrayList<WebDAVLock>();
        Iterator<WebDAVLock> it = locks.iterator();
        
        while (it.hasNext()) {
            WebDAVLock l = it.next();
            if (l.getFn() != null) {
                if (l.getFn().equals(fn)) {
                    ref.add(l);
                }
            }
        }
        
        return ref;        
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
        Date timestamp;

        public WebDAVLock() {
            basefn = null;
            fn = null;
            type = null;
            scope = null;
            token = null;
            depth = null;
            timeout = null;
            timestamp = new Date();            
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

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }
    }
}
