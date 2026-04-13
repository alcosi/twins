package org.twins.core.dao;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldDecimalIncrement;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdvancedEntityManager {
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    private final Map<Class<?>, EntityInsertDescriptor> cache = new ConcurrentHashMap<>();

    public <T> void insertOnConflictDoNothing(List<T> entities, List<String> conflictFields, int batchSize) {
        if (entities == null || entities.isEmpty()) return;
        if (conflictFields == null || conflictFields.isEmpty()) {
            throw new IllegalArgumentException("conflictFields must not be empty");
        }

        Class<?> clazz = entities.getFirst().getClass();
        EntityInsertDescriptor d = getDescriptor(clazz);

        List<String> conflictColumns = conflictFields.stream()
                .map(f -> Optional.ofNullable(d.fieldToColumn.get(f))
                        .orElseThrow(() -> new IllegalArgumentException("Unknown field: " + f)))
                .toList();

        String sql = buildSql(d.tableName, d.columns, conflictColumns);

        // Flush any pending JPA/Hibernate changes before raw JDBC insert
        entityManager.flush();

        jdbcTemplate.batchUpdate(sql, entities, batchSize, (PreparedStatement ps, T entity) -> {
            for (int i = 0; i < d.extractors.size(); i++) {
                Object value = d.extractors.get(i).apply(entity);
                ps.setObject(i + 1, value);
            }
        });
    }

    public <T> void insertOnConflictDoNothing(List<T> entities, List<String> conflictFields) {
        insertOnConflictDoNothing(entities, conflictFields, 1000);
    }

    /**
     * Batch increment operation: inserts new rows or increments existing values.
     * For each entity, the incrementColumns values are added to existing DB values.
     *
     * @param entities entities to insert/increment
     * @param conflictFields fields that define uniqueness (e.g., List.of("twinId", "twinClassFieldId"))
     * @param incrementColumns columns to increment (e.g., List.of("value")) - values are treated as deltas
     * @param batchSize batch size for JDBC operations
     * @param <T> entity type
     */
    public <T> void insertOnConflictIncrement(List<T> entities, List<String> conflictFields, List<String> incrementColumns, int batchSize) {
        if (entities == null || entities.isEmpty()) return;
        if (conflictFields == null || conflictFields.isEmpty()) {
            throw new IllegalArgumentException("conflictFields must not be empty");
        }
        if (incrementColumns == null || incrementColumns.isEmpty()) {
            throw new IllegalArgumentException("incrementColumns must not be empty");
        }

        Class<?> clazz = entities.getFirst().getClass();
        EntityInsertDescriptor d = getDescriptor(clazz);

        List<String> conflictColumns = conflictFields.stream()
                .map(f -> Optional.ofNullable(d.fieldToColumn.get(f))
                        .orElseThrow(() -> new IllegalArgumentException("Unknown field: " + f)))
                .toList();

        List<String> incrementColumnDefs = incrementColumns.stream()
                .map(f -> Optional.ofNullable(d.fieldToColumn.get(f))
                        .orElseThrow(() -> new IllegalArgumentException("Unknown field: " + f)))
                .toList();

        String sql = buildIncrementSql(d.tableName, d.columns, conflictColumns, incrementColumnDefs);

        entityManager.flush();

        jdbcTemplate.batchUpdate(sql, entities, batchSize, (PreparedStatement ps, T entity) -> {
            for (int i = 0; i < d.extractors.size(); i++) {
                Object value = d.extractors.get(i).apply(entity);
                ps.setObject(i + 1, value);
            }
        });
    }

    public <T> void insertOnConflictIncrement(List<T> entities, List<String> conflictFields, List<String> incrementColumns) {
        insertOnConflictIncrement(entities, conflictFields, incrementColumns, 1000);
    }

    /**
     * Atomically increments decimal field values using native SQL.
     * Bypasses JPA and works directly with JDBC for maximum performance.
     *
     * @param increments list of TwinFieldDecimalIncrement objects
     */
    public void incrementDecimalFields(List<TwinFieldDecimalIncrement> increments) {
        if (increments == null || increments.isEmpty()) return;

        entityManager.flush();

        jdbcTemplate.batchUpdate(
                "INSERT INTO twin_field_decimal (id, twin_id, twin_class_field_id, value) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (twin_id, twin_class_field_id) " +
                "DO UPDATE SET value = COALESCE(twin_field_decimal.value, 0) + EXCLUDED.value",
                increments,
                1000,
                (PreparedStatement ps, TwinFieldDecimalIncrement inc) -> {
                    ps.setObject(1, inc.getId());
                    ps.setObject(2, inc.getTwinId());
                    ps.setObject(3, inc.getTwinClassFieldId());
                    ps.setObject(4, inc.getDelta());
                }
        );
    }

    private EntityInsertDescriptor getDescriptor(Class<?> clazz) {
        return cache.computeIfAbsent(clazz, this::buildDescriptor);
    }

    private EntityInsertDescriptor buildDescriptor(Class<?> clazz) {
        String tableName = resolveTableName(clazz);

        List<String> columns = new ArrayList<>();
        List<Function<Object, Object>> extractors = new ArrayList<>();
        Map<String, String> fieldToColumn = new HashMap<>();

        for (Field field : getAllFields(clazz)) {
            field.setAccessible(true);

            // skip @Transient
            if (field.isAnnotationPresent(Transient.class)) continue;

            // skip insertable=false
            Column columnAnn = field.getAnnotation(Column.class);
            if (columnAnn != null && !columnAnn.insertable()) continue;

            // skip collections
            if (Collection.class.isAssignableFrom(field.getType())) continue;

            // skip relations
            if (field.isAnnotationPresent(ManyToOne.class)
                    || field.isAnnotationPresent(OneToOne.class)) {
                continue;
            }

            String columnName = resolveColumnName(field);

            columns.add(columnName);
            fieldToColumn.put(field.getName(), columnName);

            extractors.add(entity -> {
                try {
                    Object value = field.get(entity);
                    if (value instanceof Enum<?>) {
                        return ((Enum<?>) value).name();
                    }
                    return value;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return new EntityInsertDescriptor(tableName, columns, extractors, fieldToColumn);
    }

    private String resolveTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException("Missing @Table on " + clazz.getName());
        }

        if (!table.schema().isEmpty()) {
            return table.schema() + "." + table.name();
        }
        return table.name();
    }

    private String resolveColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return normalizeColumn(column.name());
        }
        return field.getName();
    }

    private String normalizeColumn(String name) {
        // remove mysql quoting
        name = name.replace("`", "");
        if (name.startsWith("\"") && name.endsWith("\"")) {
            return name;
        }
        // quoting for PostgreSQL
        return "\"" + name + "\"";
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();

        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    private String buildSql(String tableName, List<String> columns, List<String> conflictColumns) {
        String columnList = String.join(", ", columns);
        String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
        String conflict = String.join(", ", conflictColumns);

        return "INSERT INTO " + tableName +
                " (" + columnList + ") " +
                "VALUES (" + placeholders + ") " +
                "ON CONFLICT (" + conflict + ") DO NOTHING";
    }

    private String buildIncrementSql(String tableName, List<String> columns, List<String> conflictColumns, List<String> incrementColumns) {
        String columnList = String.join(", ", columns);
        String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
        String conflict = String.join(", ", conflictColumns);
        String updateClause = incrementColumns.stream()
                .map(col -> col + " = COALESCE(" + tableName + "." + col + ", 0) + EXCLUDED." + col)
                .collect(Collectors.joining(", "));

        return "INSERT INTO " + tableName +
                " (" + columnList + ") " +
                "VALUES (" + placeholders + ") " +
                "ON CONFLICT (" + conflict + ") " +
                "DO UPDATE SET " + updateClause;
    }

    private record EntityInsertDescriptor(
            String tableName,
            List<String> columns,
            List<Function<Object, Object>> extractors,
            Map<String, String> fieldToColumn
    ) {}
}