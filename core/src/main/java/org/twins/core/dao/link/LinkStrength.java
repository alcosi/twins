package org.twins.core.dao.link;

public enum LinkStrength {
    MANDATORY,
    OPTIONAL,
    OPTIONAL_BUT_DELETE_CASCADE;

    public static final String _MANDATORY = "MANDATORY";
    public static final String _OPTIONAL = "OPTIONAL";
    public static final String _OPTIONAL_BUT_DELETE_CASCADE = "OPTIONAL_BUT_DELETE_CASCADE";
}
