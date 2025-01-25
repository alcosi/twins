package org.twins.core.featurer.resource.inputstream;

import java.io.*;

public class CachedReadInputStream extends InputStream {
    protected final InputStream inputStream;
    protected final boolean closeStream;

    protected final ByteArrayOutputStream readOutputStream = new ByteArrayOutputStream();

    public CachedReadInputStream(InputStream inputStream, boolean closeStream) {
        this.inputStream = inputStream;
        this.closeStream = closeStream;
    }

    public PushbackInputStream toUnreadPushbackInputStream() {
        byte[] alreadyRead = flushAndGetBufferBytes();
        int secondPartOfStreamReadStartsAt = alreadyRead.length;
        PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream, secondPartOfStreamReadStartsAt);
        try {
            pushbackInputStream.unread(alreadyRead);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pushbackInputStream;
    }

    public byte[] flushAndGetBufferBytes() {
        try {
            readOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return readOutputStream.toByteArray();
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        readOutputStream.write(bytes);
        return bytes;
    }

    @Override
    public byte[] readNBytes(int count) throws IOException {
        byte[] bytes = inputStream.readNBytes(count);
        readOutputStream.write(bytes);
        return bytes;
    }

    @Override
    public int read(byte[] b) throws IOException {
        byte[] bytes = inputStream.readNBytes(b.length);
        readOutputStream.write(bytes);
        if (bytes.length == 0) {
            return -1;
        }
        System.arraycopy(bytes, 0, b, 0, bytes.length);
        return bytes.length;
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        byte[] bytes = inputStream.readNBytes(len);
        readOutputStream.write(bytes);
        if (bytes.length == 0) {
            return -1;
        }
        System.arraycopy(bytes, 0, b, off, bytes.length);
        return bytes.length;
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        byte[] bytes = inputStream.readNBytes((int) n);
        readOutputStream.write(bytes);
        if (bytes.length != (int) n) {
            // skipped negative or too many bytes
            throw new IOException("Unable to skip exactly");
        }
    }

    @Override
    public long skip(long n) throws IOException {
        byte[] bytes = inputStream.readNBytes((int) n);
        readOutputStream.write(bytes);
        if (bytes.length == 0) {
            return 0;
        }
        return bytes.length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        byte[] bytes = inputStream.readNBytes(len);
        readOutputStream.write(bytes);
        if (bytes.length == 0) {
            return -1;
        }
        System.arraycopy(bytes, 0, b, off, bytes.length);
        return bytes.length;
    }

    @Override
    public void close() throws IOException {
        if (closeStream) {
            inputStream.close();
        }
    }

    @Override
    public int read() throws IOException {
        int nextByte = inputStream.read();
        if (nextByte != -1) {
            readOutputStream.write(nextByte);
        }
        return nextByte;
    }

    @Override
    public boolean markSupported() {
        return super.markSupported();
    }

    @Override
    public int available() throws IOException {
        return super.available();
    }

    @Override
    public void mark(int readlimit) {
        super.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
    }
}