package net.finetunes.ftcldstr.wrappers;

import java.util.ArrayList;
import java.util.Arrays;

import net.finetunes.ftcldstr.wrappers.common.AbstractWrapper;
import net.finetunes.ftcldstr.wrappers.common.WrapperPathProvider;
import net.finetunes.ftcldstr.wrappers.types.ReadDirectoryResult;
import net.finetunes.ftcldstr.wrappers.types.WrapperResult;

public class ReadDirectoryContentWrapper extends AbstractWrapper {
    
    public static final String WRAPPER_ID = "READ_DIRECTORY_WRAPPER";
    
    public ReadDirectoryResult readDirectory(String directoryName) {
        
        ReadDirectoryResult result = new ReadDirectoryResult();
        wrapperPath = WrapperPathProvider.getWrapperPath(WRAPPER_ID);
        
        String[] args = new String[]{directoryName};
        WrapperResult externalCallResult = run(wrapperPath, args);
        
        result.setExitCode(externalCallResult.getExitCode());

        if (externalCallResult.getExitCode() != 0) {
            result.setErrorMessage(externalCallResult.getErrOutput());
        }
        else {
            String externalCallOutput = externalCallResult.getStdOutput();
            String[] content = externalCallOutput.split("\n");
            
            ArrayList<String> directoryContent = new ArrayList<String>(Arrays.asList(content));
            
            for (int i = directoryContent.size() - 1; i >= 0; i--) {
                if (directoryContent.get(i).equals(".") || directoryContent.get(i).equals("..")) {
                    directoryContent.remove(i);
                }
            }
            
            result.setContent(directoryContent);
        }
        
        return result;
    }
    

}