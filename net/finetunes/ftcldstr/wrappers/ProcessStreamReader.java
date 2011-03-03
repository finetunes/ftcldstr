package net.finetunes.ftcldstr.wrappers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Vector;

/**
 * Class to read output io streams (output/error) of a process.
 * 
 */
public class ProcessStreamReader extends Thread {

    private InputStream inputStream;
    private Vector<String> stream;
    boolean streamClosed = false;

    /**
     * Constructor for ProcessStreamReader
     * 
     * @param inputStream input stream to read from
     * @param streamType the name of the stream
     */
    ProcessStreamReader(InputStream inputStream) {

        this.inputStream = inputStream;

        stream = new Vector<String>();
    }

    /**
     * Starts reading from the stream.
     */
    public void run() {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("utf8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                stream.add(line);
            }
        } catch (IOException e) {
            // The stream seems to be closed.
            // Most likely the process was terminated.
            streamClosed = true;
        }
    }

    /**
     * Gets the current line count read from the stream. Used as a index to start search from in the
     * multiline output.
     * 
     * @return current line count read from the stream
     */
    public int getCurrentIndexPosition() {
        return stream.size();
    }

    /**
     * Returns the output line value read from the stream. Used to find a particular line with the
     * known prefix in the multiline output.
     * 
     * @param prefix prefix of the line to match
     * @param startIndex index of the line to start search from
     * @return
     */
    public String getOutput() {
        if (stream != null) {

            StringBuffer output = new StringBuffer();
            for (int i = 0; i < stream.size(); i++) {
                output.append(stream.get(i) + "\n");
            }

            return output.toString();
        }

        return null;
    }
}