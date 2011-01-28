package net.finetunes.ftcldstr.routines.handlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.routines.webdav.properties.PropertiesActions;
import net.finetunes.ftcldstr.routines.webdav.properties.StatusResponse;

public class PropertyRequestHandler {
	
	public static void handlePropertyRequest(RequestParams requestParams, String xml, HashMap<String, Object> dataRef, StatusResponse resp_200, StatusResponse resp_403) {
	    
	    if (dataRef.get("{DAV:}remove").getClass().isArray()) {
	        
	        HashMap<String, Object>[] removeEntries = (HashMap<String, Object>[])dataRef.get("{DAV:}remove");
	        for (int i = 0; i < removeEntries.length; i++) {
	            HashMap<String, Object> remove = removeEntries[i];
	            HashMap<String, Object> props = (HashMap<String, Object>)remove.get("{DAV:}prop");
	            Set<String> keys = props.keySet();
	            Iterator<String> it = keys.iterator();
	            while (it.hasNext()) {
	                String propname = it.next();
	                PropertiesActions.removeProperty(requestParams, propname, props, resp_200, resp_403);
	            }               
	        }
	        
/*
	        foreach my $remove (@{$$dataRef{'{DAV:}remove'}}) {
            foreach my $propname (keys %{$$remove{'{DAV:}prop'}}) {
                removeProperty($propname, $$remove{'{DAV:}prop'}, $resp_200, $resp_403);
            }
        }        
*/	        
	    }
	    else if (dataRef.get("{DAV:}remove") instanceof HashMap<?, ?>) {
	        
	        HashMap<String, Object> props = (HashMap<String, Object>)((HashMap<String, Object>)dataRef.get("{DAV:}remove")).get("{DAV:}prop");
	        Set<String> keys = props.keySet();
	        Iterator<String> it = keys.iterator();
	        while (it.hasNext()) {
	            String propname = it.next();
	            PropertiesActions.removeProperty(requestParams, propname, props, resp_200, resp_403);
	        }
	        
	        
/*
	        foreach my $propname (keys %{$$dataRef{'{DAV:}remove'}{'{DAV:}prop'}}) {
            removeProperty($propname, $$dataRef{'{DAV:}remove'}{'{DAV:}prop'}, $resp_200, $resp_403);
        }        
*/	        
	    }
	    
	    if (dataRef.get("{DAV:}set").getClass().isArray()) {
            HashMap<String, Object>[] setEntries = (HashMap<String, Object>[])dataRef.get("{DAV:}set");
            for (int i = 0; i < setEntries.length; i++) {
                HashMap<String, Object> set = setEntries[i];
                HashMap<String, Object> props = (HashMap<String, Object>)set.get("{DAV:}prop");
                Set<String> keys = props.keySet();
                Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String propname = it.next();
                    PropertiesActions.setProperty(requestParams, propname, props, resp_200, resp_403);
                }               
            }	        
	    }
	    else if (dataRef.get("{DAV:}set") instanceof HashMap<?, ?>) {
	        boolean lastmodifiedprocessed = false;
            HashMap<String, Object> props = (HashMap<String, Object>)((HashMap<String, Object>)dataRef.get("{DAV:}set")).get("{DAV:}prop");
            Set<String> keys = props.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String propname = it.next();
                if (propname.equals("{DAV:}getlastmodified") || propname.equals("{urn:schemas-microsoft-com:}Win32LastModifiedTime")) {
                    if (lastmodifiedprocessed) {
                        continue;
                    }
                    lastmodifiedprocessed = true;
                }
                PropertiesActions.setProperty(requestParams, propname, props, resp_200, resp_403);
            }
	    }
	    
	    // original comment was:
	    // ## fix parser bug: set/remove|remove/set of the same prop
	    // FIXME: do we need this in java?
	    if (xml.matches("(?s).*<([^:]+:)?set[\\s>]+.*<([^:]+:)?remove[\\s>]+.*")) {
	        if (dataRef.get("{DAV:}remove").getClass().isArray()) {
	            
	            HashMap<String, Object>[] removeEntries = (HashMap<String, Object>[])dataRef.get("{DAV:}remove");
	            for (int i = 0; i < removeEntries.length; i++) {
	                HashMap<String, Object> remove = removeEntries[i];
	                HashMap<String, Object> props = (HashMap<String, Object>)remove.get("{DAV:}prop");
	                Set<String> keys = props.keySet();
	                Iterator<String> it = keys.iterator();
	                while (it.hasNext()) {
	                    String propname = it.next();
	                    PropertiesActions.removeProperty(requestParams, propname, props, resp_200, resp_403);
	                }               
	            }	            
	        }
	        else if (dataRef.get("{DAV:}remove") instanceof HashMap<?, ?>) {
	            HashMap<String, Object> props = (HashMap<String, Object>)((HashMap<String, Object>)dataRef.get("{DAV:}remove")).get("{DAV:}prop");
	            Set<String> keys = props.keySet();
	            Iterator<String> it = keys.iterator();
	            while (it.hasNext()) {
	                String propname = it.next();
	                PropertiesActions.removeProperty(requestParams, propname, props, resp_200, resp_403);
	            }	            
	        }
	    }
	}
}
