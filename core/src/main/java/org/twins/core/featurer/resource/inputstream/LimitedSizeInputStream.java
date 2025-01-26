
package org.twins.core.featurer.resource.inputstream;

import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LimitedSizeInputStream extends InputStream {
    protected final InputStream inputStream;
    protected final Integer sizeLimit;
    @Getter
    protected long bytesRead;
    protected int markPosition = 0;

    public LimitedSizeInputStream(InputStream inputStream, Integer sizeLimit) {
        this(inputStream, sizeLimit, 0L);
    }

    public LimitedSizeInputStream(InputStream inputStream, Integer sizeLimit, long bytesRead) {
        boolean haveToCheckSize = sizeLimit != null && sizeLimit != -1 && sizeLimit != Integer.MAX_VALUE;
        this.bytesRead = bytesRead;
        this.sizeLimit = sizeLimit;
        this.inputStream = inputStream;
        if (inputStream instanceof ByteArrayInputStream) {
            if (haveToCheckSize && (this.bytesRead + ((ByteArrayInputStream) inputStream).available()) > this.sizeLimit) {
                throwException();
            }
        }
        if (haveToCheckSize && this.bytesRead > this.sizeLimit) {
            throwException();
        }
    }

    public static class SizeExceededException extends RuntimeException {
        final public int limit;
        final public long bytesRead;

        public SizeExceededException(String message, int limit, long bytesRead) {
            super(message);
            this.limit = limit;
            this.bytesRead = bytesRead;
        }
    }


    protected boolean dontHaveToLimit() {
        return sizeLimit==null||sizeLimit < 0 || sizeLimit == Integer.MAX_VALUE;
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        if (dontHaveToLimit()) {
            long readBefore = bytesRead;
            byte[] bytes = inputStream.readAllBytes();
            long uncountedBytesRead = bytes.length - (bytesRead - readBefore);
            bytesRead += uncountedBytesRead;
            return bytes;
        }
        Long bytesLeft = (sizeLimit.intValue() - bytesRead);
        byte[] tillLimit = readNBytes(bytesLeft.intValue());
        if (tillLimit.length < bytesLeft.intValue()) {
            return tillLimit;
        }
        //Check if there is some more bytes
        int additionalByte = read();
        return tillLimit;
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        long readBefore = bytesRead;
        int read = inputStream.readNBytes(b, off, len);
        long uncountedBytesRead = read - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        checkLimit();
        return read;
    }

    private void checkLimit() {
        if (!dontHaveToLimit() && bytesRead > sizeLimit) {
            throwException();
        }
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        long readBefore = bytesRead;
        inputStream.skipNBytes(n);
        long uncountedBytesRead = n - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        checkLimit();
    }

    @Override
    public long skip(long n) throws IOException {
        long readBefore = bytesRead;
        long skipped = inputStream.skip(n);
        long uncountedBytesRead = skipped - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        checkLimit();
        return skipped;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long readBefore = bytesRead;
        int read = inputStream.read(b, off, len);
        long uncountedBytesRead = read - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        checkLimit();
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        long readBefore = bytesRead;
        int read = inputStream.read(b);
        long uncountedBytesRead = read - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        checkLimit();
        return read;
    }

    @Override
    public byte[] readNBytes(int count) throws IOException {
        long readBefore = bytesRead;
        byte[] bytes = inputStream.readNBytes(count);
        long uncountedBytesRead = bytes.length - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        checkLimit();
        return bytes;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public int read() throws IOException {
        int nextByte = inputStream.read();
        if (nextByte != -1) {
            bytesRead += 1;
            checkLimit();
        }
        return nextByte;
    }


    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void mark(int readlimit) {
        inputStream.mark(readlimit);
        markPosition = readlimit;
    }

    @Override
    public void reset() throws IOException {
        inputStream.reset();
        bytesRead = markPosition;

    }

    protected void throwException() {
        throw new SizeExceededException("File size limit " + sizeLimit + " exceeded (" + bytesRead + " bytes)", sizeLimit, bytesRead);
    }


}