package org.twins.core.enums.twin;

/**
 * How sibling groups of one level are ordered. VALUE orders by what the user sees (the entity name
 * for user/twin keys, the localized i18n name for status/class/datalist-option keys, the scalar
 * itself for text/date/boolean keys); COUNT orders by group size.
 */
public enum TwinGroupSort {
    VALUE_ASC,
    VALUE_DESC,
    COUNT_ASC,
    COUNT_DESC
}
