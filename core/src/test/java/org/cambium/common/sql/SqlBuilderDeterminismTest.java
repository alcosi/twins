package org.cambium.common.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that {@link SqlBuilder} produces deterministic SQL: rows sorted by id, hstore/jsonb keys
 * sorted lexicographically, and identical output regardless of input collection order. No Spring
 * context needed — {@code SqlBuilder} is a plain {@code @Component} usable via {@code new}.
 */
class SqlBuilderDeterminismTest {

    private final SqlBuilder sqlBuilder = new SqlBuilder();

    @Entity
    @Table(name = "test_entity")
    static class TestEntity {
        @Column(name = "id")
        UUID id;
        @Column(name = "name")
        String name;
        @Column(name = "params")
        HashMap<String, String> params;   // -> hstore branch (HashMap)
        @Column(name = "tags")
        TreeMap<String, String> tags;     // -> jsonb branch (Map, not a HashMap subtype)

        TestEntity id(UUID id) { this.id = id; return this; }
        TestEntity name(String name) { this.name = name; return this; }
    }

    @Test
    void buildInserts_withComparator_sortsById() {
        UUID first  = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID second = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID third  = UUID.fromString("00000000-0000-0000-0000-000000000003");

        // deliberately fed out of order
        String out = sqlBuilder.buildInserts(
                List.of(e(third, "c"), e(first, "a"), e(second, "b")),
                Comparator.comparing(e -> e.id));

        int i1 = out.indexOf("'" + first + "'");
        int i2 = out.indexOf("'" + second + "'");
        int i3 = out.indexOf("'" + third + "'");
        assertTrue(i1 >= 0 && i2 >= 0 && i3 >= 0, "all ids must be present; got:\n" + out);
        assertTrue(i1 < i2 && i2 < i3, "INSERTs must appear in id order; got:\n" + out);
    }

    @Test
    void buildInsert_hstoreKeysAreSorted() {
        TestEntity e = e(UUID.randomUUID(), "x");
        e.params = new HashMap<>();
        e.params.put("zeta", "1");
        e.params.put("alpha", "2");
        e.params.put("middle", "3");

        String out = sqlBuilder.buildInsert(e);
        int ia = out.indexOf("alpha");
        int im = out.indexOf("middle");
        int iz = out.indexOf("zeta");
        assertTrue(ia >= 0 && im >= 0 && iz >= 0, "hstore keys must be present; got:\n" + out);
        assertTrue(ia < im && im < iz, "hstore keys must be sorted; got:\n" + out);
    }

    @Test
    void buildInsert_jsonbKeysAreSorted() {
        TestEntity e = e(UUID.randomUUID(), "x");
        e.tags = new TreeMap<>();
        e.tags.put("zeta", "1");
        e.tags.put("alpha", "2");

        String out = sqlBuilder.buildInsert(e);
        int ia = out.indexOf("\"alpha\"");
        int iz = out.indexOf("\"zeta\"");
        assertTrue(ia >= 0 && iz >= 0, "jsonb keys must be present; got:\n" + out);
        assertTrue(ia < iz, "jsonb keys must be sorted; got:\n" + out);
    }

    @Test
    void buildInserts_isStableAcrossShuffles() {
        List<TestEntity> base = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            base.add(e(UUID.fromString(String.format("00000000-0000-0000-0000-%012d", i)), "n" + i));
        }
        String reference = sqlBuilder.buildInserts(base, Comparator.comparing(e -> e.id));
        assertNotNull(reference);

        // Same logical content, different input order — output must be byte-for-byte identical.
        Random rng = new Random(42);
        for (int run = 0; run < 50; run++) {
            List<TestEntity> shuffled = new ArrayList<>(base);
            Collections.shuffle(shuffled, rng);
            String out = sqlBuilder.buildInserts(shuffled, Comparator.comparing(e -> e.id));
            assertEquals(reference, out, "run " + run + " diverged from reference");
        }
    }

    private TestEntity e(UUID id, String name) {
        return new TestEntity().id(id).name(name);
    }
}
