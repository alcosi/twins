package org.cambium.common.exception;

import java.util.Hashtable;

public interface IContextException {
    public Hashtable<String, String> getContext();
    public <T extends IContextException> T addContext(String key, String value);
}
