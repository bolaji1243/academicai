package com.schoolproject.app.common;

import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.metadata.Metadata;
import org.xml.sax.ContentHandler;

import java.io.InputStream;

public final class TextExtractor {

    private static final int MAX_BYTES = 512 * 1024;

    private static final AutoDetectParser PARSER = new AutoDetectParser();

    private TextExtractor() {}

    public static String extract(InputStream inputStream) {
        return extract(inputStream, MAX_BYTES, 12000);
    }

    public static String extract(InputStream inputStream, int maxBytes, int maxChars) {
        InputStream bounded = new BoundedInputStream(inputStream, maxBytes);
        ContentHandler handler = new BodyContentHandler(maxChars);
        try {
            PARSER.parse(bounded, handler, new Metadata());
        } catch (Exception e) {
            throw new RuntimeException("Tika parse failed: " + e.getMessage(), e);
        }
        return handler.toString();
    }

    private static class BoundedInputStream extends InputStream {
        private final InputStream delegate;
        private long remaining;

        BoundedInputStream(InputStream delegate, long maxBytes) {
            this.delegate = delegate;
            this.remaining = maxBytes;
        }

        @Override
        public int read() throws java.io.IOException {
            if (remaining <= 0) return -1;
            int b = delegate.read();
            if (b >= 0) remaining--;
            return b;
        }

        @Override
        public int read(byte[] buf, int off, int len) throws java.io.IOException {
            if (remaining <= 0) return -1;
            int toRead = (int) Math.min(len, remaining);
            int read = delegate.read(buf, off, toRead);
            if (read > 0) remaining -= read;
            return read;
        }

        @Override
        public void close() throws java.io.IOException {
            delegate.close();
        }
    }
}
