package net.finetunes.ftcldstr.implementation.webdav.locking;

import java.util.HashMap;

import net.finetunes.ftcldstr.implementation.webdav.locking.types.LockInfo;

public class LockingService {
	
	public static boolean isLockedRecurse(String filename) {
		
		// TODO: implement
		return false;
		
	}
	
	public static boolean isLocked(String filename) {
		
		// TODO: implement
		return false;
		
	}
	
	// TODO: second parameter should be an XML structure array
	public static boolean isLockable(String filename) {
		
		// TODO: implement
		return false;
		
	}
	
	public static LockInfo getLockDiscovery(String filename) {
		
		// TODO: implement
		return new LockInfo();
		
	}
	
	// TODO: parameters and return type
	public static void lockResource() {
		
	}
	
	
	public static boolean unlockResource(String filename, String token) {
		
		// TODO: implement
		return false;
		
	}
	
	public static boolean isAllowed(String filename, boolean recurse) {
		
		// TODO: implement
		return false;
		
	}
	
	public static boolean inheritLock(String fileaname, boolean checkContent, HashMap visited) {
		
		// TODO: implement
		return false;
		
	}

}