package net.finetunes.ftcldstr.routines.fileoperations;

import java.util.Comparator;

import net.finetunes.ftcldstr.helper.MIMETypesHelper;

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
                Object[] a_stats = FileOperationsService.stat(fp_a);
                Object[] b_stats = FileOperationsService.stat(fp_b);

                int idx = 7;
                if (order.matches(".*(lastmodified).*")) {
                    idx = 9;
                }
                else if (order.matches(".*(mode).*")) {
                    idx = 2;
                }
                
                if (!a_stats[idx].equals(a_stats[idx])) {
                    if (order.matches(".*(_desc)")) {

                        // TODO: check whether this code works correctly
                        if (((Comparable)b_stats[idx]).compareTo(a_stats[idx]) != 0) {
                            return ((Comparable)b_stats[idx]).compareTo(a_stats[idx]);
                        }
                        else {
                            return b.compareToIgnoreCase(a);
                        }
                    }
                    
                    // TODO: check whether this code works correctly
                    if (((Comparable)a_stats[idx]).compareTo(b_stats[idx]) != 0) {
                        return ((Comparable)a_stats[idx]).compareTo(b_stats[idx]);
                    }
                    else {
                        return a.compareToIgnoreCase(b);
                    }
                }
                
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

}
