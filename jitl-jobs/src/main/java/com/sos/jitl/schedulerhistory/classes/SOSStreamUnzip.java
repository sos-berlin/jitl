package com.sos.jitl.schedulerhistory.classes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;

import org.apache.http.util.ByteArrayBuffer;

public class SOSStreamUnzip {

    private static int BUFFER = 4096;

    public static byte[] unzip(byte[] source) throws IOException {
        return unzip(source, BUFFER);
    }

    public static byte[] unzip(byte[] source, int bufferSize) throws IOException {
        if (source == null) {
           return null; 
        }
        InputStream is = new GZIPInputStream(new ByteArrayInputStream(source));
        try {
            final ByteArrayBuffer byteBuffer = new ByteArrayBuffer(bufferSize);
            byte[] buffer = new byte[bufferSize];
            int l;
            while ((l = is.read(buffer)) != -1) {
                byteBuffer.append(buffer, 0, l);
            }
            return byteBuffer.toByteArray();
        } finally {
            try {
                is.close();
                is = null;
            } catch (IOException e) {
            }
        }
    }
    
    public static Path unzipToFile(byte[] source, String prefix) throws IOException {
        if (source == null) {
            return null;
        }
        InputStream is = new GZIPInputStream(new ByteArrayInputStream(source));
        Path path = null;
        try {
            path = Files.createTempFile(prefix, null);
            Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
            return path;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e1) {
            }
            throw e;
        } finally {
            try {
                is.close();
                is = null;
            } catch (IOException e) {
            }
        }
    }
    
    public static Path zippedToFile(byte[] source, String prefix) throws IOException {
        if (source == null) {
            return null;
        }
        InputStream is = new ByteArrayInputStream(source);
        Path path = null;
        try {
            path = Files.createTempFile(prefix, null);
            Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
            return path;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e1) {
            }
            throw e;
        } finally {
            try {
                is.close();
                is = null;
            } catch (IOException e) {
            }
        }
    }

    public static String unzip2String(byte[] source) throws IOException {
        return unzip2String(source, BUFFER);
    }

    public static String unzip2String(byte[] source, int bufferSize) throws IOException {
        return new String(unzip(source, bufferSize), "UTF-8");
    }

}
