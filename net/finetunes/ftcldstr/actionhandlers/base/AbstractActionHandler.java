package net.finetunes.ftcldstr.actionhandlers.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.finetunes.ftcldstr.RequestParams;


public abstract class AbstractActionHandler {
    
    // TODO: remove this method
    public void handle() {
        System.out.println("Abstract function with no handler called.");
    }

    // TODO: add abstract declaration
    public void handle(final RequestParams requestParams) {
        System.out.println("Abstract function with no handler called.");
    }

}
