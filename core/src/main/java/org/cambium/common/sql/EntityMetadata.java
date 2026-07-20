package org.cambium.common.sql;

import java.util.List;
import java.util.function.Function;

public record EntityMetadata(
        String tableName,
        List<String> columns,
        List<String> idColumnNames,
        List<Function<Object, Object>> extractors,
        List<Class<?>> fieldTypes
) {}
