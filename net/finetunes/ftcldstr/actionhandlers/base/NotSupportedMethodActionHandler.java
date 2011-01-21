package net.finetunes.ftcldstr.actionhandlers.base;

import net.finetunes.ftcldstr.RequestParams;
import net.finetunes.ftcldstr.rendering.OutputService;

public class NotSupportedMethodActionHandler extends AbstractActionHandler {

    public void handle(final RequestParams requestParams) {

        OutputService.printHeaderAndContent(requestParams, "405 Method not allowed", "text/html");
    }
}
