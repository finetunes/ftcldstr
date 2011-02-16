package net.finetunes.ftcldstr.routines.fileoperations;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sun.org.mozilla.javascript.internal.WrapFactory;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;
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
	    if (!FileOperationsService.file_exits(src) || !FileOperationsService.is_file_readable(requestParams, src)) {
	       return false; 
	    }
	    
	    // dst writeable?
	    if (FileOperationsService.file_exits(dst) && !FileOperationsService.is_file_writable(requestParams, dst)) {
	        return false;
	    }
	    
	    String nsrc = new String(src);
	    nsrc = nsrc.replaceFirst("/$", ""); // remove trailing slash for link test (-l)
	    
	    if (FileOperationsService.is_symbolic_link(requestParams, nsrc)) { // link
	        if (!move || !FileOperationsService.rename(nsrc, dst)) {
	            String orig = FileOperationsService.readlink(requestParams, nsrc);
	            if ((!move || FileOperationsService.unlink(requestParams, nsrc)) && !FileOperationsService.symlink(orig, dst)) {
	                return false;
	            }
	        }
	    } else if (FileOperationsService.is_plain_file(requestParams, src)) { // file
	        if (FileOperationsService.is_directory(dst)) {
	            if (!dst.endsWith("/")) {
	                dst += "/";
	            }
	            dst += FileOperationsService.basename(src);
	        }
	        
	        if (!move || !FileOperationsService.rename(src, dst)) {

	            try {
    	            InputStream in = FileOperationsService.getFileContentStream(src);
    	            OutputStream out = FileOperationsService.getFileWriteStream(dst);
                    byte[] buf = new byte[1024];
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
	    else if (FileOperationsService.is_directory(src)) {
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
	                !FileOperationsService.rename(src, dst)) {
	            if (!FileOperationsService.file_exits(dst)) {
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
	                
	                if (!FileOperationsService.rmdir(src)) {
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
	        FileOperationsService.chmod(mode, fn);
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
            
            FileOperationsService.chmod(newmode, fn);
	    }
	    
	    String nfn = FileOperationsService.full_resolve(requestParams, fn);
	    
	    if (visited == null) {
	        visited = new ArrayList<String>();
	    }
	    
	    if (visited.contains(nfn)) {
	        return;
	    }
	    
	    visited.add(nfn);
	    
	    if (recurse && FileOperationsService.is_directory(fn)) {
	        
	    }
	    
        List<String> files = WrappingUtilities.getFileList(requestParams, fn);
        if (files != null) {
            Iterator<String> it = files.iterator();
            while (it.hasNext()) {
                String f = it.next();
                
                if (FileOperationsService.is_directory(fn + f) && !f.endsWith("/")) {
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
	
	public static String getFileContent(String filename) {
		
		// TODO: implement
		return null;
		
	}
	
	// additional methods
	
    public static InputStream getFileContentStream(String filename) {

    //  if (open(F,"<$fn")) {
//      binmode(STDOUT);
//      while (read(F,my $buffer, $BUFSIZE)>0) {
//          print $buffer;
//      }
//      close(F);
//  }             
        
        // TODO: implement
        return null;
        
    }
	
    public static boolean is_directory(String filename) {

        File file = new File(filename);
        return file.isDirectory();
    }	
    
    // TODO: check whether works on dirs as well, not only files 
    public static boolean file_exits(String filename) {
        
        File file = new File(filename);
        return file.exists();
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
    public static Object[] lstat(String filename) {
        
        // If the file is a symbolic link, it returns the information for the link, 
        // rather than the file it points to. Otherwise, it returns the information 
        // for the file.        
        
        // ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, 
        // $atime,$mtime,$ctime,$blksize,$blocks) = stat($filename);
        
        // TODO: implement
        // return true;
        
        return new Object[] {0, 0, "0888", 0, 0, 0, 0, 0, 0, "", 0, 0, 0};
        
    }       
    
    public static boolean mkdir(RequestParams requestParams, String dirname, ArrayList<String> err) {
        
        return WrappingUtilities.mkdir(requestParams, dirname, err);
    }
    
    public static boolean mkdir(RequestParams requestParams, String dirname) {
        
        return mkdir(requestParams, dirname, null);
    }    
    
    public static boolean chmod(int mode, String filename) {
        
        // TODO: implement
        return false;
        
    }
    
    public static String full_resolve(RequestParams requestParams, String fn) {
        
        // Returns the filename of $file with all links in the path resolved.
        return WrappingUtilities.fullResolveSymbolicLink(requestParams, fn);
    }
    
    public static boolean write_file(String fn, String content) {

        // method creates a new file and writets content to it
        // returns false if file can't be created or written
        // otherwise returns true

/*            
        if (open(my $f,">$PATH_TRANSLATED")) {
            binmode STDIN;
            binmode $f;
            my $maxread = 0;
            while (my $read = read(STDIN, $buffer, $BUFSIZE)>0) {
                print $f $buffer;
                $maxread+=$read;
            }
            close($f);
            inheritLock();
            if (exists $ENV{CONTENT_LENGTH} && $maxread != $ENV{CONTENT_LENGTH}) {
                debug("_PUT: ERROR: maxread=$maxread, content-length: $ENV{CONTENT_LENGTH}");
                #$status='400';
            }


            logger("PUT($PATH_TRANSLATED)");
        } else {
            $status='403 Forbidden';
            $content="";
            $type='text/plain';
        }
            
*/
        
        // TODO: implement
        return false;
        
    }
    
    // TODO: returns stream to write the file contents to
    public static OutputStream getFileWriteStream(String fn) {
        return null;
    }
    
    
    public static boolean writeFileFromStream(String fn, FileInputStream fis) {
        if (fn != null && fis != null) {
            OutputStream outStream = getFileWriteStream(fn);
            BufferedInputStream buf = null;
            try {
                buf = new BufferedInputStream(fis);
                int readBytes = 0;
                
                while ((readBytes = buf.read()) != -1) {
                    outStream.write(readBytes);            
                }
                
                return true;
            }
            catch (IOException e) {
                Logger.log("Exception: Unable write the output file." + e.getMessage());
            }
            finally {
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
    
    public static boolean rmdir(String dirname) {
        
        // TODO: write errors in log if any
        // as there is no way to get the error code
        // without passing errRef param in this method
        
        // TODO: implement
        return false;
    }

    public static boolean rename(String src, String dst) {
        
        // TODO: write errors in log if any
        // as there is no way to get the error code
        // without passing errRef param in this method        
        
        // TODO: implement
        return false;
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
    
    public static boolean symlink(String src, String dst) {
        
        // TODO: write errors in log if any
        // as there is no way to get the error code
        // without passing errRef param in this method        
        
        // TODO: implement
        return false;
    }          
    
    // creates a new file (or rewrites exising and writes content into it)
    // e. g.:
    // open(F,">$fn")
    // print F '';
    // close(F);
    public static boolean create_file(String fn, String content) {
        
        // TODO: write errors in log if any
        // as there is no way to get the error code
        // without passing errRef param in this method        
        
        // TODO: implement
        return false;
    }
    
    // sets access and modification time of a file
    public static boolean utime(java.util.Date atime, java.util.Date mtime, String fn) {
        // TODO: implement
        // perl code: utime($atime,$mtime,$fn);
        return false;
    }
    
    public class StatData {

        // $dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks

        int dev;
        int ino;
        int mode; // 2
        int nlink;
        int uid;
        int gid;
        int rdev;
        int size; // 7
        long atime;
        long mtime; // 9
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
