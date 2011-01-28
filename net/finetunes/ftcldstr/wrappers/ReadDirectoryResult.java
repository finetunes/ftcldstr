package net.finetunes.ftcldstr.wrappers;

import java.util.ArrayList;

public class ReadDirectoryResult extends AbstractWrapperResult {
    
    // list of objects in a directory
    private ArrayList<String> content = null;

    public ArrayList<String> getContent() {
        return content;
    }

    public void setContent(ArrayList<String> content) {
        this.content = content;
    }

}
