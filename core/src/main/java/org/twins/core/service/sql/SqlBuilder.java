package org.twins.core.service.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SqlBuilder {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String buildInsert(Object entity) {
        Class<?> clazz = getRealClass(entity.getClass());
        EntityMetadata metadata = extractMetadata(clazz);

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(metadata.tableName).append(" (");

        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (int i = 0; i < metadata.columns.size(); i++) {
            Function<Object, Object> extractor = metadata.extractors.get(i);
            Object value = extractor.apply(entity);

            if (value != null) {
                columns.add(metadata.columns.get(i));
                values.add(formatValue(value, metadata.fieldTypes.get(i)));
            }
        }

        if (columns.isEmpty()) {
            return "";
        }

        sql.append(String.join(", ", columns)).append(") VALUES (");
        sql.append(String.join(", ", values));
        sql.append(") ON CONFLICT DO NOTHING;");

        return sql.toString();
    }

    public String buildInserts(Collection<?> entities) {
        return entities.stream()
                .map(this::buildInsert)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    private String formatValue(Object value, Class<?> fieldType) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof UUID) {
            return "'" + value + "'";
        }

        if (value instanceof HashMap) {
            @SuppressWarnings("unchecked")
            HashMap<String, String> hstoreMap = (HashMap<String, String>) value;
            if (hstoreMap.isEmpty()) {
                return "''::hstore";
            }
            StringBuilder hstore = new StringBuilder();
            for (Map.Entry<String, String> entry : hstoreMap.entrySet()) {
                if (hstore.length() > 0) {
                    hstore.append(", ");
                }
                hstore.append(formatHstoreValue(entry.getKey()))
                     .append("=>")
                     .append(formatHstoreValue(entry.getValue()));
            }
            return "'" + hstore + "'::hstore";
        }

        if (value instanceof String) {
            return "'" + escapeSqlString((String) value) + "'";
        }

        if (value instanceof Timestamp) {
            return "'" + value + "'";
        }

        if (value instanceof Enum<?>) {
            return "'" + ((Enum<?>) value).name() + "'";
        }

        if (value instanceof Boolean) {
            return ((Boolean) value) ? "TRUE" : "FALSE";
        }

        if (value instanceof Number) {
            return value.toString();
        }

        if (value instanceof Map) {
            try {
                String json = objectMapper.writeValueAsString(value);
                return "'" + escapeSqlString(json) + "'::jsonb";
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize Map to JSON", e);
            }
        }

        return "'" + escapeSqlString(value.toString()) + "'";
    }

    private String escapeSqlString(String value) {
        return value.replace("'", "''");
    }

    private String formatHstoreValue(String value) {
        // PostgreSQL hstore quoting rules:
        // 1. Empty strings must be quoted: ""
        // 2. Strings with spaces, commas, =>, or double quotes must be quoted
        // 3. Double quotes inside are escaped by doubling: ""
        if (value.isEmpty()) {
            return "\"\"";
        }

        // Check if value needs quoting
        boolean needsQuoting = value.contains(" ") || value.contains(",") ||
                value.contains("=>") || value.contains("\"") ||
                value.contains("\t") || value.contains("\n") ||
                value.startsWith("'") || value.startsWith("0") ||
                value.matches("\\d+"); // Numeric strings need quoting

        if (!needsQuoting) {
            // Unquoted value (NULL keyword becomes unquoted NULL)
            return "NULL".equals(value) ? "NULL" : value;
        }

        // Quote and escape double quotes by doubling
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private EntityMetadata extractMetadata(Class<?> clazz) {
        String tableName = resolveTableName(clazz);

        List<String> columns = new ArrayList<>();
        List<Function<Object, Object>> extractors = new ArrayList<>();
        List<Class<?>> fieldTypes = new ArrayList<>();

        for (Field field : getAllFields(clazz)) {
            field.setAccessible(true);

            Column columnAnn = field.getAnnotation(Column.class);
            if (columnAnn != null && !columnAnn.insertable()) {
                continue;
            }

            if (Collection.class.isAssignableFrom(field.getType())) {
                continue;
            }

            if (field.isAnnotationPresent(jakarta.persistence.ManyToOne.class)
                    || field.isAnnotationPresent(jakarta.persistence.OneToOne.class)) {
                continue;
            }

            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            if (field.isAnnotationPresent(jakarta.persistence.Transient.class)) {
                continue;
            }

            String columnName = resolveColumnName(field);
            columns.add(columnName);
            fieldTypes.add(field.getType());

            // Use getter method instead of direct field access for proper lazy loading
            final String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            java.lang.reflect.Method getter;
            try {
                getter = clazz.getMethod(getterName);
            } catch (NoSuchMethodException e) {
                // Try "is" prefix for boolean fields
                try {
                    getter = clazz.getMethod("is" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1));
                } catch (NoSuchMethodException ex) {
                    // Fall back to field access if no getter found
                    getter = null;
                }
            }

            final java.lang.reflect.Method finalGetter = getter;
            extractors.add(entity -> {
                try {
                    Object fieldValue = finalGetter != null ? finalGetter.invoke(entity) : field.get(entity);
                    if (fieldValue instanceof Enum<?>) {
                        return ((Enum<?>) fieldValue).name();
                    }
                    return fieldValue;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return new EntityMetadata(tableName, columns, extractors, fieldTypes);
    }

    private Class<?> getRealClass(Class<?> clazz) {
        if (clazz.getName().contains("$HibernateProxy")) {
            return clazz.getSuperclass();
        }
        return clazz;
    }

    private String resolveTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException("Missing @Table on " + clazz.getName());
        }
        if (!table.schema().isEmpty()) {
            return table.schema() + "." + table.name();
        }
        return "\"" + table.name() + "\"";
    }

    private String resolveColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return "\"" + column.name().replace("`", "") + "\"";
        }
        return "\"" + field.getName() + "\"";
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private record EntityMetadata(
            String tableName,
            List<String> columns,
            List<Function<Object, Object>> extractors,
            List<Class<?>> fieldTypes
    ) {}
}
