package org.twins.core.enums.user;

public enum UserStatus {
    ACTIVE,
    DELETED,
    EMAIL_VERIFICATION_REQUIRED,
    BLOCKED;

    public static final String _ACTIVE =  "ACTIVE";
    public static final String _DELETED =  "DELETED";
    public static final String _BLOCKED =  "BLOCKED";
    public static final String _EMAIL_VERIFICATION_REQUIRED =  "EMAIL_VERIFICATION_REQUIRED";
}
