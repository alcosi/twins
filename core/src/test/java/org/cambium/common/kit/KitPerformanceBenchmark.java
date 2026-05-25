package org.cambium.common.kit;

import java.util.*;
import java.util.function.Function;

/**
 * Микробенчмарк: Kit v1 (ArrayList + lazy LinkedHashMap) vs v3 (LinkedHashMap only).
 * Не JMH — но с прогревом и усреднением. Достаточно для принятия решения.
 */
public class KitPerformanceBenchmark {

    static class Entity {
        final UUID id;
        final String name;
        Entity(UUID id, String name) { this.id = id; this.name = name; }
        public UUID getId() { return id; }
    }

    // ===================== V1 (текущая реализация) =====================
    static class KitV1<E, K> {
        private ArrayList<E> list;
        private LinkedHashMap<K, E> map;
        private final Function<? super E, ? extends K> keyFn;

        KitV1(Function<? super E, ? extends K> keyFn) {
            this.keyFn = keyFn;
        }

        boolean add(E e) {
            if (list == null) list = new ArrayList<>();
            if (map != null) {
                K key = keyFn.apply(e);
                map.put(key, e);
            }
            return list.add(e);
        }

        E get(K key) {
            if (map == null) buildMap();
            return map.get(key);
        }

        boolean containsKey(K key) {
            if (map == null) buildMap();
            return map.containsKey(key);
        }

        List<E> getList() {
            if (list == null) return Collections.emptyList();
            return list;
        }

        LinkedHashMap<K, E> getMap() {
            if (map == null) buildMap();
            return map;
        }

        boolean remove(E e) {
            if (list == null) return false;
            boolean ret = list.remove(e);
            if (ret && map != null) {
                map.remove(keyFn.apply(e));
            }
            return ret;
        }

        int size() { return list == null ? 0 : list.size(); }

        private void buildMap() {
            if (list == null) { map = new LinkedHashMap<>(); return; }
            map = new LinkedHashMap<>(list.size() * 4 / 3 + 1);
            for (E e : list) map.put(keyFn.apply(e), e);
        }

        Iterator<E> iterator() { return list != null ? list.iterator() : Collections.emptyIterator(); }
    }

    // ===================== V3 (LinkedHashMap only) =====================
    static class KitV3<E, K> {
        private LinkedHashMap<K, E> map;
        private final Function<? super E, ? extends K> keyFn;

        KitV3(Function<? super E, ? extends K> keyFn) {
            this.keyFn = keyFn;
        }

        boolean add(E e) {
            if (map == null) map = new LinkedHashMap<>();
            map.put(keyFn.apply(e), e);
            return true;
        }

        E get(K key) {
            if (map == null) return null;
            return map.get(key);
        }

        boolean containsKey(K key) {
            if (map == null) return false;
            return map.containsKey(key);
        }

        List<E> getList() {
            if (map == null) return Collections.emptyList();
            return new ArrayList<>(map.values());
        }

        LinkedHashMap<K, E> getMap() {
            if (map == null) map = new LinkedHashMap<>();
            return map;
        }

        boolean remove(E e) {
            if (map == null) return false;
            return map.remove(keyFn.apply(e)) != null;
        }

        int size() { return map == null ? 0 : map.size(); }

        Iterator<E> iterator() { return map != null ? map.values().iterator() : Collections.emptyIterator(); }
    }

    // ===================== MEMORY MEASUREMENT =====================

    static long measureMemory(Runnable initializer) {
        System.gc(); System.gc(); System.gc();
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        initializer.run();
        long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.gc(); System.gc();
        long afterGc = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return afterGc - before;
    }

    // ===================== BENCHMARK HELPERS =====================

    static Entity[] generateEntities(int n) {
        Entity[] entities = new Entity[n];
        for (int i = 0; i < n; i++) {
            entities[i] = new Entity(UUID.randomUUID(), "entity-" + i);
        }
        return entities;
    }

    static long timeNs(Runnable r) {
        long start = System.nanoTime();
        r.run();
        return System.nanoTime() - start;
    }

    static double median(long[] arr) {
        long[] sorted = arr.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }

    static final int WARMUP = 50;
    static final int MEASURE = 100;

    static void bench(String name, Runnable warmup, Runnable measured) {
        // warmup
        for (int i = 0; i < WARMUP; i++) warmup.run();
        // measure
        long[] times = new long[MEASURE];
        for (int i = 0; i < MEASURE; i++) {
            times[i] = timeNs(measured);
        }
        double med = median(times);
        double avg = Arrays.stream(times).average().orElse(0);
        System.out.printf("  %-45s median=%,.0f ns  avg=%,.0f ns%n", name, med, avg);
    }

    // ===================== MAIN =====================

    public static void main(String[] args) {
        int[] sizes = {0, 10, 100, 1000, 10000};
        Function<Entity, UUID> keyFn = Entity::getId;

        System.out.println("==========================================================");
        System.out.println("  Kit v1 vs v3 — Performance Benchmark");
        System.out.println("==========================================================\n");

        // ==================== 1. MEMORY ====================
        System.out.println("## Memory v1 vs v3");
        System.out.println("----------------------------------------------------------");
        System.out.printf("%-8s | %-20s | %-20s | %-15s%n", "N", "V1 bytes", "V3 bytes", "Delta");
        System.out.println("----------------------------------------------------------");

        for (int n : sizes) {
            Entity[] entities = generateEntities(n);

            long v1mem = measureMemory(() -> {
                KitV1<Entity, UUID> kit = new KitV1<>(keyFn);
                for (Entity e : entities) kit.add(e);
                // touch map to measure dual-storage cost
                kit.getMap();
            });

            long v3mem = measureMemory(() -> {
                KitV3<Entity, UUID> kit = new KitV3<>(keyFn);
                for (Entity e : entities) kit.add(e);
            });

            String delta;
            if (v1mem == 0 && v3mem == 0) delta = "~0";
            else delta = String.format("%+d (%+.0f%%)", v3mem - v1mem, ((double)(v3mem - v1mem) / Math.max(v1mem, 1)) * 100);
            System.out.printf("%-8d | %,20d | %,20d | %s%n", n, v1mem, v3mem, delta);
        }

        // V1 без инициализации map (только ArrayList)
        System.out.println("\n--- V1: только ArrayList (map не создавался) vs V3 ---");
        System.out.printf("%-8s | %-20s | %-20s | %-15s%n", "N", "V1 (list only)", "V3 (LHM only)", "Delta");
        System.out.println("----------------------------------------------------------");
        for (int n : sizes) {
            Entity[] entities = generateEntities(n);

            long v1listOnly = measureMemory(() -> {
                KitV1<Entity, UUID> kit = new KitV1<>(keyFn);
                for (Entity e : entities) kit.add(e);
                // getMap() NOT called — only ArrayList exists
            });

            long v3mem = measureMemory(() -> {
                KitV3<Entity, UUID> kit = new KitV3<>(keyFn);
                for (Entity e : entities) kit.add(e);
            });

            String delta = String.format("%+d (%+.0f%%)", v3mem - v1listOnly, ((double)(v3mem - v1listOnly) / Math.max(v1listOnly, 1)) * 100);
            System.out.printf("%-8d | %,20d | %,20d | %s%n", n, v1listOnly, v3mem, delta);
        }

        // ==================== 2. CPU: ADD ====================
        System.out.println("\n## CPU: add()");
        System.out.println("----------------------------------------------------------");
        for (int n : sizes) {
            Entity[] entities = generateEntities(n);
            bench(String.format("N=%-6d V1 add %d elements", n, n),
                () -> { KitV1<Entity, UUID> k = new KitV1<>(keyFn); for (Entity e : entities) k.add(e); },
                () -> { KitV1<Entity, UUID> k = new KitV1<>(keyFn); for (Entity e : entities) k.add(e); }
            );
            bench(String.format("N=%-6d V3 add %d elements", n, n),
                () -> { KitV3<Entity, UUID> k = new KitV3<>(keyFn); for (Entity e : entities) k.add(e); },
                () -> { KitV3<Entity, UUID> k = new KitV3<>(keyFn); for (Entity e : entities) k.add(e); }
            );
        }

        // ==================== 3. CPU: GET (lookup by key) ====================
        System.out.println("\n## CPU: get(key) — lookup");
        System.out.println("----------------------------------------------------------");
        for (int n : new int[]{10, 100, 1000, 10000}) {
            Entity[] entities = generateEntities(n);
            // pre-build
            KitV1<Entity, UUID> kitV1 = new KitV1<>(keyFn);
            KitV3<Entity, UUID> kitV3 = new KitV3<>(keyFn);
            for (Entity e : entities) { kitV1.add(e); kitV3.add(e); }

            UUID[] lookupKeys = new UUID[1000];
            for (int i = 0; i < 1000; i++) lookupKeys[i] = entities[i % n].id;

            bench(String.format("N=%-6d V1 get() x1000 (first call, builds map)", n),
                () -> { KitV1<Entity, UUID> k = new KitV1<>(keyFn); for (Entity e : entities) k.add(e); k.get(lookupKeys[0]); },
                () -> { KitV1<Entity, UUID> k = new KitV1<>(keyFn); for (Entity e : entities) k.add(e); k.get(lookupKeys[0]); }
            );
            bench(String.format("N=%-6d V1 get() x1000 (map already built)", n),
                () -> { for (UUID key : lookupKeys) kitV1.get(key); },
                () -> { for (UUID key : lookupKeys) kitV1.get(key); }
            );
            bench(String.format("N=%-6d V3 get() x1000", n),
                () -> { for (UUID key : lookupKeys) kitV3.get(key); },
                () -> { for (UUID key : lookupKeys) kitV3.get(key); }
            );
        }

        // ==================== 4. CPU: containsKey ====================
        System.out.println("\n## CPU: containsKey()");
        System.out.println("----------------------------------------------------------");
        for (int n : new int[]{10, 100, 1000, 10000}) {
            Entity[] entities = generateEntities(n);
            KitV1<Entity, UUID> kitV1 = new KitV1<>(keyFn);
            KitV3<Entity, UUID> kitV3 = new KitV3<>(keyFn);
            for (Entity e : entities) { kitV1.add(e); kitV3.add(e); }
            // warm up map in v1
            kitV1.containsKey(entities[0].id);

            UUID[] lookupKeys = new UUID[1000];
            for (int i = 0; i < 1000; i++) lookupKeys[i] = entities[i % n].id;

            bench(String.format("N=%-6d V1 containsKey() x1000", n),
                () -> { for (UUID key : lookupKeys) kitV1.containsKey(key); },
                () -> { for (UUID key : lookupKeys) kitV1.containsKey(key); }
            );
            bench(String.format("N=%-6d V3 containsKey() x1000", n),
                () -> { for (UUID key : lookupKeys) kitV3.containsKey(key); },
                () -> { for (UUID key : lookupKeys) kitV3.containsKey(key); }
            );
        }

        // ==================== 5. ITERATION ====================
        System.out.println("\n## CPU: Iteration (for-each loop, sum of name lengths)");
        System.out.println("----------------------------------------------------------");
        for (int n : new int[]{10, 100, 1000, 10000}) {
            Entity[] entities = generateEntities(n);
            KitV1<Entity, UUID> kitV1 = new KitV1<>(keyFn);
            KitV3<Entity, UUID> kitV3 = new KitV3<>(keyFn);
            for (Entity e : entities) { kitV1.add(e); kitV3.add(e); }

            bench(String.format("N=%-6d V1 iteration (ArrayList)", n),
                () -> { int s = 0; Iterator<Entity> it = kitV1.iterator(); while (it.hasNext()) s += it.next().name.length(); },
                () -> { int s = 0; Iterator<Entity> it = kitV1.iterator(); while (it.hasNext()) s += it.next().name.length(); }
            );
            bench(String.format("N=%-6d V3 iteration (LHM.values())", n),
                () -> { int s = 0; Iterator<Entity> it = kitV3.iterator(); while (it.hasNext()) s += it.next().name.length(); },
                () -> { int s = 0; Iterator<Entity> it = kitV3.iterator(); while (it.hasNext()) s += it.next().name.length(); }
            );

            // Raw ArrayList vs raw LHM.values() baseline
            ArrayList<Entity> rawList = new ArrayList<>(Arrays.asList(entities));
            LinkedHashMap<UUID, Entity> rawMap = new LinkedHashMap<>();
            for (Entity e : entities) rawMap.put(e.id, e);

            bench(String.format("N=%-6d RAW ArrayList iteration", n),
                () -> { int s = 0; for (Entity e : rawList) s += e.name.length(); },
                () -> { int s = 0; for (Entity e : rawList) s += e.name.length(); }
            );
            bench(String.format("N=%-6d RAW LinkedHashMap.values() iteration", n),
                () -> { int s = 0; for (Entity e : rawMap.values()) s += e.name.length(); },
                () -> { int s = 0; for (Entity e : rawMap.values()) s += e.name.length(); }
            );
        }

        // ==================== 6. getList() overhead in V3 ====================
        System.out.println("\n## CPU: getList() overhead (new ArrayList<>(map.values()))");
        System.out.println("----------------------------------------------------------");
        for (int n : new int[]{10, 100, 1000, 10000}) {
            Entity[] entities = generateEntities(n);
            KitV1<Entity, UUID> kitV1 = new KitV1<>(keyFn);
            KitV3<Entity, UUID> kitV3 = new KitV3<>(keyFn);
            for (Entity e : entities) { kitV1.add(e); kitV3.add(e); }

            bench(String.format("N=%-6d V1 getList() — returns same ArrayList ref", n),
                () -> kitV1.getList(),
                () -> kitV1.getList()
            );
            bench(String.format("N=%-6d V3 getList() — creates new ArrayList copy", n),
                () -> kitV3.getList(),
                () -> kitV3.getList()
            );
        }

        // ==================== 7. REMOVE ====================
        System.out.println("\n## CPU: remove()");
        System.out.println("----------------------------------------------------------");
        for (int n : new int[]{100, 1000, 10000}) {
            bench(String.format("N=%-6d V1 remove all one-by-one", n),
                () -> {
                    Entity[] ents = generateEntities(n);
                    KitV1<Entity, UUID> k = new KitV1<>(keyFn);
                    for (Entity e : ents) k.add(e);
                    k.getMap(); // warm up map
                    for (Entity e : ents) k.remove(e);
                },
                () -> {
                    Entity[] ents = generateEntities(n);
                    KitV1<Entity, UUID> k = new KitV1<>(keyFn);
                    for (Entity e : ents) k.add(e);
                    k.getMap();
                    for (Entity e : ents) k.remove(e);
                }
            );
            bench(String.format("N=%-6d V3 remove all one-by-one", n),
                () -> {
                    Entity[] ents = generateEntities(n);
                    KitV3<Entity, UUID> k = new KitV3<>(keyFn);
                    for (Entity e : ents) k.add(e);
                    for (Entity e : ents) k.remove(e);
                },
                () -> {
                    Entity[] ents = generateEntities(n);
                    KitV3<Entity, UUID> k = new KitV3<>(keyFn);
                    for (Entity e : ents) k.add(e);
                    for (Entity e : ents) k.remove(e);
                }
            );
        }

        // ==================== 8. GC ALLOCATION PATTERNS ====================
        System.out.println("\n## GC: Allocation patterns (add N elements)");
        System.out.println("----------------------------------------------------------");
        for (int n : new int[]{100, 1000, 10000}) {
            Entity[] entities = generateEntities(n);

            // Count allocations by running many iterations and measuring GC
            long v1allocs = countAllocations(() -> {
                KitV1<Entity, UUID> k = new KitV1<>(keyFn);
                for (Entity e : entities) k.add(e);
            }, 1000);

            long v3allocs = countAllocations(() -> {
                KitV3<Entity, UUID> k = new KitV3<>(keyFn);
                for (Entity e : entities) k.add(e);
            }, 1000);

            System.out.printf("  N=%-6d V1 total alloc (KB, ~approx): %,d KB%n", n, v1allocs / 1024);
            System.out.printf("  N=%-6d V3 total alloc (KB, ~approx): %,d KB%n", n, v3allocs / 1024);
        }

        // getList() allocations
        System.out.println("\n## GC: getList() allocations per call");
        System.out.println("----------------------------------------------------------");
        for (int n : new int[]{100, 1000, 10000}) {
            Entity[] entities = generateEntities(n);
            KitV3<Entity, UUID> kitV3 = new KitV3<>(keyFn);
            for (Entity e : entities) kitV3.add(e);

            long allocs = countAllocations(kitV3::getList, 10000);
            System.out.printf("  N=%-6d V3 getList() alloc/call (~approx): %,d bytes  (%.1f KB)%n",
                n, allocs / 10000, (allocs / 10000.0) / 1024);
        }

        System.out.println("\n==========================================================");
        System.out.println("  Benchmark complete.");
        System.out.println("==========================================================");
    }

    static long countAllocations(Runnable r, int iterations) {
        // Force GC, measure free memory before and after many iterations
        System.gc(); System.gc(); System.gc();
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}

        long freeBefore = Runtime.getRuntime().freeMemory();
        for (int i = 0; i < iterations; i++) r.run();
        long freeAfter = Runtime.getRuntime().freeMemory();

        return Math.max(0, freeBefore - freeAfter);
    }
}
