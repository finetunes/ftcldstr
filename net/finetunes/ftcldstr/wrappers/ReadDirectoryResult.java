package net.finetunes.ftcldstr.wrappers;

import java.util.List;

public class ReadDirectoryResult extends AbstractWrapperResult {
    
    // list of objects in a directory
    private List<String> content = null;

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

}
