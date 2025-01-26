
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
        if (inputStream instanceof ByteArrayInputStream) {
            this.bytesRead = ((ByteArrayInputStream) inputStream).available()+bytesRead;
            if (haveToCheckSize &&  this.bytesRead > sizeLimit) {
                this.sizeLimit = sizeLimit;
                throwException();
            } else {
                this.sizeLimit = null;
            }
        } else {
            this.bytesRead = bytesRead;
            this.sizeLimit = sizeLimit;
            if (haveToCheckSize && this.bytesRead > sizeLimit) {
                throwException();
            }
        }
        this.inputStream = inputStream;

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
        return sizeLimit < 0 || sizeLimit == Integer.MAX_VALUE;
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
        if (dontHaveToLimit()) {
            long readBefore = bytesRead;
            int bytes = inputStream.readNBytes(b, off, len);
            if (bytes == -1) {
                return bytes;
            }
            long uncountedBytesRead = bytes - (bytesRead - readBefore);
            bytesRead += uncountedBytesRead;
            return bytes;
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
            long readBefore = bytesRead;
            inputStream.skipNBytes(n);
            long uncountedBytesRead = n - (bytesRead - readBefore);
            bytesRead += uncountedBytesRead;
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
            long readBefore = bytesRead;
            long bytes = inputStream.skip(n);
            long uncountedBytesRead = bytes - (bytesRead - readBefore);
            bytesRead += uncountedBytesRead;
            return bytes;
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
            long readBefore = bytesRead;
            int bytes = inputStream.read(b, off, len);
            if (bytes == -1) {
                return bytes;
            }
            long uncountedBytesRead = bytes - (bytesRead - readBefore);
            bytesRead += uncountedBytesRead;
            return bytes;
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
            long readBefore = bytesRead;
            int bytes = inputStream.read(b);
            if (bytes == -1) {
                return bytes;
            }
            long uncountedBytesRead = bytes - (bytesRead - readBefore);
            bytesRead += uncountedBytesRead;
            return bytes;
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
            long readBefore = bytesRead;
            byte[] bytes = inputStream.readNBytes(count);
            long uncountedBytesRead = bytes.length - (bytesRead - readBefore);
            bytesRead += uncountedBytesRead;
            return bytes;
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
        if (nextByte != -1) {
            bytesRead += 1;
            if (!dontHaveToLimit() && bytesRead > sizeLimit) {
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

    public static void main(String[] args) {
        try {
            byte[] data = "Example input stream data".getBytes();
            InputStream inputStream = new ByteArrayInputStream(data);

            // Set a size limit lower than the data length
            int sizeLimit = 10;

            // Instantiate LimitedSizeInputStream
            LimitedSizeInputStream limitedStream = new LimitedSizeInputStream(inputStream, sizeLimit);

            // Use the LimitedSizeInputStream to read data
            try {
                byte[] buffer = limitedStream.readAllBytes();
                System.out.println(new String(buffer));
            } catch (LimitedSizeInputStream.SizeExceededException e) {
                System.err.println(e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}