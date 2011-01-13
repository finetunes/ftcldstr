package net.finetunes.ftcldstr.rendering;

public class RenderingHelper {
	
	public static String getmodecolors(String filename, String mode) {
		
		// TODO: implement
		return null;
		
	}
	
	public static String mode2str(String filename, String mode) {
		
		// TODO: implement
		return null;
		
	}
	
    public static String HTMLEncode(String s) {
        StringBuffer raus = new StringBuffer();
        char c;

        for (int oo = 0; s != null && oo < s.length(); oo++) {
            c = s.charAt(oo);

            if (c < 65 || c > 122 || c == '\'' || c == '"') {
                raus.append("&#");
                raus.append((int) c);
                raus.append(';');
            } else {
                raus.append(c);
            }
        }

        return raus.toString();
    }
	

}
