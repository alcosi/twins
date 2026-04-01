package org.twins.core.service.bulk;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HibernateBulkInserter {

    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    public <T> void insertIgnore(List<T> entities, List<String> conflictFields, int batchSize) {
        if (entities == null || entities.isEmpty()) return;
        if (conflictFields == null || conflictFields.isEmpty()) {
            throw new IllegalArgumentException("conflictFields must not be empty");
        }

        // Flush any pending JPA/Hibernate changes before raw JDBC insert
        entityManager.flush();

        Class<?> clazz = entities.getFirst().getClass();
        AbstractEntityPersister p = getPersister(clazz);

        List<String> columns = resolveColumns(p);
        List<String> conflictColumns = resolveConflictColumns(p, conflictFields);

        String sql = buildSql(clazz, columns, conflictColumns);

        jdbcTemplate.batchUpdate(sql, entities, batchSize, (PreparedStatement ps, T entity) -> {
            try {
                bind(ps, p, entity);
            } catch (Exception e) {
                throw new RuntimeException("Failed to bind entity values", e);
            }
        });
    }

    public <T> void insertIgnore(List<T> entities, List<String> conflictFields) {
        insertIgnore(entities, conflictFields, 1000);
    }

    private AbstractEntityPersister getPersister(Class<?> clazz) {
        Session session = entityManager.unwrap(Session.class);
        SessionFactoryImplementor factory = (SessionFactoryImplementor) session.getSessionFactory();
        return (AbstractEntityPersister) factory.getMetamodel().entityPersister(clazz);
    }

    private List<String> resolveColumns(AbstractEntityPersister p) {
        List<String> columns = new ArrayList<>();
        Set<String> addedColumns = new HashSet<>();

        for (String idCol : p.getIdentifierColumnNames()) {
            if (addedColumns.add(idCol)) {
                columns.add(idCol);
            }
        }

        String[] props = p.getPropertyNames();
        boolean[] insertable = p.getPropertyInsertability();

        for (int i = 0; i < props.length; i++) {
            if (!insertable[i]) continue;

            String[] colNames = p.getPropertyColumnNames(props[i]);
            if (colNames != null) {
                for (String col : colNames) {
                    if (addedColumns.add(col)) {
                        columns.add(col);
                    }
                }
            }
        }

        return columns;
    }

    private List<String> resolveConflictColumns(AbstractEntityPersister p, List<String> conflictFields) {
        return conflictFields.stream()
                .map(field -> {
                    String[] cols = p.getPropertyColumnNames(field);
                    if (cols == null || cols.length == 0) {
                        throw new IllegalArgumentException("Unknown field: " + field);
                    }
                    if (cols.length > 1) {
                        throw new IllegalArgumentException("Field maps to multiple columns: " + field);
                    }
                    return cols[0];
                })
                .collect(Collectors.toList());
    }

    private String buildSql(Class<?> clazz, List<String> columns, List<String> conflictColumns) {
        String tableName = getTableName(clazz);
        String columnList = String.join(", ", columns);
        String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
        String conflict = String.join(", ", conflictColumns);

        return "INSERT INTO " + tableName +
                " (" + columnList + ") " +
                "VALUES (" + placeholders + ") " +
                "ON CONFLICT (" + conflict + ") DO NOTHING";
    }

    private String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException("Entity " + clazz.getName() + " is not annotated with @Table");
        }
        String tableName = table.name();
        String schema = table.schema();
        if (schema != null && !schema.isEmpty()) {
            return schema + "." + tableName;
        }
        return tableName;
    }

    private void bind(PreparedStatement ps, AbstractEntityPersister p, Object entity) throws Exception {
        // Build column -> value mapping first, then bind in order
        Map<String, Object> columnValues = new LinkedHashMap<>();

        // ID - use reflection to read directly from entity (getPropertyValue doesn't work for transient entities)
        Object id;
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            id = idField.get(entity);
        } catch (Exception e) {
            // Fallback to Hibernate methods
            try {
                String idProp = p.getIdentifierPropertyName();
                id = p.getPropertyValue(entity, idProp);
            } catch (Exception e2) {
                id = p.getIdentifier(entity, (SharedSessionContractImplementor) null);
            }
        }
        for (String idCol : p.getIdentifierColumnNames()) {
            columnValues.put(idCol, id);
        }

        // Properties
        String[] props = p.getPropertyNames();
        boolean[] insertable = p.getPropertyInsertability();
        String idProp = p.getIdentifierPropertyName();

        for (int i = 0; i < props.length; i++) {
            if (!insertable[i]) continue;
            if (props[i].equals(idProp)) continue;

            // Skip collection properties (@OneToMany, @ManyToMany) - they don't have columns in this table
            try {
                Field field = entity.getClass().getDeclaredField(props[i]);
                if (Collection.class.isAssignableFrom(field.getType())) continue;
            } catch (NoSuchFieldException e) {
                // Field might be in superclass, check via getter method
                try {
                    String getterName = "get" + props[i].substring(0, 1).toUpperCase() + props[i].substring(1);
                    Method method = entity.getClass().getMethod(getterName);
                    if (Collection.class.isAssignableFrom(method.getReturnType())) continue;
                } catch (NoSuchMethodException ignored) {}
            }

            Object value = p.getPropertyValue(entity, props[i]);
            String[] cols = p.getPropertyColumnNames(props[i]);
            if (cols == null) continue;

            for (String col : cols) {
                columnValues.put(col, value);
            }
        }

        // Bind in column order
        int index = 1;
        for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
            Object value = entry.getValue();
            Object bindValue = (value instanceof Enum) ? ((Enum<?>) value).name() : value;
            ps.setObject(index++, bindValue);
        }
    }
}
