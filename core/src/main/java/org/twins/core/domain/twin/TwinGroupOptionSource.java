package org.twins.core.domain.twin;

/**
 * Where a DATA_LIST_OPTION group key comes from. The two sources are rendered through different
 * request show-modes: flavor through showTwinFlavor2DataListOptionMode, a twin class field through
 * showTwinField2DataListOptionMode. Null for every other {@link org.twins.core.enums.twin.TwinGroupKeyType}.
 */
public enum TwinGroupOptionSource {
    FLAVOR,
    TWIN_FIELD
}
