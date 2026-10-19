package org.twins.core.unit.dao.datascope;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.enums.datascope.DataScopeType;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Gate test for the {@link DataScope} classification campaign: every JPA entity under
 * {@code org.twins.core.dao} must declare a non-empty {@code @DataScope}, and the declared scopes
 * must stay consistent with the entity's physical scoping columns:
 * <ul>
 *   <li>{@code domain_id} column present ⇒ scopes contain {@link DataScopeType#DOMAIN} or
 *       {@link DataScopeType#DOMAIN_MEMBER} (never {@link DataScopeType#SYSTEM} alone);</li>
 *   <li>business-account ownership column ({@code business_account_id} / {@code *owner_business_account_id})
 *       present ⇒ scopes contain {@link DataScopeType#BUSINESS_ACCOUNT};</li>
 *   <li>{@link DataScopeType#SYSTEM} alone ⇒ no {@code domain_id} column.</li>
 * </ul>
 * Pure classpath reflection — no Spring context, no database.
 */
class DataScopeAnnotationCoverageTest extends BaseUnitTest {

    /**
     * Explicit exceptions to the column/scope consistency rules. Keep empty unless a table
     * genuinely cannot satisfy a rule (and document why).
     */
    private static final Set<String> ALLOWLIST = Set.of();

    /**
     * Tables an entity may reference via {@code <table>_id} without inheriting that table's scope —
     * value references (labels, owners), not scope-defining parents.
     */
    private static final Set<String> VALUE_FK_TABLES = Set.of("i18n", "user");

    private static final String DAO_PACKAGE = "org.twins.core.dao";

    @Test
    void everyEntityIsClassified() {
        List<Class<?>> unclassified = new ArrayList<>();
        for (Class<?> entity : scanEntities()) {
            DataScope dataScope = entity.getAnnotation(DataScope.class);
            if (dataScope == null || dataScope.value().length == 0) {
                unclassified.add(entity);
            }
        }
        assertTrue(unclassified.isEmpty(),
                () -> "Entities without @DataScope: " + names(unclassified));
    }

    @Test
    void domainIdColumnImpliesDomainScoped() {
        List<Class<?>> wrong = new ArrayList<>();
        for (Class<?> entity : scanEntities()) {
            if (isAllowlisted(entity)) {
                continue;
            }
            Set<DataScopeType> scopes = scopesOf(entity);
            if (scopes.isEmpty() || scopes.equals(EnumSet.of(DataScopeType.SYSTEM))) {
                continue; // SYSTEM-only entities are checked by the inverse rule below
            }
            if (hasColumn(entity, "domain_id")
                    && !scopes.contains(DataScopeType.DOMAIN)
                    && !scopes.contains(DataScopeType.DOMAIN_MEMBER)) {
                wrong.add(entity);
            }
        }
        assertTrue(wrong.isEmpty(),
                () -> "Entities with domain_id column but no DOMAIN/DOMAIN_MEMBER scope: " + names(wrong));
    }

    @Test
    void systemOnlyEntitiesHaveNoDomainIdColumn() {
        List<Class<?>> wrong = new ArrayList<>();
        for (Class<?> entity : scanEntities()) {
            if (isAllowlisted(entity)) {
                continue;
            }
            if (scopesOf(entity).equals(EnumSet.of(DataScopeType.SYSTEM)) && hasColumn(entity, "domain_id")) {
                wrong.add(entity);
            }
        }
        assertTrue(wrong.isEmpty(),
                () -> "Entities marked SYSTEM-only but having domain_id column: " + names(wrong));
    }

    @Test
    void businessAccountOwnershipColumnImpliesBusinessAccountScope() {
        List<Class<?>> wrong = new ArrayList<>();
        for (Class<?> entity : scanEntities()) {
            if (isAllowlisted(entity)) {
                continue;
            }
            boolean ownsBaRow = hasColumn(entity, "business_account_id")
                    || columnNames(entity).stream().anyMatch(n -> n.endsWith("owner_business_account_id"));
            if (ownsBaRow && !scopesOf(entity).contains(DataScopeType.BUSINESS_ACCOUNT)) {
                wrong.add(entity);
            }
        }
        assertTrue(wrong.isEmpty(),
                () -> "Entities with business-account ownership column but no BUSINESS_ACCOUNT scope: " + names(wrong));
    }

    @Test
    void systemOnlyEntitiesDoNotReferenceDomainScopedParents() {
        Set<Class<?>> entities = scanEntities();
        Set<String> domainScopedTables = new HashSet<>();
        for (Class<?> entity : entities) {
            Set<DataScopeType> scopes = scopesOf(entity);
            if (scopes.contains(DataScopeType.DOMAIN) || scopes.contains(DataScopeType.DOMAIN_MEMBER)) {
                jakarta.persistence.Table table = entity.getAnnotation(jakarta.persistence.Table.class);
                if (table != null && !table.name().isEmpty()) {
                    domainScopedTables.add(table.name());
                }
            }
        }
        List<Class<?>> wrong = new ArrayList<>();
        for (Class<?> entity : entities) {
            if (isAllowlisted(entity) || !scopesOf(entity).equals(EnumSet.of(DataScopeType.SYSTEM))) {
                continue;
            }
            for (String column : columnNames(entity)) {
                if (!column.endsWith("_id")) {
                    continue;
                }
                String targetTable = column.substring(0, column.length() - 3);
                if (VALUE_FK_TABLES.contains(targetTable)) {
                    continue; // label/owner value reference, not a scope parent
                }
                if (domainScopedTables.contains(targetTable)) {
                    wrong.add(entity);
                    break;
                }
            }
        }
        assertTrue(wrong.isEmpty(),
                () -> "Entities marked SYSTEM-only but referencing a domain-scoped parent table via FK"
                        + " (they should inherit the parent's scope):" + names(wrong));
    }

    @Test
    void domainScopeWithoutSystemMatchesNotNullableColumn() {
        List<Class<?>> wrong = new ArrayList<>();
        for (Class<?> entity : scanEntities()) {
            if (isAllowlisted(entity)) {
                continue;
            }
            Set<DataScopeType> scopes = scopesOf(entity);
            if (!scopes.contains(DataScopeType.DOMAIN) && !scopes.contains(DataScopeType.DOMAIN_MEMBER)) {
                continue;
            }
            Column domainId = columnAnnotation(entity, "domain_id");
            if (domainId == null) {
                continue; // scoped via parent, no own column
            }
            boolean systemToo = scopes.contains(DataScopeType.SYSTEM);
            // SYSTEM in scopes <=> table also holds global rows <=> column is nullable;
            // DOMAIN/DOMAIN_MEMBER without SYSTEM <=> every row is domain-owned <=> NOT NULL
            if (!systemToo && !domainId.nullable()) {
                continue; // consistent
            }
            if (systemToo && domainId.nullable()) {
                continue; // consistent
            }
            wrong.add(entity);
        }
        assertTrue(wrong.isEmpty(),
                () -> "Entities whose @DataScope (DOMAIN/DOMAIN_MEMBER with or without SYSTEM) contradicts the"
                        + " domain_id column nullability (no SYSTEM <=> nullable = false):" + names(wrong));
    }

    // === helpers ===

    private Set<Class<?>> scanEntities() {
        Set<Class<?>> entities = new TreeSet<>(Comparator.comparing(Class::getName));
        // walk the compiled classes of the dao package via the classloader
        String packageName = DAO_PACKAGE;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        var resources = new java.util.LinkedHashSet<java.net.URI>();
        try {
            var enumeration = cl.getResources(path);
            while (enumeration.hasMoreElements()) {
                resources.add(enumeration.nextElement().toURI());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot enumerate " + packageName, e);
        }
        for (var uri : resources) {
            var dir = new java.io.File(uri);
            if (!dir.isDirectory()) {
                continue; // jar-scanned packages are not expected in this module's tests
            }
            collectEntities(dir, packageName, cl, entities);
        }
        return entities;
    }

    private void collectEntities(java.io.File dir, String packageName, ClassLoader cl, Set<Class<?>> out) {
        java.io.File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (java.io.File f : files) {
            if (f.isDirectory()) {
                collectEntities(f, packageName + "." + f.getName(), cl, out);
            } else if (f.getName().endsWith(".class")) {
                String className = packageName + '.' + f.getName().substring(0, f.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className, false, cl);
                    if (clazz.isAnnotationPresent(Entity.class)) {
                        out.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Cannot load " + className, e);
                }
            }
        }
    }

    private Set<DataScopeType> scopesOf(Class<?> entity) {
        DataScope dataScope = entity.getAnnotation(DataScope.class);
        if (dataScope == null) {
            return EnumSet.noneOf(DataScopeType.class);
        }
        return EnumSet.copyOf(Arrays.asList(dataScope.value()));
    }

    private boolean hasColumn(Class<?> entity, String columnName) {
        return columnNames(entity).contains(columnName);
    }

    /**
     * Physical column names declared on the entity's fields, walking the class hierarchy
     * (parent classes may hold mapped columns, e.g. joined-inheritance bases).
     */
    private Set<String> columnNames(Class<?> entity) {
        Set<String> names = new HashSet<>();
        for (Class<?> c = entity; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (column != null && !column.name().isEmpty()) {
                    names.add(column.name());
                }
            }
        }
        return names;
    }

    /**
     * The {@code @Column} mapping of the given physical column name, or {@code null} if the entity
     * does not map it. Walks the class hierarchy like {@link #columnNames(Class)}.
     */
    private Column columnAnnotation(Class<?> entity, String columnName) {
        for (Class<?> c = entity; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (column != null && columnName.equals(column.name())) {
                    return column;
                }
            }
        }
        return null;
    }

    private boolean isAllowlisted(Class<?> entity) {
        return ALLOWLIST.contains(entity.getName());
    }

    private String names(List<Class<?>> classes) {
        var sb = new StringBuilder();
        for (Class<?> c : classes) {
            sb.append("\n  ").append(c.getName());
        }
        return sb.toString();
    }
}
