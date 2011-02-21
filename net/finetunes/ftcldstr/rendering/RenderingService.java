package net.finetunes.ftcldstr.rendering;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.helper.ConfigService;

/**
 * The rendering classes provide a web interface for the WebDAV protocol. 
 * The web interface consists of 3 pages (file/folder browser, properties view, 
 * and search view). 
 * 
 * The rendering classes receive requests from the web application, process them, 
 * make a call to the protocol implementation classes to get the required 
 * WebDAV data, wrap the result into the html page and return in the response.
 *
 */

public class RenderingService {
	
	public static String start_html(String title) {

	    String content = "";
	    content += "<!DOCTYPE html>\n";
	    content += "<head><title>" + RenderingHelper.escapeHTML(title) + "</title>";
	    content += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + ConfigService.CHARSET + "\"/>";
	    content += "<meta name=\"author\" content=\"Daniel Rohde\"/>";
	    content += "</head><body>";
	    
	    return content;	    
	}
	
	public static String getPageNavBar(RequestParams requestParams, String ru, int count) {
	
	    
	    int limit = ConfigService.PAGE_LIMIT;
	    if (limit < 0) {
	        limit = -1;
	    }
	    
	    int showall = 0;
	    if (requestParams.requestParamExists("showall")) {
	        showall = Integer.parseInt(requestParams.getRequestParam("showall"));
	    }
	    
	    String order = "name";
        if (requestParams.requestParamExists("order")) {
            order = requestParams.getRequestParam("order");
        }
        
        int page = 1;
        if (requestParams.requestParamExists("page")) {
            page = Integer.parseInt(requestParams.getRequestParam("page"));
        }
        
        String content = "";
        if (limit < 1 || count < limit) {
            return content;
        }
        
        if (showall != 0) {
            content = "<div style=\"font-weight: bold; font-size:0.9em;padding: 10px 0px 10px 0px\">";
            content += "<a href=\"" + ru +"?order=" + order + "\" style=\"text-decoration:none;\">";
            content += ConfigService.stringMessages.get("navpageview");
            content += "</a>";
            content += "</div>";
            return content;
        }
        
        int maxpages = count / limit;
        if (count % limit > 0) {
            maxpages++;
        }
        
        content += ConfigService.stringMessages.get("navpage") + page + "/" + maxpages + ": ";
        
        if (page > 1) {
            content += "<a href=\"" + ru + "?order=" + order + ConfigService.URL_PARAM_SEPARATOR + "page=1\"" +
            		        " title=\"" + ConfigService.stringMessages.get("navfirsttooltip") + "\"" +
            		        " style=\"text-decoration:none\">";
            content += ConfigService.stringMessages.get("navfirst");
            content += "</a>";
        }
        else {
            content += ConfigService.stringMessages.get("navfirstblind");
        }
        
        if (page > 1) {
            content += "<a href=\"" + ru + "?order=" + order + ConfigService.URL_PARAM_SEPARATOR + "page=" + (page - 1) + "\"" +
                            " title=\"" + ConfigService.stringMessages.get("navprevtooltip") + "\"" +
                            " style=\"text-decoration:none\">";
            content += ConfigService.stringMessages.get("navprev");
            content += "</a>";
        }
        else {
            content += ConfigService.stringMessages.get("navprevblind");
        } 
        
        content += String.format("%02d-%02d/%d", ((limit * (page - 1)) + 1), ((page < maxpages || count % limit == 0) ? limit * page : (limit * (page - 1)) + count % limit), count);
        
        if (page < maxpages) {
            content += "<a href=\"" + ru + "?order=" + order + ConfigService.URL_PARAM_SEPARATOR + "page=" + (page + 1) + "\"" +
                            " title=\"" + ConfigService.stringMessages.get("navnexttooltip") + "\"" +
                            " style=\"text-decoration:none\">";
            content += ConfigService.stringMessages.get("navnext");
            content += "</a>";
        }
        else {
            content += ConfigService.stringMessages.get("navnextblind");
        }
        
        if (page < maxpages) {
            content += "<a href=\"" + ru + "?order=" + order + ConfigService.URL_PARAM_SEPARATOR + "page=" + (maxpages) + "\"" +
                            " title=\"" + ConfigService.stringMessages.get("navlasttooltip") + "\"" +
                            " style=\"text-decoration:none\">";
            content += ConfigService.stringMessages.get("navlast");
            content += "</a>";
        }
        else {
            content += ConfigService.stringMessages.get("navlastblind");
        }
        
        content += "<a href=\"" + ru + "?order=" + order + ConfigService.URL_PARAM_SEPARATOR + "showall=1\" " +
        		        " style=\"text-decoration:none\"" +
        		        " title=\"" + ConfigService.stringMessages.get("navalltooltip") + "\">";
        content += ConfigService.stringMessages.get("navall");
        content += "</a>";
        
        content = "<div style=\"font-weight: bold; font-size:0.9em;padding: 10px 0px 10px 0px\">" + content + "</div>";
        
        return content;
	}
	
	public static String getQuickNavPath(String ru, String query) {
		
	    ru = RenderingHelper.uri_unescape(ru);
	    
	    String content = "";
	    String path = "";
	    String[] pe = ru.split("/");
	    for (int i = 0; i < pe.length; i++) {
	        path += RenderingHelper.uri_escape(pe[i]) + "/";
	        if (path.equals("//")) {
	            path = "/";
	        }
	        
	        String href = path;
	        if (query != null && !query.isEmpty()) {
	            href += "?" + query;
	        }
            content += "<a href=\"" + href + "\" title=\"" + path + "\">" + pe[i] + "/";
            content += "</a>";
	    }
	    
	    if (content.isEmpty()) {
            content += "<a href=\"/\" title=\"/\">/</a>";
	    }
	    
	    content += "";
	    return content;
	}
	
	
    public static String getQuickNavPath(String ru) {
        
        return getQuickNavPath(ru, null);
    }
    
	public static String createMsgQuery(String msg, String msgparam,
				String errmsg, String errmsgparam) {
	
	    String query = "";
        if (msg != null && !msg.isEmpty()) {
            query += ConfigService.URL_PARAM_SEPARATOR + "msg=" + msg;
        }
        if (msg != null && !msg.isEmpty() && msgparam != null && !msgparam.isEmpty()) {
            query += ConfigService.URL_PARAM_SEPARATOR + msgparam;
        }
        if (errmsg != null && !errmsg.isEmpty()) {
            query += ConfigService.URL_PARAM_SEPARATOR + "errmsg=" + errmsg;
        }
        if (errmsg != null && !errmsg.isEmpty() && errmsgparam != null && !errmsgparam.isEmpty()) {
            query += ConfigService.URL_PARAM_SEPARATOR + errmsgparam;
        }
        
        query = "?t=" + System.currentTimeMillis() + query;

        return query;
	}
	
	// additional routines
	
    public static String end_html() {
        
        String content = "</body></html>";
        return content;
    }

}
