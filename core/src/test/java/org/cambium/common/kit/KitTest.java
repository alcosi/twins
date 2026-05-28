package org.cambium.common.kit;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class KitTest {

    private UUID id() {
        return UUID.randomUUID();
    }

    // ===== add + get =====

    @Nested
    class AddAndGet {
        @Test
        public void testAddAndLookup() {
            String val = "hello";
            Kit<String, Integer> kit = new Kit<>(String::length);
            kit.add(val);
            assertEquals(val, kit.get(val.length()));
        }

        @Test
        public void testAddAndGetByKey() {
            UUID uuid = id();
            var entity = new EntityStub(uuid, "test");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            kit.add(entity);
            assertEquals(entity, kit.get(uuid));
        }

        @Test
        public void testGetReturnsNullForMissingKey() {
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            assertNull(kit.get(id()));
        }

        @Test
        public void testGetSafeThrowsForMissingKey() {
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            assertThrows(ServiceException.class, () -> kit.getSafe(id()));
        }

        @Test
        public void testGetSafeReturnsExisting() throws ServiceException {
            UUID uuid = id();
            var entity = new EntityStub(uuid, "test");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            kit.add(entity);
            assertEquals(entity, kit.getSafe(uuid));
        }
    }

    // ===== DuplicateKeyMode.IGNORE (default) =====

    @Nested
    class DuplicateKeyIgnore {
        @Test
        public void testAddDuplicateKeyIgnoreKeepsFirst() {
            UUID uuid = id();
            var first = new EntityStub(uuid, "first");
            var second = new EntityStub(uuid, "second");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId, DuplicateKeyMode.IGNORE);
            kit.add(first);
            kit.add(second);
            assertEquals(first, kit.get(uuid));
            assertEquals(1, kit.size());
        }

        @Test
        public void testAddSameInstanceReturnsFalse() {
            UUID uuid = id();
            var entity = new EntityStub(uuid, "test");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId, DuplicateKeyMode.IGNORE);
            assertTrue(kit.add(entity));
            assertFalse(kit.add(entity));
            assertEquals(1, kit.size());
        }

        @Test
        public void testDuplicateIgnoreViaInitialCollection() {
            UUID uuid = id();
            var first = new EntityStub(uuid, "first");
            var second = new EntityStub(uuid, "second");
            Kit<EntityStub, UUID> kit = new Kit<>(Arrays.asList(first, second), EntityStub::getId, DuplicateKeyMode.IGNORE);
            assertEquals(first, kit.get(uuid));
            assertEquals(1, kit.size());
        }
    }

    // ===== DuplicateKeyMode.REPLACE =====

    @Nested
    class DuplicateKeyReplace {
        @Test
        public void testAddDuplicateKeyReplace() {
            UUID uuid = id();
            var first = new EntityStub(uuid, "first");
            var second = new EntityStub(uuid, "second");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId, DuplicateKeyMode.REPLACE);
            kit.add(first);
            kit.add(second);
            assertEquals(second, kit.get(uuid));
        }

        @Test
        public void testReplaceViaInitialCollection() {
            UUID uuid = id();
            var first = new EntityStub(uuid, "first");
            var second = new EntityStub(uuid, "second");
            Kit<EntityStub, UUID> kit = new Kit<>(Arrays.asList(first, second), EntityStub::getId, DuplicateKeyMode.REPLACE);
            assertEquals(second, kit.get(uuid));
        }
    }

    // ===== DuplicateKeyMode.THROW =====

    @Nested
    class DuplicateKeyThrow {
        @Test
        public void testAddDuplicateKeyThrows() {
            UUID uuid = id();
            var first = new EntityStub(uuid, "first");
            var second = new EntityStub(uuid, "second");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId, DuplicateKeyMode.THROW);
            kit.add(first);
            assertThrows(IllegalStateException.class, () -> kit.add(second));
        }

        @Test
        public void testAddSameInstanceDoesNotThrow() {
            UUID uuid = id();
            var entity = new EntityStub(uuid, "test");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId, DuplicateKeyMode.THROW);
            kit.add(entity);
            assertDoesNotThrow(() -> kit.add(entity));
        }

        @Test
        public void testAddEqualInstanceDoesNotThrow() {
            UUID uuid = id();
            var first = new EntityStub(uuid, "test");
            var second = new EntityStub(uuid, "test");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId, DuplicateKeyMode.THROW);
            kit.add(first);
            assertDoesNotThrow(() -> kit.add(second));
        }

        @Test
        public void testThrowViaInitialCollection() {
            UUID uuid = id();
            var first = new EntityStub(uuid, "first");
            var second = new EntityStub(uuid, "second");
            assertThrows(IllegalStateException.class,
                    () -> new Kit<>(Arrays.asList(first, second), EntityStub::getId, DuplicateKeyMode.THROW));
        }

        @Test
        public void testThrowDetectsInGetMapAfterAddAll() {
            UUID uuid = id();
            var first = new EntityStub(uuid, "first");
            var second = new EntityStub(uuid, "second");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId, DuplicateKeyMode.THROW);
            kit.add(first);
            assertThrows(IllegalStateException.class, () -> kit.addAll(Collections.singletonList(second)));
        }
    }

    // ===== null key =====

    @Nested
    class NullKeyTests {
        @Test
        public void testAddNullKeyThrowsNPE() {
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            var entityWithNullId = new EntityStub(null, "null-key");
            NullPointerException ex = assertThrows(NullPointerException.class, () -> kit.add(entityWithNullId));
            assertTrue(ex.getMessage().contains("Kit does not supports null keys"));
        }

        @Test
        public void testAddNullKeyThrowsNPEViaAddAll() {
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            var entityWithNullId = new EntityStub(null, "null-key");
            assertThrows(NullPointerException.class, () -> kit.addAll(Collections.singletonList(entityWithNullId)));
        }

        @Test
        public void testAddNullKeyThrowsNPEViaConstructor() {
            var entityWithNullId = new EntityStub(null, "null-key");
            assertThrows(NullPointerException.class,
                    () -> new Kit<>(Collections.singletonList(entityWithNullId), EntityStub::getId));
        }

        @Test
        public void testKitStillUsableAfterNullKeyRejection() {
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            var entityWithNullId = new EntityStub(null, "null-key");
            assertThrows(NullPointerException.class, () -> kit.add(entityWithNullId));
            assertEquals(0, kit.size());
            UUID uuid = id();
            var valid = new EntityStub(uuid, "valid");
            kit.add(valid);
            assertEquals(1, kit.size());
            assertEquals(valid, kit.get(uuid));
        }
    }

    // ===== addAll =====

    @Nested
    class AddAllTests {
        @Test
        public void testAddAll() {
            var e1 = new EntityStub(id(), "a");
            var e2 = new EntityStub(id(), "b");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            kit.addAll(Arrays.asList(e1, e2));
            assertEquals(e1, kit.get(e1.getId()));
            assertEquals(e2, kit.get(e2.getId()));
        }

        @Test
        public void testAddAllIncrementalMapUpdate() {
            var e1 = new EntityStub(id(), "a");
            var e2 = new EntityStub(id(), "b");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            kit.add(e1);
            kit.getMap();
            kit.addAll(Collections.singletonList(e2));
            assertEquals(e2, kit.get(e2.getId()));
            assertEquals(e1, kit.get(e1.getId()));
        }
    }

    // ===== remove / clear =====

    @Nested
    class RemoveTests {
        @Test
        public void testRemove() {
            UUID uuid = id();
            var entity = new EntityStub(uuid, "test");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            kit.add(entity);
            assertTrue(kit.remove(entity));
            assertNull(kit.get(uuid));
            assertEquals(0, kit.size());
        }

        @Test
        public void testRemoveFromEmptyKit() {
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            assertFalse(kit.remove(new EntityStub(id(), "x")));
        }

        @Test
        public void testClear() {
            var e1 = new EntityStub(id(), "a");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            kit.add(e1);
            kit.clear();
            assertEquals(0, kit.size());
            assertNull(kit.get(e1.getId()));
        }

        @Test
        public void testClearEmptyKit() {
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            assertDoesNotThrow(kit::clear);
        }
    }

    // ===== toArray =====

    @Nested
    class ToArrayTests {
        @Test
        public void testToArrayNotNullOnEmptyKit() {
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            Object[] arr = kit.toArray();
            assertNotNull(arr);
            assertEquals(0, arr.length);
        }

        @Test
        public void testToArrayGenericOnEmptyKit() {
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            EntityStub[] arr = kit.toArray(new EntityStub[0]);
            assertNotNull(arr);
            assertEquals(0, arr.length);
        }

        @Test
        public void testToArrayReturnsElements() {
            var e1 = new EntityStub(id(), "a");
            var e2 = new EntityStub(id(), "b");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            kit.addAll(Arrays.asList(e1, e2));
            Object[] arr = kit.toArray();
            assertEquals(2, arr.length);
        }
    }

    // ===== containsKey =====

    @Nested
    class ContainsKeyTests {
        @Test
        public void testContainsKey() {
            UUID uuid = id();
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            kit.add(new EntityStub(uuid, "test"));
            assertTrue(kit.containsKey(uuid));
            assertFalse(kit.containsKey(id()));
        }

        @Test
        public void testContainsKeyCaseInsensitive() {
            Kit<String, String> kit = new Kit<>(Function.identity(), String::toLowerCase);
            kit.add("Hello");
            assertTrue(kit.containsKey("hello"));
            assertTrue(kit.containsKey("HELLO"));
            assertTrue(kit.containsKey("Hello"));
            assertFalse(kit.containsKey("world"));
        }
    }

    // ===== Kit.EMPTY =====

    @Nested
    class EmptyKitTests {
        @Test
        public void testEmptyKitIsImmutable() {
            Kit<EntityStub, UUID> empty = Kit.emptyKit();
            assertEquals(0, empty.size());
            assertThrows(UnsupportedOperationException.class, () -> empty.add(new EntityStub(id(), "x")));
            assertThrows(UnsupportedOperationException.class, () -> empty.addAll(Collections.emptyList()));
            assertThrows(UnsupportedOperationException.class, empty::clear);
        }

        @Test
        public void testEmptyKitGetReturnsNull() {
            Kit<EntityStub, UUID> empty = Kit.emptyKit();
            assertNull(empty.get(id()));
        }

        @Test
        public void testEmptyKitGetMapReturnsEmpty() {
            Kit<EntityStub, UUID> empty = Kit.emptyKit();
            assertTrue(empty.getMap().isEmpty());
        }
    }

    // ===== KitIterator =====

    @Nested
    class IteratorTests {
        @Test
        public void testIteratorRemove() {
            var e1 = new EntityStub(id(), "a");
            var e2 = new EntityStub(id(), "b");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            kit.addAll(Arrays.asList(e1, e2));
            Iterator<EntityStub> it = kit.iterator();
            it.next();
            it.remove();
            assertEquals(1, kit.size());
        }

        @Test
        public void testIteratorRemoveUpdatesMap() {
            var e1 = new EntityStub(id(), "a");
            var e2 = new EntityStub(id(), "b");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            kit.addAll(Arrays.asList(e1, e2));
            kit.getMap();
            Iterator<EntityStub> it = kit.iterator();
            EntityStub removed = it.next();
            it.remove();
            assertNull(kit.get(removed.getId()));
        }

        @Test
        public void testIteratorOnEmptyKit() {
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            assertFalse(kit.iterator().hasNext());
        }
    }

    // ===== getIdSet / getIdSetSafe =====

    @Nested
    class IdSetTests {
        @Test
        public void testGetIdSetSafe() {
            var e1 = new EntityStub(id(), "a");
            var e2 = new EntityStub(id(), "b");
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            kit.addAll(Arrays.asList(e1, e2));
            Set<UUID> ids = kit.getIdSetSafe();
            assertEquals(2, ids.size());
            assertTrue(ids.contains(e1.getId()));
            assertTrue(ids.contains(e2.getId()));
        }

        @Test
        public void testGetIdSetSafeOnEmptyKit() {
            Kit<EntityStub, UUID> kit = new Kit<>(EntityStub::getId);
            assertNotNull(kit.getIdSetSafe());
            assertTrue(kit.getIdSetSafe().isEmpty());
        }
    }

    // ===== Helper stub =====

    static class EntityStub {
        private final UUID id;
        private final String name;

        EntityStub(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        public UUID getId() { return id; }
        public String getName() { return name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EntityStub that)) return false;
            return Objects.equals(id, that.id) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }
}
