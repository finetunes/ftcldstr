package net.finetunes.ftcldstr.routines.fileoperations;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.wrappers.ReadDirectoryContentWrapper;
import net.finetunes.ftcldstr.wrappers.ReadDirectoryResult;

public class FileOperationsService {
	
	public static boolean rcopy(String sourcePath, String destinationPath, 
			boolean move) {
		
		// TODO: implement
		return false;
	}
	
    public static boolean rcopy(String sourcePath, String destinationPath) {
        
        return rcopy(sourcePath, destinationPath, false);
    }	
	
	public static boolean rmove(String sourcePath, String destinationPath) {
		
		// TODO: implement
		return false;
		
	}
	
	public static void changeFilePermissions(String fn, 
			int mode, String type, boolean recurse, 
			ArrayList<String> visited) {
	    
	    if (type == null) {
	        type = "";
	    }
	    
	    if (type.equals("s")) {
	        FileOperationsService.chmod(mode, fn);
	    }
	    else {
	        StatData stat = FileOperationsService.stat(fn);
	        int newmode = 0;
	        if (type.equals("a")) {
	            newmode = stat.getMode() | mode;
	        }
	        
            if (type.equals("r")) {
                newmode = stat.getMode() ^ (stat.getMode() & mode);
            }
            
            FileOperationsService.chmod(newmode, fn);
	    }
	    
	    String nfn = FileOperationsService.full_resolve(fn);
	    
	    if (visited == null) {
	        visited = new ArrayList<String>();
	    }
	    
	    if (visited.contains(nfn)) {
	        return;
	    }
	    
	    visited.add(nfn);
	    
	    if (recurse && FileOperationsService.is_directory(fn)) {
	        
	    }
	    
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
            
            if (FileOperationsService.is_directory(fn + f) && !f.endsWith("/")) {
                f += "/";
            }
            
            changeFilePermissions(fn + f, mode, type, recurse, visited);
        }
	}
	
    public static void changeFilePermissions(String filename, 
            int mode, String type, boolean recurse) {

        changeFilePermissions(filename, mode, type, recurse, null);
    }	

	public static boolean is_hidden(String filename) {
		
		// TODO: implement
		return false;
		
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
    public static boolean is_plain_file(String filename) {
        
        // TODO: implement !
        // return false;
        // File.isFile() ?
        
        return !is_directory(filename);
        
    }
    
    // determines whether the file is a symbolic link
    public static boolean is_symbolic_link(String filename) {
        
        // TODO: implement
        return false;
        
    }   
    
    // determines whether the file is a block special file
    public static boolean is_block_special_file(String filename) {
        
        // TODO: implement
        return false;
        
    }
    
    // determines whether the file is a character special file
    public static boolean is_character_special_file(String filename) {
        
        // TODO: implement
        return false;
        
    }       
    
    // determines whether the file has setuid bit set
    public static boolean file_has_setuid_bit_set(String filename) {
        
        // TODO: implement
        return false;
        
    }           
    
    // determines whether the file has setgid bit set
    public static boolean file_has_setgid_bit_set(String filename) {
        
        // TODO: implement
        return false;
        
    }   
    
    public static boolean file_has_sticky_bit_set(String filename) {
        
        // TODO: implement
        return false;
    }        
    
    // determines whether the file or directory is readable with the effective uid/gid
    public static boolean is_file_readable(String filename) {
        
        // TODO: implement
        return true;
        
    }       
    
    // determines whether the file or directory is writable with the effective uid/gid
    public static boolean is_file_writable(String filename) {
        
        // TODO: implement
        return true;
        
    }
    
    // determines whether the file or directory is executable with the effective uid/gid
    public static boolean is_file_executable(String filename) {
        
        // TODO: implement
        return true;
        
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
            
            File f = new File(filename);
            if (f.isFile() || f.isDirectory()) {
                if (f.isFile()) {
                    fullparent = f.getParent();
                    basename = f.getName();
                }
                else {
                    fullparent = f.getPath();
                    basename = "";
                }
                
            }
            else {
                if (fn.equals("/") || fn.equals("//")) {
                    fullparent = "";
                    basename = "";
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
                    
                    if (index + 1 < fn.length()) {
                        basename = fn.substring(index + 1);
                    }
                }
                
            }
        }
        
        return new String[] {fullparent, basename};
        
    }
    
    // returns the result of stat unix funtion
    public static StatData stat(String filename) {
        
        // ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, 
        // $atime,$mtime,$ctime,$blksize,$blocks) = stat($filename);
        
        // TODO: implement
        // return true;
        
        // return new Object[] {0, 0, "0888", 0, 0, 0, 0, 0, 0, "", 0, 0, 0};
        return new FileOperationsService().new StatData();
        
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
    
    public static boolean mkdir(String dirname, ArrayList<String> err) {
        
        // push into err any system errors
        // create it first if null
        
        // TODO: implement
        return false;
        
    }
    
    public static boolean mkdir(String dirname) {
        
        return mkdir(dirname, null);
    }    
    
    public static boolean chmod(int mode, String filename) {
        
        // TODO: implement
        return false;
        
    }
    
    public static String full_resolve(String filename) {
        
        // Returns the filename of $file with all links in the path resolved.
        // This sub tries to use Cwd::abs_path via ->resolve_path.
        
        // resolve_path($file) Returns the filename of $file with all links in the path resolved.
        // This sub uses Cwd::abs_path and is independent of the rest of File::Spec::Link.
        
        // TODO implement
        return "";
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
    
    public static boolean unlink(String filename) {
        
        // TODO: write errors in log if any
        // as there is no way to get the error code
        // without passing errRef param in this method
        
        // TODO: implement
        return false;
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
    
    public static String readlink(String fn) {
        
        // TODO: write errors in log if any
        // as there is no way to get the error code
        // without passing errRef param in this method        
        
        // TODO: implement
        return "";
    }      
    
    public static boolean symlink(String src, String dst) {
        
        // TODO: write errors in log if any
        // as there is no way to get the error code
        // without passing errRef param in this method        
        
        // TODO: implement
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
