package net.finetunes.ftcldstr.wrappers;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class to store the raw result from a wrapper after an async call.
 *
 */
public class AsyncCallResult {
    
    private InputStream inputStream = null;
    private InputStream errorStream = null;
    private OutputStream outputStream = null;
    
    public AsyncCallResult(InputStream inputStream,
            InputStream errorStream, OutputStream outputStream) {
        super();
        this.inputStream = inputStream;
        this.errorStream = errorStream;
        this.outputStream = outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getErrorStream() {
        return errorStream;
    }

    public void setErrorStream(InputStream errorStream) {
        this.errorStream = errorStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
