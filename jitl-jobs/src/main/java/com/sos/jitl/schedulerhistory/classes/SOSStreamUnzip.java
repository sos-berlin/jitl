package com.sos.jitl.schedulerhistory.classes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

/** @author Uwe Risse */
public class SOSStreamUnzip {

    private byte[] source;
    private int bufferSize = 8192;

    public SOSStreamUnzip(byte[] source_) {
        source = source_;
    }

    public byte[] unzip() throws IOException {
        InputStream is = null;
        OutputStream os = null;
        is = new GZIPInputStream(new ByteArrayInputStream(source));
        os = new ByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];
        for (int length; (length = is.read(buffer)) >= 0;) {
            os.write(buffer, 0, length);
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return ((ByteArrayOutputStream) os).toByteArray();
    }

    public String unzip2String() throws IOException {
        return new String(unzip());
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
