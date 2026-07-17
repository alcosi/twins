package org.cambium.service;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the static {@code loadKit} helpers on {@link EntitySecureFindServiceImpl}.
 * <p>
 * The headline regression ({@link LoadKitGrouped#testCrossGroupDuplicateKeyNoLongerThrows}) reproduces
 * the production crash from the i18n export path: a flat query result spanning multiple source groups,
 * where the Kit-key (e.g. locale "en") legitimately repeats across groups. The old KitGrouped-based
 * implementation indexed the whole flat collection by that key and threw IllegalStateException; the
 * current manual grouping must distribute results per-group instead.
 */
public class EntitySecureFindServiceImplLoadKitTest {

    private UUID id() {
        return UUID.randomUUID();
    }

    // ===== overload 1: loadKit(..., queryResultGetId, queryResultGetGroupId) =====

    @Nested
    class LoadKitGrouped {

        @Test
        public void testCrossGroupDuplicateKeyNoLongerThrows() {
            // regression: two source groups each having a result with the same Kit-key ("en")
            UUID i18n1 = id();
            UUID i18n2 = id();
            var src1 = new Src(i18n1);
            var src2 = new Src(i18n2);

            var t1 = new Result(i18n1, "en", "to storage");
            var t2 = new Result(i18n2, "en", "to project");
            Function<Set<UUID>, Collection<Result>> query = ids -> new ArrayList<>(List.of(t1, t2));

            assertDoesNotThrow(() ->
                    EntitySecureFindServiceImpl.loadKit(
                            List.of(src1, src2),
                            Src::getId,
                            Src::getItems,
                            Src::setItems,
                            query,
                            Result::getKey,       // Kit-key (locale)
                            Result::getGroupId)); // grouping (i18nId)

            // each source received only its own translation, no cross-group bleed
            assertEquals("to storage", src1.getItems().get("en").getValue());
            assertEquals("to project", src2.getItems().get("en").getValue());
            assertNull(src1.getItems().get("ru"));
        }

        @Test
        public void testMultipleKeysWithinSingleGroup() {
            UUID i18n1 = id();
            var src1 = new Src(i18n1);
            Function<Set<UUID>, Collection<Result>> query = ids -> new ArrayList<>(List.of(
                    new Result(i18n1, "en", "hello"),
                    new Result(i18n1, "ru", "privet")));
            EntitySecureFindServiceImpl.loadKit(List.of(src1),
                    Src::getId, Src::getItems, Src::setItems,
                    query, Result::getKey, Result::getGroupId);

            assertEquals(2, src1.getItems().size());
            assertEquals("hello", src1.getItems().get("en").getValue());
            assertEquals("privet", src1.getItems().get("ru").getValue());
        }

        @Test
        public void testGroupWithoutDataGetsEmptyKit() {
            UUID i18n1 = id();
            UUID i18n2 = id();
            var src1 = new Src(i18n1);
            var src2 = new Src(i18n2);
            Function<Set<UUID>, Collection<Result>> query = ids ->
                    new ArrayList<>(List.of(new Result(i18n1, "en", "only-for-1")));
            EntitySecureFindServiceImpl.loadKit(List.of(src1, src2),
                    Src::getId, Src::getItems, Src::setItems,
                    query, Result::getKey, Result::getGroupId);

            assertEquals("only-for-1", src1.getItems().get("en").getValue());
            assertNotNull(src2.getItems());
            assertTrue(src2.getItems().isEmpty());
        }

        @Test
        public void testSourceWithPreloadedKitIsSkipped() {
            UUID i18n1 = id();
            UUID i18n2 = id();
            var src1 = new Src(i18n1);
            var src2 = new Src(i18n2);
            Kit<Result, String> preloaded = new Kit<>(Result::getKey);
            preloaded.add(new Result(i18n2, "en", "preloaded"));
            src2.setItems(preloaded);

            boolean[] queryCalled = {false};
            Function<Set<UUID>, Collection<Result>> query = ids -> {
                queryCalled[0] = true;
                assertFalse(ids.contains(i18n2), "src with a non-null kit must not be reloaded");
                return new ArrayList<>(List.of(new Result(i18n1, "en", "loaded")));
            };

            EntitySecureFindServiceImpl.loadKit(List.of(src1, src2),
                    Src::getId, Src::getItems, Src::setItems,
                    query, Result::getKey, Result::getGroupId);

            assertTrue(queryCalled[0]);
            assertEquals("loaded", src1.getItems().get("en").getValue());
            assertSame(preloaded, src2.getItems(), "preloaded kit must be left untouched");
        }

        @Test
        public void testEmptySourceCollectionDoesNothing() {
            boolean[] queryCalled = {false};
            Function<Set<UUID>, Collection<Result>> query = ids -> {
                queryCalled[0] = true;
                return List.of();
            };
            assertDoesNotThrow(() ->
                    EntitySecureFindServiceImpl.loadKit(List.of(),
                            Src::getId, Src::getItems, Src::setItems,
                            query, Result::getKey, Result::getGroupId));
            assertFalse(queryCalled[0]);
        }

        @Test
        public void testEmptyQueryResultYieldsEmptyKit() {
            UUID i18n1 = id();
            var src1 = new Src(i18n1);
            Function<Set<UUID>, Collection<Result>> query = ids -> new ArrayList<>();
            EntitySecureFindServiceImpl.loadKit(List.of(src1),
                    Src::getId, Src::getItems, Src::setItems,
                    query, Result::getKey, Result::getGroupId);
            assertNotNull(src1.getItems());
            assertTrue(src1.getItems().isEmpty());
        }

        @Test
        public void testResultWithNullGroupIdIsIgnored() {
            UUID i18n1 = id();
            var src1 = new Src(i18n1);
            Function<Set<UUID>, Collection<Result>> query = ids -> new ArrayList<>(List.of(
                    new Result(i18n1, "en", "ok"),
                    new Result(null, "en", "orphan")));
            EntitySecureFindServiceImpl.loadKit(List.of(src1),
                    Src::getId, Src::getItems, Src::setItems,
                    query, Result::getKey, Result::getGroupId);
            assertEquals(1, src1.getItems().size());
            assertEquals("ok", src1.getItems().get("en").getValue());
        }

        @Test
        public void testDuplicateKeyWithinSingleGroupStillThrows() {
            // contract preserved: a genuine duplicate (same group + same Kit-key) is still a data error
            UUID i18n1 = id();
            var src1 = new Src(i18n1);
            Function<Set<UUID>, Collection<Result>> query = ids -> new ArrayList<>(List.of(
                    new Result(i18n1, "en", "first"),
                    new Result(i18n1, "en", "second")));
            assertThrows(IllegalStateException.class, () ->
                    EntitySecureFindServiceImpl.loadKit(List.of(src1),
                            Src::getId, Src::getItems, Src::setItems,
                            query, Result::getKey, Result::getGroupId));
        }
    }

    // ===== overload 2: loadKit(..., transformFunction, resultGetId, queryResultGetId, queryResultGetGroupId) =====

    @Nested
    class LoadKitGroupedTransform {

        @Test
        public void testTransformAppliedAndCrossGroupDuplicateKeyNoLongerThrows() {
            UUID i18n1 = id();
            UUID i18n2 = id();
            var src1 = new Src(i18n1);
            var src2 = new Src(i18n2);
            Function<Set<UUID>, Collection<Result>> query = ids -> new ArrayList<>(List.of(
                    new Result(i18n1, "en", "to storage"),
                    new Result(i18n2, "en", "to project")));
            Function<Result, Result> transform = r ->
                    new Result(r.getGroupId(), r.getKey(), r.getValue() + "_T");

            assertDoesNotThrow(() ->
                    EntitySecureFindServiceImpl.loadKit(
                            List.of(src1, src2),
                            Src::getId, Src::getItems, Src::setItems,
                            query, transform,
                            Result::getKey,      // resultGetId (on TL)
                            Result::getKey,      // queryResultGetId (on Q)
                            Result::getGroupId));// queryResultGetGroupId

            assertEquals("to storage_T", src1.getItems().get("en").getValue());
            assertEquals("to project_T", src2.getItems().get("en").getValue());
        }

        @Test
        public void testGroupWithoutDataGetsEmptyKit() {
            UUID i18n1 = id();
            UUID i18n2 = id();
            var src1 = new Src(i18n1);
            var src2 = new Src(i18n2);
            Function<Set<UUID>, Collection<Result>> query = ids ->
                    new ArrayList<>(List.of(new Result(i18n1, "en", "only-for-1")));
            Function<Result, Result> identity = r -> r;
            EntitySecureFindServiceImpl.loadKit(List.of(src1, src2),
                    Src::getId, Src::getItems, Src::setItems,
                    query, identity,
                    Result::getKey, Result::getKey, Result::getGroupId);
            assertEquals("only-for-1", src1.getItems().get("en").getValue());
            assertTrue(src2.getItems().isEmpty());
        }
    }

    // ===== Helper stubs =====

    static class Src {
        private final UUID id;
        private Kit<Result, String> items;

        Src(UUID id) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }

        public Kit<Result, String> getItems() {
            return items;
        }

        public void setItems(Kit<Result, String> items) {
            this.items = items;
        }
    }

    static class Result {
        private final UUID groupId;
        private final String key;
        private final String value;

        Result(UUID groupId, String key, String value) {
            this.groupId = groupId;
            this.key = key;
            this.value = value;
        }

        public UUID getGroupId() {
            return groupId;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Result that)) return false;
            return Objects.equals(groupId, that.groupId)
                    && Objects.equals(key, that.key)
                    && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, key, value);
        }
    }
}
