package net.finetunes.ftcldstr.routines.webdav.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import net.finetunes.ftcldstr.helper.Logger;

public class Properties implements Serializable {
    
    /*
     * For Serialization purposes
     */
    private static final long serialVersionUID = -6591814866812883182L;
    HashMap<String, FileProperties> properties;
    
    public Properties() {
        properties = new HashMap<String, FileProperties>(); 
    }
    
    public FileProperties getProperties(String fn) {
        
        return properties.get(fn);
    }
    
    public String getProperty(String fn, String property) {
        
        FileProperties fp = getProperties(fn);
        if (fp != null) {
            return fp.getProperty(property);
        }
        
        return null;
    }
    
    public void setProperty(String fn, String property, String value) {
        FileProperties fp = getProperties(fn);
        if (fp == null) {
            fp = new FileProperties(fn);
            properties.put(fn, fp);
        }
        
        fp.setProperty(property, value);
    }
    
    
    public static boolean serialize(String filename, Properties properties) {
        
        if (properties != null && filename != null) {
            try {
                FileOutputStream fout = new FileOutputStream(filename);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(properties);
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
    
    public static Properties deserialize(String filename) {
        
        try {
            Properties p = new Properties();
            File f = new File(filename);

            if (f.exists()) {
                FileInputStream fin = new FileInputStream(filename);
                ObjectInputStream ois = new ObjectInputStream(fin);
                p = (Properties) ois.readObject();
                ois.close();
                return p;
            }
            else {
                p = new Properties();
            }
            
            return p;

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
    
    
    
    public class FileProperties implements Serializable {
        
        /*
         * For Serialization purposes
         */
        private static final long serialVersionUID = -6005819485703054171L;
        String filename;
        HashMap<String, String> properties;
        
        public FileProperties(String filename) {
            this.filename = filename;
            properties = new HashMap<String, String>();
        }
        
        public void setProperty(String property, String value) {
            properties.put(property, value);
        }
        
        public String getProperty(String propertyname) {
            return properties.get(propertyname);
        }
    }
    

}
