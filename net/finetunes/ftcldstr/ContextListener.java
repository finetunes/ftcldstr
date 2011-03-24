package net.finetunes.ftcldstr;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.finetunes.ftcldstr.helper.InitializationService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.wrappers.WrappingUtilities;

/**
 * Starts initialisation of the servlet as soon as 
 * servlet context is available.
 *
 */
public class ContextListener implements ServletContextListener {
    
    public void contextInitialized(ServletContextEvent contextEvent) {
        Logger.log("Servlet context created. Starting initialization.");
        InitializationService.init(contextEvent.getServletContext());
    }
    
    public void contextDestroyed(ServletContextEvent contextEvent) {
        
    }
}