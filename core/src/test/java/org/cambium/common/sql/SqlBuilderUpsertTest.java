package org.cambium.common.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the upsert / delete helpers added to {@link SqlBuilder}:
 * <ul>
 *   <li>{@code buildUpsert} primary-key detection — single {@code @Id}, composite ({@code @IdClass}-style
 *       multiple {@code @Id}), fallback to an {@code "id"} column, and the failure mode when no PK is
 *       detectable.</li>
 *   <li>{@code buildUpserts} deterministic id ordering.</li>
 *   <li>{@code buildDeleteByColumn} IN-clause rendering and the empty-ids short-circuit.</li>
 * </ul>
 * No Spring context needed — {@link SqlBuilder} is a plain {@code @Component} usable via {@code new}.
 */
class SqlBuilderUpsertTest {

    private static final UUID ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ID_3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private final SqlBuilder sqlBuilder = new SqlBuilder();

    @Entity
    @Table(name = "single_pk")
    static class SinglePkEntity {
        @Id
        UUID id;
        @Column(name = "name")
        String name;
        @Column(name = "cnt")
        Integer cnt;
    }

    @Entity
    @Table(name = "composite_pk")
    static class CompositePkEntity {
        @Id
        @Column(name = "a")
        String a;
        @Id
        @Column(name = "b")
        String b;
        @Column(name = "data")
        String data;
    }

    @Entity
    @Table(name = "fallback_id")
    static class FallbackIdEntity {
        // no @Id annotation — buildUpsert must fall back to the "id" column by name
        @Column(name = "id")
        UUID id;
        @Column(name = "name")
        String name;
    }

    @Entity
    @Table(name = "no_pk")
    static class NoPkEntity {
        // neither @Id nor an "id" column — buildUpsert must refuse this
        @Column(name = "name")
        String name;
    }

    @Entity
    @Table(name = "only_id")
    static class OnlyIdEntity {
        @Id
        UUID id;
    }

    @Entity
    @Table(name = "deletable")
    static class DeletableEntity {
    }

    @Test
    void buildUpsert_singlePk_emitsOnConflictDoUpdateWithoutIdInSet() {
        SinglePkEntity e = new SinglePkEntity();
        e.id = ID_1;
        e.name = "n";
        e.cnt = 5;

        String sql = sqlBuilder.buildUpsert(e);

        assertTrue(sql.startsWith("INSERT INTO \"single_pk\" ("), "wrong INSERT header; got:\n" + sql);
        assertTrue(sql.contains(") ON CONFLICT (\"id\") DO UPDATE SET "), "missing single-column conflict target; got:\n" + sql);
        assertTrue(sql.contains("\"name\"=EXCLUDED.\"name\""), "non-pk column must be refreshed; got:\n" + sql);
        assertTrue(sql.contains("\"cnt\"=EXCLUDED.\"cnt\""), "non-pk column must be refreshed; got:\n" + sql);
        assertFalse(sql.contains("\"id\"=EXCLUDED.\"id\""), "PK column must NOT appear in SET clause; got:\n" + sql);
        assertTrue(sql.endsWith(";"), "statement must be terminated; got:\n" + sql);
    }

    @Test
    void buildUpsert_compositePk_usesAllIdColumnsAsConflictTarget() {
        CompositePkEntity e = new CompositePkEntity();
        e.a = "a1";
        e.b = "b1";
        e.data = "d";

        String sql = sqlBuilder.buildUpsert(e);

        assertTrue(sql.contains(") ON CONFLICT (\"a\", \"b\") DO UPDATE SET "), "missing composite conflict target; got:\n" + sql);
        assertTrue(sql.contains("\"data\"=EXCLUDED.\"data\""), "non-pk column must be refreshed; got:\n" + sql);
        assertFalse(sql.contains("\"a\"=EXCLUDED.\"a\""), "PK column a must NOT appear in SET clause; got:\n" + sql);
        assertFalse(sql.contains("\"b\"=EXCLUDED.\"b\""), "PK column b must NOT appear in SET clause; got:\n" + sql);
    }

    @Test
    void buildUpsert_fallsBackToIdColumnWhenNoIdAnnotation() {
        FallbackIdEntity e = new FallbackIdEntity();
        e.id = ID_1;
        e.name = "n";

        String sql = sqlBuilder.buildUpsert(e);

        assertTrue(sql.contains(") ON CONFLICT (\"id\") DO UPDATE SET "), "fallback id-column conflict target missing; got:\n" + sql);
        assertTrue(sql.contains("\"name\"=EXCLUDED.\"name\""), "non-pk column must be refreshed; got:\n" + sql);
    }

    @Test
    void buildUpsert_throwsWhenNoPkDetected() {
        NoPkEntity e = new NoPkEntity();
        e.name = "n";

        assertThrows(IllegalStateException.class, () -> sqlBuilder.buildUpsert(e),
                "buildUpsert must reject entities with no detectable PK");
    }

    @Test
    void buildUpsert_onlyIdColumnPresent_fallsBackToOnConflictDoNothing() {
        // When only the PK column has a value there is nothing to UPDATE on conflict, so the statement
        // must keep the safe DO NOTHING tail instead of an empty SET clause.
        OnlyIdEntity e = new OnlyIdEntity();
        e.id = ID_1;

        String sql = sqlBuilder.buildUpsert(e);

        assertTrue(sql.contains(") ON CONFLICT DO NOTHING;"), "bare-id row must use DO NOTHING; got:\n" + sql);
        assertFalse(sql.contains("DO UPDATE SET"), "no empty UPDATE expected; got:\n" + sql);
    }

    @Test
    void buildUpserts_sortedById() {
        SinglePkEntity e1 = single(ID_1, "a");
        SinglePkEntity e2 = single(ID_2, "b");
        SinglePkEntity e3 = single(ID_3, "c");

        // deliberately fed out of order
        String sql = sqlBuilder.buildUpserts(List.of(e3, e1, e2), Comparator.comparing(e -> e.id));

        int i1 = sql.indexOf("'" + ID_1 + "'");
        int i2 = sql.indexOf("'" + ID_2 + "'");
        int i3 = sql.indexOf("'" + ID_3 + "'");
        assertTrue(i1 >= 0 && i2 >= 0 && i3 >= 0, "all ids must be present; got:\n" + sql);
        assertTrue(i1 < i2 && i2 < i3, "upserts must appear in id order; got:\n" + sql);
        // every emitted statement is an upsert, never a DO NOTHING
        assertFalse(sql.contains("ON CONFLICT DO NOTHING"), "sorted multi-row export must be pure upsert; got:\n" + sql);
        assertTrue(sql.contains("DO UPDATE SET"), "expected upsert SET clauses; got:\n" + sql);
    }

    @Test
    void buildDeleteByColumn_emitsInClause() {
        // List (not Set) so the IN-clause order is deterministic and assertable verbatim.
        String sql = sqlBuilder.buildDeleteByColumn(DeletableEntity.class, "twin_factory_id", List.of(ID_1, ID_2));

        assertEquals("DELETE FROM \"deletable\" WHERE \"twin_factory_id\" IN ('" + ID_1 + "', '" + ID_2 + "');", sql);
    }

    @Test
    void buildDeleteByColumn_emptyIds_returnsEmpty() {
        assertEquals("", sqlBuilder.buildDeleteByColumn(DeletableEntity.class, "twin_factory_id", Set.of()));
        assertEquals("", sqlBuilder.buildDeleteByColumn(DeletableEntity.class, "twin_factory_id", null));
    }

    private SinglePkEntity single(UUID id, String name) {
        SinglePkEntity e = new SinglePkEntity();
        e.id = id;
        e.name = name;
        return e;
    }
}
