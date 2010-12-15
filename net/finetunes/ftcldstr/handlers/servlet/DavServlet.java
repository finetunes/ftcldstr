package net.finetunes.ftcldstr.handlers.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DavServlet extends MyServlet {

    public DavServlet() throws Exception {
        super();
    }

    public void doHead(final HttpServletRequest request, final HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

    }

    @SuppressWarnings("unchecked")
    public void doPGU(final HttpServletRequest request, final HttpServletResponse response,
            final String method) {
    	
    	// request processing

        String pathinfo = request.getPathInfo();
        if (pathinfo == null) {
            pathinfo = "";
        }

        System.out.println("Pathinfo: [" + pathinfo + "]");

        String cmd = null;
        
        try {
        	response.getOutputStream().write(("Hallo, I'm a WebDAV servlet.").getBytes());
        }
        catch (IOException e) {
        	System.out.println("Something went wrong.");
        }
//
//        if (cmd == null || cmd.equals("")) {
//            // cmd="index";
//            cmd = "AccountingContacts"; // new default value
//        }
//        if ("AccountingContacts".equals(cmd)) {
//
//            ma = new AccountingContactsAction(request, response);
//        }
//        else if ("AccountingContactDetails".equals(cmd)) {
//
//            ma = new AccountingContactDetailsAction(request, response);
//        }
//        else if ("Accounting".equals(cmd)) {
//
//            ma = new AccountingAction(request, response);
//        }
//        else if ("Products".equals(cmd)) {
//
//            ma = new ProductsAction(request, response);
//        }
//        else if ("Contracts".equals(cmd)) {
//
//            ma = new ContractsAction(request, response);
//        }
//        else if ("Logout".equals(cmd)) {
//            ma = new UserLoginAction(request, response);
//        }
//        else if ("Admin".equals(cmd)) {
//            ma = new UserAdminAction(request, response);
//        }
//        else if ("UserDetails".equals(cmd)) {
//            ma = new UserDetailsAction(request, response);
//        }
//        else if ("NewUser".equals(cmd)) {
//            ma = new NewUserAction(request, response);
//        }
//        // ....
//        else if ("Profile".equals(cmd)) {
//
//            ma = new ProfileAction(request, response);
//        }
//
//        if (ma == null) {
//            ma = new InvalidRequestAction(request, response);
//        }
//
//        if (ma != null) {
//            ma.mode = method;
//
//            VelocityContext c = new VelocityContext(); // staticWM.getContext();//HT
//            // 07.02.2009 not needed
//            // till now - we could
//            // also try "apache
//            // velocity"...
//
//            try {
//                ma.performAllAction();
//
//                ma.conjoinParams(args); // das hier fГјgt aus den per get/post
//                // Гјbergebenen parametern die zu per
//                // pathinfo angegebenen hinzu
//
//                for (int zi = 0, to = ma.gimmeParameterCount(); zi < to; zi++) {
//                    c.put("param_" + ma.gimmeNameAt(zi), ma.gimmeValueAt(zi));
//                }
//
//                c.put("ma", ma);
//                c.put("encoding", ma.encoding);
//                c.put("cmd", cmd);
//
//                c.put("reqaddress", request.getRemoteAddr());
//                c.put("scheme", request.getScheme());
//
//                c.put("querystring", request.getQueryString());
//                if (c.get("querystring") == null) {
//                    c.put("querystring", "");
//                }
//                String requesturl = request.getRequestURL().toString();
//
//                c.put("request", request);
//                c.put("requesturl", requesturl);
//                c.put("server", request.getServerName());
//                c.put("port", request.getServerPort());
//
//                c.put("LinkProvider", LinkProvider.class);
//                c.put("BasePath", LinkProvider.getContextPathForHTML());
//                c.put("ConstantProvider", ConstantProvider.class);
//                c.put("MenuItemHighlightingHelper", MenuItemHighlightingHelper.class);
//
//                System.out.println("Q: " + request.getQueryString());
//
//                String ref = request.getHeader("Referer");
//
//                if (ref == null) {
//                    ref = "#";
//                }
//
//                c.put("referer", ref);
//
//                // additional check whether the user is logged in
//                if (ma.user != null && ma.user.loggedin) {
//                    Session.putSessiondataToContext(ma.user, c);
//                    ma.performAction(request.getRemoteAddr(), c);
//                    Session.saveSession(ma.user);
//                }
//                else if (ma instanceof AdminAction) {
//                    ma.performAction(request.getRemoteAddr(), c);
//                }
//            }
//            catch (DBConnectionProblemException ex) {
//                System.out.println("[DB CONNECION PROBLEM]");
//
//                try {
//                    ma.response.getOutputStream().print("Problem connecting to DB.");
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            catch (UserNotAuthenticatedException e) {
//                // the user is not authenticated
//                // show login screen
//                try {
//                    c.put("BasePath", LinkProvider.getContextPathForHTML());
//                    ma = new UserLoginAction(request, response);
//                    ma.getParametersFromRequest();
//
//                    String actionAddress = request.getRequestURI();
//                    String queryString = request.getQueryString();
//
//                    if (queryString != null && !queryString.equals("")) {
//                        actionAddress += "?" + queryString;
//                    }
//
//                    c.put("actionaddress", actionAddress);
//                    ma.performAction(request.getRemoteAddr(), c);
//                }
//                catch (Exception loginPageDisplay) {
//                    System.out.println("[UNKNOWN EXCEPTION ON LOGIN PAGE DISPLAY]");
//                    System.out.println(loginPageDisplay.getMessage());
//                    loginPageDisplay.printStackTrace();
//                }
//            }
//            catch (TemplateRenderingException e) {
//                // if we got here even error display tempate can't be rendered.
//                logger.error("Critical template problem.", e);
//                System.err.println("Critical template error. See log for details.");
//            }
//            catch (Exception ex) {
//                System.out.println("[UNKNOWN EXCEPTION]");
//                ex.printStackTrace();
//
//            }
//        } // ma!=null
    }

    public void doGet(final HttpServletRequest request, final HttpServletResponse response) {

        doPGU(request, response, "GET");
    }

    public void doPost(final HttpServletRequest request, final HttpServletResponse response) {

        doPGU(request, response, "POST");
    }

    public void init(final ServletConfig config) throws ServletException  {
        super.init(config);
    }

    public void service(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException,
            IOException {
        super.service(request, response);
    }
}
