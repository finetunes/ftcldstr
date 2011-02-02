package net.finetunes.ftcldstr.routines.webdav.properties;

import java.util.ArrayList;
import java.util.HashMap;

import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;

public class ACLActions {
	
	// returns an array to build an xml from (using createXML)
	public static HashMap<String, Object> getACLSupportedPrivilegeSet(String fn) {

        HashMap<String, Object> set = new HashMap<String, Object>();
        HashMap<String, Object> supportedprivilege = new HashMap<String, Object>();
        
        HashMap<String, Object> privilege = new HashMap<String, Object>();
        privilege.put("all", null);
        supportedprivilege.put("privilege", privilege);
        supportedprivilege.put("abstract", null);
        supportedprivilege.put("description", "Any operation");
        
        ArrayList<HashMap<String, Object>> supportedprivilegelist = new ArrayList<HashMap<String,Object>>();
        
        HashMap<String, Object> sp1 = new HashMap<String, Object>();
        HashMap<String, Object> sp1privilege = new HashMap<String, Object>();
        sp1privilege.put("read", null);
        sp1.put("privilege", sp1privilege);
        sp1.put("description", "Read any object");
        
        ArrayList<HashMap<String, Object>> splist1 = new ArrayList<HashMap<String,Object>>();
        
        HashMap<String, Object> sp11 = new HashMap<String, Object>();
        HashMap<String, Object> sp11privilege = new HashMap<String, Object>();
        sp11privilege.put("read-acl", null);
        sp11.put("privilege", sp11privilege);
        sp11.put("absract", null);
        sp11.put("description", "Read ACL");
        splist1.add(sp11);
        
        HashMap<String, Object> sp12 = new HashMap<String, Object>();
        HashMap<String, Object> sp12privilege = new HashMap<String, Object>();
        sp12privilege.put("read-current-user-privilege-set", null);
        sp12.put("privilege", sp12privilege);
        sp12.put("absract", null);
        sp12.put("description", "Read current user privilege set property");
        splist1.add(sp12);
        
        HashMap<String, Object> sp13 = new HashMap<String, Object>();
        HashMap<String, Object> sp13privilege = new HashMap<String, Object>();
        sp13privilege.put("read-free-busy", null);
        sp13.put("privilege", sp13privilege);
        sp13.put("absract", null);
        sp13.put("description", "Read busy time information");
        splist1.add(sp13);

        sp1.put("supported-privilege", splist1);
        supportedprivilegelist.add(sp1);
        
        //
        HashMap<String, Object> sp2 = new HashMap<String, Object>();
        HashMap<String, Object> sp2privilege = new HashMap<String, Object>();
        sp2privilege.put("write", null);
        sp2.put("privilege", sp2privilege);
        sp2.put("description", "Write any object");
        
        ArrayList<HashMap<String, Object>> splist2 = new ArrayList<HashMap<String,Object>>();
        
        HashMap<String, Object> sp21 = new HashMap<String, Object>();
        HashMap<String, Object> sp21privilege = new HashMap<String, Object>();
        sp21privilege.put("write-acl", null);
        sp21.put("privilege", sp21privilege);
        sp21.put("absract", null);
        sp21.put("description", "Write ACL");
        splist2.add(sp21);
        
        HashMap<String, Object> sp22 = new HashMap<String, Object>();
        HashMap<String, Object> sp22privilege = new HashMap<String, Object>();
        sp22privilege.put("write-properties", null);
        sp22.put("privilege", sp22privilege);
        sp22.put("absract", null);
        sp22.put("description", "Write properties");
        splist2.add(sp22);
        
        HashMap<String, Object> sp23 = new HashMap<String, Object>();
        HashMap<String, Object> sp23privilege = new HashMap<String, Object>();
        sp23privilege.put("write-content", null);
        sp23.put("privilege", sp23privilege);
        sp23.put("absract", null);
        sp23.put("description", "Write resource content");
        splist2.add(sp23);
        
        sp2.put("supported-privilege", splist2);        
        supportedprivilegelist.add(sp2);        
        
        //
        HashMap<String, Object> sp3 = new HashMap<String, Object>();
        HashMap<String, Object> sp3privilege = new HashMap<String, Object>();
        sp3privilege.put("unlock", null);
        sp3.put("privilege", sp3privilege);
        sp3.put("abstract", null);
        sp3.put("description", "Unlock resource");
        supportedprivilegelist.add(sp3);           
        
        //
        HashMap<String, Object> sp4 = new HashMap<String, Object>();
        HashMap<String, Object> sp4privilege = new HashMap<String, Object>();
        sp4privilege.put("bind", null);
        sp4.put("privilege", sp4privilege);
        sp4.put("abstract", null);
        sp4.put("description", "Add new files/folders");
        supportedprivilegelist.add(sp4);           
        
        //
        HashMap<String, Object> sp5 = new HashMap<String, Object>();
        HashMap<String, Object> sp5privilege = new HashMap<String, Object>();
        sp5privilege.put("unbind", null);
        sp5.put("privilege", sp5privilege);
        sp5.put("abstract", null);
        sp5.put("description", "Delete or move files/folders");
        supportedprivilegelist.add(sp5);           
        
        supportedprivilege.put("supported-privilege", supportedprivilegelist);
	    
        set.put("supported-privilege", supportedprivilege);
        return set;
	}
	
	// returns an array to build an xml from (using createXML)
	public static HashMap<String, Object> getACLCurrentUserPrivilegeSet(String fn) {
		
	    HashMap<String, Object> usergrant = new HashMap<String, Object>();
	    if (FileOperationsService.is_file_readable(fn)) {
	        HashMap<String, Object> readprivilege = new HashMap<String, Object>();
	        readprivilege.put("read", null);
            HashMap<String, Object> readaclprivilege = new HashMap<String, Object>();
	        readaclprivilege.put("read-acl", null);
            HashMap<String, Object> readcuprivilege = new HashMap<String, Object>();
	        readcuprivilege.put("read-current-user-privilege-set", null);
	        
	        ArrayList<HashMap<String, Object>> rp = new ArrayList<HashMap<String,Object>>();
            rp.add(readprivilege);
            rp.add(readaclprivilege);
            rp.add(readcuprivilege);
	        
	        usergrant.put("privilege", rp);
	        
	        if (FileOperationsService.is_file_writable(fn)) {

	            HashMap<String, Object> wp = new HashMap<String, Object>();
	            wp.put("write", null);
                HashMap<String, Object> wpacl = new HashMap<String, Object>();
                wpacl.put("write-acl", null);
                HashMap<String, Object> wpc = new HashMap<String, Object>();
                wpc.put("write-content", null);
                HashMap<String, Object> wpp = new HashMap<String, Object>();
                wpp.put("write-properties", null);
                HashMap<String, Object> wpb = new HashMap<String, Object>();
                wpb.put("bind", null);
                HashMap<String, Object> wpu = new HashMap<String, Object>();
                wpu.put("unbind", null);
	            
                ArrayList<HashMap<String, Object>> wpl = new ArrayList<HashMap<String,Object>>();
                wpl.add(wp);
                wpl.add(wpacl);
                wpl.add(wpc);
                wpl.add(wpp);
                wpl.add(wpb);
                wpl.add(wpu);
                
                usergrant.put("privilege", wpl);                
	            
	        }
	    }
	    
	    return usergrant;
	}
	
	// returns an array to build an xml from (using createXML)
    public static HashMap<String, Object> getACLProp(int mode) {
        
        HashMap<String, Object> ace = new HashMap<String, Object>();
        
        HashMap<String, Object> ownergrant = new HashMap<String, Object>();
        ArrayList<HashMap<String, Object>> ownerlist = new ArrayList<HashMap<String,Object>>();
        HashMap<String, Object> groupgrant = new HashMap<String, Object>();
        ArrayList<HashMap<String, Object>> grouplist = new ArrayList<HashMap<String,Object>>();
        HashMap<String, Object> othergrant = new HashMap<String, Object>();
        ArrayList<HashMap<String, Object>> otherlist = new ArrayList<HashMap<String,Object>>();
        
        mode = mode & 07777;
        
        // 
        if ((mode & 0400) == 0400) {
            HashMap<String, Object> p = new HashMap<String, Object>();
            p.put("read", null);
            ownerlist.add(p);
        }
        
        if ((mode & 0200) == 0200) {
            HashMap<String, Object> p1 = new HashMap<String, Object>();
            p1.put("write", null);
            ownerlist.add(p1);
            
            HashMap<String, Object> p2 = new HashMap<String, Object>();
            p2.put("bind", null);
            ownerlist.add(p2);
            
            HashMap<String, Object> p3 = new HashMap<String, Object>();
            p3.put("unbind", null);
            ownerlist.add(p3);
        }        
        
        // 
        if ((mode & 0004) == 0004) {
            HashMap<String, Object> p = new HashMap<String, Object>();
            p.put("read", null);
            otherlist.add(p);
        }
        
        if ((mode & 0002) == 0002) {
            HashMap<String, Object> p1 = new HashMap<String, Object>();
            p1.put("write", null);
            otherlist.add(p1);
            
            HashMap<String, Object> p2 = new HashMap<String, Object>();
            p2.put("bind", null);
            otherlist.add(p2);
            
            HashMap<String, Object> p3 = new HashMap<String, Object>();
            p3.put("unbind", null);
            otherlist.add(p3);
        }    
        
        // 
        if ((mode & 0400) == 0400) {
            HashMap<String, Object> p = new HashMap<String, Object>();
            p.put("read", null);
            ownerlist.add(p);
        }
        
        if ((mode & 0200) == 0200) {
            HashMap<String, Object> p1 = new HashMap<String, Object>();
            p1.put("write", null);
            ownerlist.add(p1);
            
            HashMap<String, Object> p2 = new HashMap<String, Object>();
            p2.put("bind", null);
            ownerlist.add(p2);
            
            HashMap<String, Object> p3 = new HashMap<String, Object>();
            p3.put("unbind", null);
            ownerlist.add(p3);
        }            

        ownergrant.put("privilege", ownerlist);
        groupgrant.put("privilege", grouplist);
        othergrant.put("privilege", otherlist);
        
        ArrayList<HashMap<String, Object>> acelist = new ArrayList<HashMap<String,Object>>();
        
        
        HashMap<String, Object> aceowner1 = new HashMap<String, Object>();
        aceowner1.put("owner", null);
        HashMap<String, Object> aceproperty1 = new HashMap<String, Object>();
        aceproperty1.put("property", aceowner1);
        HashMap<String, Object> ace1 = new HashMap<String, Object>();
        ace1.put("principal", aceproperty1);
        ace1.put("grant", ownergrant);
        acelist.add(ace1);
        
        HashMap<String, Object> aceowner2 = new HashMap<String, Object>();
        aceowner2.put("owner", null);
        HashMap<String, Object> aceproperty2 = new HashMap<String, Object>();
        aceproperty2.put("property", aceowner2);
        HashMap<String, Object> ace2 = new HashMap<String, Object>();
        ace2.put("principal", aceproperty2);
        HashMap<String, Object> aceall2 = new HashMap<String, Object>();
        aceall2.put("all", null);
        HashMap<String, Object> aceprivilege2 = new HashMap<String, Object>();
        aceprivilege2.put("privilege", aceall2);
        ace2.put("deny", aceprivilege2);
        acelist.add(ace2);
        
        HashMap<String, Object> acegroup3 = new HashMap<String, Object>();
        acegroup3.put("group", null);
        HashMap<String, Object> aceproperty3 = new HashMap<String, Object>();
        aceproperty3.put("property", acegroup3);
        HashMap<String, Object> ace3 = new HashMap<String, Object>();
        ace3.put("principal", aceproperty3);
        ace3.put("grant", groupgrant);
        acelist.add(ace3);
        
        HashMap<String, Object> acegroup4 = new HashMap<String, Object>();
        acegroup4.put("owner", null);
        HashMap<String, Object> aceproperty4 = new HashMap<String, Object>();
        aceproperty4.put("property", acegroup4);
        HashMap<String, Object> ace4 = new HashMap<String, Object>();
        ace4.put("principal", aceproperty4);
        HashMap<String, Object> aceall4 = new HashMap<String, Object>();
        aceall4.put("all", null);
        HashMap<String, Object> aceprivilege4 = new HashMap<String, Object>();
        aceprivilege4.put("privilege", aceall4);
        ace4.put("deny", aceprivilege4);
        acelist.add(ace4);
        
        HashMap<String, Object> aceall5 = new HashMap<String, Object>();
        aceall5.put("all", null);
        HashMap<String, Object> ace5 = new HashMap<String, Object>();
        ace5.put("principal", aceall5);
        ace5.put("grant", othergrant);
        acelist.add(ace5);
        
        ace.put("ace", acelist);
        return ace;
	}

}
