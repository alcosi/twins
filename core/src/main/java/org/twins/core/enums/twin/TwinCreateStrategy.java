package org.twins.core.enums.twin;

public enum TwinCreateStrategy {
    STRICT, // Create only a fully valid Twin. If any required fields are missing, the request fails with a validation error.
    SKETCH, // Always create a SKETCH. Required fields are not validated during creation.
    AUTO // The system decides: if all required fields are present, a Twin is created; otherwise, a SKETCH is created.
}
