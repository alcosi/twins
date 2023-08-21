package org.cambium.common.exception;

import org.apache.commons.lang3.StringUtils;

public class SimpleLogException extends Exception  {
    public SimpleLogException() {
        super();
    }

    public SimpleLogException(String message) {
        super(message);
    }

    public String getShortTrace(int depth) {
        StringBuilder ret = new StringBuilder(this.getClass().getName() + ": " + this.getMessage() + System.lineSeparator());
        StackTraceElement[] traceElements = this.getStackTrace();
        if (depth < 0 || depth > traceElements.length - 1)
            depth = traceElements.length - 1;
        for (int i = 0; i <= depth; i++) {
            ret.append("\t").append(traceElements[i]).append(System.lineSeparator());
        }
        return ret.append("\t...").toString();
    }

    public String log() {
        return this.toString() + System.lineSeparator() + getShortTrace(3);
    }
}
