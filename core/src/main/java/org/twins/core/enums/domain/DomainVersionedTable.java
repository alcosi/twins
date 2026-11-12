package org.twins.core.enums.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of the domain-configuration tables versioned by TWINS-719. API-facing alias for the
 * physical {@code domain_version_audit.table_name} values — clients speak enum constants, never
 * real DB table names. Extend together with the versioned-table registry (waves add new tables
 * and their audit rows appear under the physical name).
 */
@Getter
@RequiredArgsConstructor
public enum DomainVersionedTable {
    TWIN_CLASS("twin_class"),
    TWIN_CLASS_FIELD("twin_class_field"),
    TWIN_STATUS("twin_status"),
    DATA_LIST("data_list"),
    DATA_LIST_OPTION("data_list_option"),
    DATA_LIST_SUBSET("data_list_subset"),
    DATA_LIST_SUBSET_OPTION("data_list_subset_option");

    private final String tableName;

    private static final Map<String, DomainVersionedTable> BY_TABLE_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(DomainVersionedTable::getTableName, Function.identity()));

    /**
     * Reverse lookup of an audit row's physical table name. Throws on an unknown name — every
     * versioned table must be registered here (see class javadoc).
     */
    public static DomainVersionedTable fromTableName(String tableName) {
        DomainVersionedTable result = BY_TABLE_NAME.get(tableName);
        if (result == null)
            throw new IllegalArgumentException("Unknown versioned table name: " + tableName);
        return result;
    }
}
