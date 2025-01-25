
package org.twins.core.featurer.resource.inputstream;

import java.io.IOException;
import java.io.InputStream;

public class LimitedSizeInputStream extends InputStream {
    public static class SizeExceededException extends RuntimeException {
        final public int limit;
        final public long bytesRead;

        public SizeExceededException(String message, int limit, long bytesRead) {
            super(message);
            this.limit = limit;
            this.bytesRead = bytesRead;
        }
    }

    protected final InputStream inputStream;
    protected final Integer sizeLimit;
    protected long bytesRead = 0L;
    protected int markPosition = 0;

    public LimitedSizeInputStream(InputStream inputStream, Integer sizeLimit) {
        this.inputStream = inputStream;
        this.sizeLimit = sizeLimit;
    }

    protected boolean dontHaveToLimit() {
        return sizeLimit < 0 || sizeLimit == Integer.MAX_VALUE;
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        if (dontHaveToLimit()) {
            return inputStream.readAllBytes();
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
        if (dontHaveToLimit()) {
            return inputStream.readNBytes(b, off, len);
        }
        long readBefore = bytesRead;
        int read = inputStream.readNBytes(b, off, len);
        long uncountedBytesRead = read - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        if (bytesRead > sizeLimit) {
            throwException();
        }
        return read;
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        if (dontHaveToLimit()) {
            inputStream.skipNBytes(n);
            return;
        }
        long readBefore = bytesRead;
        inputStream.skipNBytes(n);
        long uncountedBytesRead = n - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        if (bytesRead > sizeLimit) {
            throwException();
        }
    }
    @Override
    public long skip(long n) throws IOException {
        if (dontHaveToLimit()) {
            return inputStream.skip(n);
        }
        long readBefore = bytesRead;
        long skipped = inputStream.skip(n);
        long uncountedBytesRead = skipped - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        if (bytesRead > sizeLimit) {
            throwException();
        }
        return skipped;
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (dontHaveToLimit()) {
            return inputStream.read(b, off, len);
        }
        long readBefore = bytesRead;
        int read = inputStream.read(b, off, len);
        long uncountedBytesRead = read - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        if (bytesRead > sizeLimit) {
            throwException();
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (dontHaveToLimit()) {
            return inputStream.read(b);
        }
        long readBefore = bytesRead;
        int read = inputStream.read(b);
        long uncountedBytesRead = read - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        if (bytesRead > sizeLimit) {
            throwException();
        }
        return read;
    }

    @Override
    public byte[] readNBytes(int count) throws IOException {
        if (dontHaveToLimit()) {
            return inputStream.readNBytes(count);
        }
        long readBefore = bytesRead;
        byte[] bytes = inputStream.readNBytes(count);
        long uncountedBytesRead = bytes.length - (bytesRead - readBefore);
        bytesRead += uncountedBytesRead;
        if (bytesRead > sizeLimit) {
            throwException();
        }
        return bytes;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public int read() throws IOException {
        int nextByte = inputStream.read();
        if (dontHaveToLimit()) {
            return nextByte;
        }
        if (nextByte != -1) {
            bytesRead += 1;
            if (bytesRead > sizeLimit) {
                throwException();
            }
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
        if (!dontHaveToLimit()) {
            markPosition = readlimit;
        }
    }

    @Override
    public void reset() throws IOException {
        inputStream.reset();
        if (!dontHaveToLimit()) {
            bytesRead = markPosition;
        }
    }

    protected void throwException() {
        throw new SizeExceededException("File size limit " + sizeLimit + " exceeded (" + bytesRead + " bytes)", sizeLimit, bytesRead);
    }

}