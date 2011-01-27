package net.finetunes.ftcldstr.routines.fileoperations;

import java.util.Comparator;

import net.finetunes.ftcldstr.helper.MIMETypesHelper;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService.StatData;

public class FilenameComparator implements Comparator<String> {
    
    private String pathTranslated;
    private String order;
    
    public FilenameComparator(String pathTranslated, String order) {
        this.pathTranslated = pathTranslated;
        this.order = order;
    }
    
    public int compare(String a, String b) {
    
        String fp_a = pathTranslated + a;
        String fp_b = pathTranslated + b;
        
        if (FileOperationsService.is_directory(fp_a) && !FileOperationsService.is_directory(b)) {
            return -1;
        }
        
        if (!FileOperationsService.is_directory(fp_a) && FileOperationsService.is_directory(b)) {
            return 1;
        }
        
        if (order != null && !order.isEmpty()) {
            if (order.matches("(lastmodified|size|mode).*")) {
                StatData a_stats = FileOperationsService.stat(fp_a);
                StatData b_stats = FileOperationsService.stat(fp_b);

                int idx = 7;
                if (order.matches(".*(lastmodified).*")) {
                    idx = 9;
                }
                else if (order.matches(".*(mode).*")) {
                    idx = 2;
                }
                
                return compareStat(a_stats, b_stats, a, b, idx, order.matches(".*(_desc)"));
            }
            else if (order.matches(".*(mimetype).*")) {
                String a_mime = MIMETypesHelper.getMIMEType(a);
                String b_mime = MIMETypesHelper.getMIMEType(b);
                
                if (!a_mime.equals(b_mime)) {
                    if (order.matches(".*(_desc)")) {
                        if (b_mime.compareTo(a_mime) != 0) {
                            return b_mime.compareTo(a_mime);
                        }
                        else {
                            return b.compareToIgnoreCase(a);
                        }
                    }
                    
                    if (a_mime.compareTo(b_mime) != 0) {
                        return a_mime.compareTo(b_mime);
                    }
                    else {
                        return a.compareToIgnoreCase(b);
                    }
                }
            }
        }
        
        return a.compareToIgnoreCase(b);
    }
    
    private int compareStat(StatData astat, StatData bstat, String a, String b, int idx, boolean desc) {
        
        int c = 0;
        switch (idx) {
        case 2: // mode
            c = astat.getMode() - bstat.getMode();
            if (desc) {
                c = -c;
            }
            return c;
        case 7: // size
            c = astat.getSize() - bstat.getSize();
            if (desc) {
                c = -c;
            }
            return c;
        case 9: // last modified
            // TODO: implement
            // depends on the returning type;
            // Date?
            break;
        }
        
        c = a.compareToIgnoreCase(b);
        if (desc) {
            c = -c;
        }
        
        return c;
    }

}
