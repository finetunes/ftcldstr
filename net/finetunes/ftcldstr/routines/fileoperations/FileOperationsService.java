package net.finetunes.ftcldstr.routines.fileoperations;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sun.org.mozilla.javascript.internal.WrapFactory;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.InitializationService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesActions;
import net.finetunes.ftcldstr.wrappers.CommonContentWrapper;
import net.finetunes.ftcldstr.wrappers.CommonWrapperResult;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;

public class FileOperationsService {
	
	public static boolean rcopy(RequestParams requestParams, String src, String dst, 
			boolean move) {
	    
	    // src exists and readable?
	    if (!FileOperationsService.file_exits(requestParams, src) || !FileOperationsService.is_file_readable(requestParams, src)) {
	       return false; 
	    }
	    
	    // dst writeable?
	    if (FileOperationsService.file_exits(requestParams, dst) && !FileOperationsService.is_file_writable(requestParams, dst)) {
	        return false;
	    }
	    
	    String nsrc = new String(src);
	    nsrc = nsrc.replaceFirst("/$", ""); // remove trailing slash for link test (-l)
	    
	    if (FileOperationsService.is_symbolic_link(requestParams, nsrc)) { // link
	        if (!move || !FileOperationsService.rename(requestParams, nsrc, dst)) {
	            String orig = FileOperationsService.readlink(requestParams, nsrc);
	            if ((!move || FileOperationsService.unlink(requestParams, nsrc)) && !FileOperationsService.symlink(requestParams, orig, dst)) {
	                return false;
	            }
	        }
	    } else if (FileOperationsService.is_plain_file(requestParams, src)) { // file
	        if (FileOperationsService.is_directory(requestParams, dst)) {
	            if (!dst.endsWith("/")) {
	                dst += "/";
	            }
	            dst += FileOperationsService.basename(src);
	        }
	        
	        if (!move || !FileOperationsService.rename(requestParams, src, dst)) {

	            try {
    	            InputStream in = FileOperationsService.getFileContentStream(requestParams, src);
    	            OutputStream out = FileOperationsService.getFileWriteStream(requestParams, dst);
                    byte[] buf = new byte[1024 * 20];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
	            }
	            catch (IOException e) {
	                Logger.log("Error on copying the file " + src + " to " + dst + "; " + e.getMessage());
	                return false;
	            }
	            
	            if (move) {
	                if (!FileOperationsService.is_file_writable(requestParams, src)) {
	                    return false;
	                }
	                
	                if (!FileOperationsService.unlink(requestParams, src)) {
	                    return false;
	                }
	            }
	        }
	    }
	    else if (FileOperationsService.is_directory(requestParams, src)) {
	        // cannot write folders to files:
	        if (FileOperationsService.is_plain_file(requestParams, dst)) {
	            return false;
	        }
	        
	        if (!dst.endsWith("/")) {
	            dst += "/";
	        }
	        
	        if (!src.endsWith("/")) {
	            src += "/";
	        }
	        
	        if (!move || DirectoryOperationsService.getDirInfo(requestParams, src, "realchildcount") > 0 ||
	                !FileOperationsService.rename(requestParams, src, dst)) {
	            if (!FileOperationsService.file_exits(requestParams, dst)) {
	                FileOperationsService.mkdir(requestParams, dst);
	            }
	            
	            ArrayList<String> files = WrappingUtilities.getFileList(requestParams, src);
	            if (files == null) {
	                return false;
	            }
	            
	            Iterator<String> it = files.iterator();
	            while (it.hasNext()) {
	                String filename = it.next();
	                FileOperationsService.rcopy(requestParams, src + filename, dst + filename, move);
	            }

	            if (move) {
	                if (!FileOperationsService.is_file_writable(requestParams, src)) {
	                    return false;
	                }
	                
	                if (!FileOperationsService.rmdir(requestParams, src)) {
	                    return false;
	                }
	            }
	            
	        }
	    }
	    else {
	        return false;
	    }
	    
	    ConfigService.properties.deleteProperties(dst);
	    ConfigService.properties.copyProperties(src, dst);
	    if (move) {
	        ConfigService.properties.deleteProperties(src);
	    }
	    
	    return true;
	}
	
    public static boolean rcopy(RequestParams requestParams, String src, String dst) {
        
        return rcopy(requestParams, src, dst, false);
    }	
	
	public static boolean rmove(RequestParams requestParams, String src, String dst) {
		
        return rcopy(requestParams, src, dst, true);
	}
	
	public static void changeFilePermissions(RequestParams requestParams, String fn, 
			int mode, String type, boolean recurse, 
			ArrayList<String> visited) {
	    
	    if (type == null) {
	        type = "";
	    }
	    
	    if (type.equals("s")) {
	        FileOperationsService.chmod(requestParams, fn, mode);
	    }
	    else {
	        StatData stat = FileOperationsService.stat(requestParams, fn);
	        int newmode = 0;
	        if (type.equals("a")) {
	            newmode = stat.getMode() | mode;
	        }
	        
            if (type.equals("r")) {
                newmode = stat.getMode() ^ (stat.getMode() & mode);
            }
            
            FileOperationsService.chmod(requestParams, fn, newmode);
	    }
	    
	    String nfn = FileOperationsService.full_resolve(requestParams, fn);
	    
	    if (visited == null) {
	        visited = new ArrayList<String>();
	    }
	    
	    if (visited.contains(nfn)) {
	        return;
	    }
	    
	    visited.add(nfn);
	    
	    if (recurse && FileOperationsService.is_directory(requestParams, fn)) {
	        
	    }
	    
        List<String> files = WrappingUtilities.getFileList(requestParams, fn);
        if (files != null) {
            Iterator<String> it = files.iterator();
            while (it.hasNext()) {
                String f = it.next();
                
                if (FileOperationsService.is_directory(requestParams, fn + f) && !f.endsWith("/")) {
                    f += "/";
                }
                
                changeFilePermissions(requestParams, fn + f, mode, type, recurse, visited);
            }
        }
	}
	
    public static void changeFilePermissions(RequestParams requestParams, String filename, 
            int mode, String type, boolean recurse) {

        changeFilePermissions(requestParams, filename, mode, type, recurse, null);
    }	

	public static boolean is_hidden(String fn) {
		
	    if (ConfigService.HIDDEN != null && ConfigService.HIDDEN.size() > 0) {
	        return ConfigService.HIDDEN.contains(fn);
	    }
	    else {
	        return false;
	    }
	}
	
	public static String getFileContent(RequestParams requestParams, String fn) {
		
        return WrappingUtilities.readFile(requestParams, fn);
	}
	
	// additional methods
	
    public static InputStream getFileContentStream(RequestParams requestParams, String fn) {

        return WrappingUtilities.getFileContentReadStream(requestParams, fn);
    }

    public static boolean is_directory(RequestParams requestParams, String fn) {

        File file = new File(fn);
        return file.isDirectory();
        
        // return WrappingUtilities.checkFileIsDirectory(requestParams, fn);
    }	
    
    public static boolean file_exits(RequestParams requestParams, String fn) {
        
        File file = new File(fn);
        return file.exists();

        // return WrappingUtilities.checkFileExists(requestParams, fn);
    }

    // determines whether the file is a plain file (not a link, directory, pipe, etc.)
    public static boolean is_plain_file(RequestParams requestParams, String fn) {

        return WrappingUtilities.checkFileIsPlain(requestParams, fn);
    }
    
    // determines whether the file is a symbolic link
    public static boolean is_symbolic_link(RequestParams requestParams, String fn) {

        return WrappingUtilities.checkFileIsSymbolicLink(requestParams, fn);
    }   
    
    // determines whether the file is a block special file
    public static boolean is_block_special_file(RequestParams requestParams, String fn) {
        
        return WrappingUtilities.checkFileIsBlockSpecialFile(requestParams, fn);
    }
    
    // determines whether the file is a character special file
    public static boolean is_character_special_file(RequestParams requestParams, String fn) {
        
        return WrappingUtilities.checkFileIsCharacterSpecialFile(requestParams, fn);
    }       
    
    // determines whether the file has setuid bit set
    public static boolean file_has_setuid_bit_set(RequestParams requestParams, String fn) {
        
        return WrappingUtilities.checkFileHasSetuidBitSet(requestParams, fn);
    }           
    
    // determines whether the file has setgid bit set
    public static boolean file_has_setgid_bit_set(RequestParams requestParams, String fn) {

        return WrappingUtilities.checkFileHasSetgidBitSet(requestParams, fn);
    }   
    
    public static boolean file_has_sticky_bit_set(RequestParams requestParams, String fn) {
        
        return WrappingUtilities.checkFileHasStickyBitSet(requestParams, fn);
    }        
    
    // determines whether the file or directory is readable with the effective uid/gid
    public static boolean is_file_readable(RequestParams requestParams, String fn) {
        
        return WrappingUtilities.checkFileIsReadable(requestParams, fn);
    }       
    
    // determines whether the file or directory is writable with the effective uid/gid
    public static boolean is_file_writable(RequestParams requestParams, String fn) {
        
        return WrappingUtilities.checkFileIsWritable(requestParams, fn);
    }
    
    // determines whether the file or directory is executable with the effective uid/gid
    public static boolean is_file_executable(RequestParams requestParams, String fn) {
        
        return WrappingUtilities.checkFileIsExecutable(requestParams, fn);
    }
    
    public static String dirname(String filename) {
        return splitFilename(filename)[0];
    }
    
    public static String basename(String filename) {
        return splitFilename(filename)[1];
    }
    
    public static String[] splitFilename(String filename) {
        
        String fullparent = "";
        String basename = "";
        String fn = filename;
        
        if (fn != null && !fn.isEmpty()) {
            if (fn.equals("/") || fn.equals("//")) {
                fullparent = "/";
                basename = "/";
            }
            else {
                int index = fn.lastIndexOf("/");
                if (fn.length() == index + 1) {
                    fn = fn.substring(0, index);
                    index = fn.lastIndexOf("/");
                }
                
                if (index > 0) {
                    fullparent = fn.substring(0, index);
                }
                else if (index == 0) {
                    fullparent = "/";
                }
                
                if (index + 1 < fn.length()) {
                    basename = fn.substring(index + 1);
                }
            }
        }
        
        return new String[] {fullparent, basename};
        
    }
    
    // returns the result of stat unix funtion
    public static StatData stat(RequestParams requestParams, String fn) {
        
        // ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, 
        // $atime,$mtime,$ctime,$blksize,$blocks) = stat($filename);
        
        return WrappingUtilities.stat(requestParams, fn);
    }   
    
    // returns the result of lstat unix funtion
    public static StatData lstat(RequestParams requestParams, String fn) {
        
        // If the file is a symbolic link, it returns the information for the link, 
        // rather than the file it points to. Otherwise, it returns the information 
        // for the file.        
        
        // ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, 
        // $atime,$mtime,$ctime,$blksize,$blocks) = stat($filename);

        return WrappingUtilities.lstat(requestParams, fn);
    }       
    
    public static boolean mkdir(RequestParams requestParams, String dirname, ArrayList<String> err) {
        
        return WrappingUtilities.mkdir(requestParams, dirname, err);
    }
    
    public static boolean mkdir(RequestParams requestParams, String dirname) {
        
        InitializationService.initUmaskSettings(requestParams);
        return mkdir(requestParams, dirname, null);
    }    
    
    public static boolean chmod(RequestParams requestParams, String fn, int mode) {
        
        return WrappingUtilities.chmod(requestParams, fn, Integer.toOctalString(mode));
    }
    
    public static String full_resolve(RequestParams requestParams, String fn) {
        
        // Returns the filename of $file with all links in the path resolved.
        
        try {
            File file = new File(fn);
            return file.getCanonicalPath();
        }
        catch (IOException e) {
            Logger.log("Exception on resolving the link: " + fn + ". Error message: " + e.getMessage());
            return fn;
        }
        
        // return WrappingUtilities.fullResolveSymbolicLink(requestParams, fn);
    }
    
    // returns stream to write the file content to
    public static OutputStream getFileWriteStream(RequestParams requestParams, String fn) {
        
        InitializationService.initUmaskSettings(requestParams);
        return WrappingUtilities.getFileContentWriteStream(requestParams, fn);
    }
    
    
    public static boolean writeFileFromStream(RequestParams requestParams, String fn, FileInputStream fis) {
        if (fn != null && fis != null) {
            OutputStream outStream = getFileWriteStream(requestParams, fn);
            return writeContentFromStream(fis, outStream);
        }
        
        return false;
    }

    public static boolean writeContentFromStream(FileInputStream fis, OutputStream outStream) {
        if (outStream != null && fis != null) {
            BufferedInputStream buf = null;
            try {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = fis.read(buffer)) >= 0) {
                    outStream.write(buffer, 0, length);
                }                
            }
            catch (IOException e) {
                Logger.log("Exception: Unable write the output file." + e.getMessage());
            }
            finally {
                try {
                    fis.close();
                }
                catch (IOException e) {
                    
                }
                try {
                    outStream.close();
                }
                catch (IOException e) {
                    
                }
                
                if (buf != null) {
                    try {
                        buf.close();
                    }
                    catch (IOException e) {
                        // do nothing
                    }
                }
            }            
        }
        
        return false;
    }    
    
    public static boolean unlink(RequestParams requestParams, String fn) {
        
        return WrappingUtilities.unlink(requestParams, fn);
    }
    
    public static boolean rmdir(RequestParams requestParams, String dirname) {

        return WrappingUtilities.rmdir(requestParams, dirname);
    }

    public static boolean rename(RequestParams requestParams, String src, String dst) {
        
        return WrappingUtilities.rename(requestParams, src, dst);
    }    
    
    public static String readlink(RequestParams requestParams, String fn) {
        
        if (is_symbolic_link(requestParams, fn)) {
            return full_resolve(requestParams, fn);
        }
        else {
            Logger.log("Error: not a symblic link: " + fn);
        }
        
        return null;
    }      
    
    public static boolean symlink(RequestParams requestParams, String src, String dst) {
        
        return WrappingUtilities.symlink(requestParams, src, dst);
    }          
    
    // creates a new file (or rewrites exising and writes content into it)
    public static boolean create_file(RequestParams requestParams, String fn, String content) {
        
        OutputStream os = getFileWriteStream(requestParams, fn);
        Writer out = new BufferedWriter(new OutputStreamWriter(os));
        try {
            out.write(content);
        }
        catch (IOException e) {
            return false;
        }
        finally {
            try {
                out.close();
            }
            catch (IOException ex) {
                // ignore
            }
        }

        return true;
    }
    
    // sets access and modification time of a file
    public static boolean utime(RequestParams requestParams, java.util.Date atime, java.util.Date mtime, String fn) {
        
        if (atime != null && mtime != null) {
            long autime = atime.getTime() / 1000;
            long mutime = mtime.getTime() / 1000;
            
            return WrappingUtilities.utime(requestParams, fn, String.valueOf(autime), String.valueOf(mutime));
        }
        
        return false;
    }
    
    // changes the owner of an uploaded file to the user who uploaded it.
    // automatically an uploaded file gets the root as an owner if
    // the server it running under root permissions
    public static boolean setUploadedFileOwner(RequestParams requestParams, String fn) {
        
        String username = requestParams.getUsername();
        String groupname = WrappingUtilities.getUserGroupName(requestParams, username);
        
        if (fn != null && username != null && groupname != null) {
            if (!WrappingUtilities.chown(requestParams, fn, username, groupname)) {
                return false;
            }
            
            return FileOperationsService.chmod(requestParams, fn, 0777 - ConfigService.UMASK);
        }
        
        return false;
    }
    
    public class StatData {

        // $dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks

        int dev;
        int ino;
        int mode;
        int nlink;
        int uid;
        int gid;
        int rdev;
        int size;
        long atime;
        long mtime;
        long ctime;
        int blksize;
        int blocks;
        
        public java.util.Date getMtimeDate() {
            return convertToDate(getMtime());
        }
        
        public java.util.Date getAtimeDate() {
            return convertToDate(getAtime());
        }      
        
        public java.util.Date getCtimeDate() {
            return convertToDate(getCtime());
        }        
        
        private java.util.Date convertToDate(long d) {
            long timestamp = d * 1000L;
            java.util.Date md = new java.util.Date(timestamp);
            return md;
        }
        
        public int getDev() {
            return dev;
        }
        public void setDev(int dev) {
            this.dev = dev;
        }
        public int getIno() {
            return ino;
        }
        public void setIno(int ino) {
            this.ino = ino;
        }
        public int getMode() {
            return mode;
        }
        public void setMode(int mode) {
            this.mode = mode;
        }
        public int getNlink() {
            return nlink;
        }
        public void setNlink(int nlink) {
            this.nlink = nlink;
        }
        public int getUid() {
            return uid;
        }
        public void setUid(int uid) {
            this.uid = uid;
        }
        public int getGid() {
            return gid;
        }
        public void setGid(int gid) {
            this.gid = gid;
        }
        public int getRdev() {
            return rdev;
        }
        public void setRdev(int rdev) {
            this.rdev = rdev;
        }
        public int getSize() {
            return size;
        }
        public void setSize(int size) {
            this.size = size;
        }
        public long getAtime() {
            return atime;
        }
        public void setAtime(long atime) {
            this.atime = atime;
        }
        public long getMtime() {
            return mtime;
        }
        public void setMtime(long mtime) {
            this.mtime = mtime;
        }
        public long getCtime() {
            return ctime;
        }
        public void setCtime(long ctime) {
            this.ctime = ctime;
        }
        public int getBlksize() {
            return blksize;
        }
        public void setBlksize(int blksize) {
            this.blksize = blksize;
        }
        public int getBlocks() {
            return blocks;
        }
        public void setBlocks(int blocks) {
            this.blocks = blocks;
        }
    }
    
    
}
