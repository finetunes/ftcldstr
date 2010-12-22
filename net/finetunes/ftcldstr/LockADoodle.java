package net.finetunes.ftcldstr;

import java.util.*;

public class LockADoodle {
	private final static Object lock = new Object();
	private final static Hashtable<String, Object[]> locks = new Hashtable<String, Object[]>();
	
	//when returning "null" -> lock has succeeded
	public final static Object[] acquireLock(String forWhat, Object objRequestingLock) {
		Object[] ret = null;
		synchronized(lock) {
			Object[] wb = locks.get(forWhat);
			if(wb == null) {
				//i can have that lock...
				locks.put(forWhat, new Object[]{
						objRequestingLock, //could even use some nice interfaced object with ".forceReleaseLock()" 
						new Date()
					});
			}
			else {
				Object who = wb[0];
				Date when = (Date)wb[1];
				
				ret = new Object[]{who, when};
			}
		}
		
		return ret;
	}
}
