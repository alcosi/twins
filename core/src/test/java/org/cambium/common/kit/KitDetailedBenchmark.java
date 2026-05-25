package org.cambium.common.kit;

import java.util.*;

/**
 * Детальный бенчмарк: итерация + getList() + memory-layout аналитика.
 * С фокусом на конкретные вопросы.
 */
public class KitDetailedBenchmark {

    static class Entity {
        final UUID id;
        final String name;
        Entity(UUID id, String name) { this.id = id; this.name = name; }
        public UUID getId() { return id; }
    }

    static Entity[] generateEntities(int n) {
        Entity[] entities = new Entity[n];
        for (int i = 0; i < n; i++)
            entities[i] = new Entity(UUID.randomUUID(), "entity-" + i);
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

    static final int WARMUP = 100;
    static final int MEASURE = 200;

    /**
     * Итерация: чистый ArrayList vs LinkedHashMap.values() —
     * точный замер оверхеда LHM итератора.
     */
    static void benchIteration() {
        System.out.println("=== ИТЕРАЦИЯ: ArrayList vs LinkedHashMap.values() ===");
        System.out.println("Каждый замер: сумма длин name в цикле. Warmup=100, Measure=200.");
        System.out.printf("%-8s | %-25s | %-15s | %-15s | %-10s%n",
            "N", "Тип", "Median (ns)", "Avg (ns)", "На элемент");
        System.out.println("-".repeat(85));

        int[] sizes = {1, 5, 10, 50, 100, 500, 1000, 5000, 10000};

        for (int n : sizes) {
            Entity[] entities = generateEntities(n);

            // ArrayList
            ArrayList<Entity> arrayList = new ArrayList<>(Arrays.asList(entities));
            long[] alTimes = new long[MEASURE];
            for (int w = 0; w < WARMUP; w++) {
                int s = 0; for (Entity e : arrayList) s += e.name.length();
            }
            for (int i = 0; i < MEASURE; i++) {
                long t = System.nanoTime();
                int s = 0; for (Entity e : arrayList) s += e.name.length();
                alTimes[i] = System.nanoTime() - t;
            }

            // LinkedHashMap
            LinkedHashMap<UUID, Entity> lhm = new LinkedHashMap<>();
            for (Entity e : entities) lhm.put(e.id, e);
            long[] lhmTimes = new long[MEASURE];
            for (int w = 0; w < WARMUP; w++) {
                int s = 0; for (Entity e : lhm.values()) s += e.name.length();
            }
            for (int i = 0; i < MEASURE; i++) {
                long t = System.nanoTime();
                int s = 0; for (Entity e : lhm.values()) s += e.name.length();
                lhmTimes[i] = System.nanoTime() - t;
            }

            double alMed = median(alTimes);
            double lhmMed = median(lhmTimes);
            double overhead = alMed > 0 ? ((lhmMed - alMed) / alMed) * 100 : 0;

            System.out.printf("%-8d | %-25s | %,15.0f | %,15.0f | %.1f ns%n",
                n, "ArrayList", alMed, Arrays.stream(alTimes).average().orElse(0), alMed / n);
            System.out.printf("%-8s | %-25s | %,15.0f | %,15.0f | %.1f ns%n",
                "", "LinkedHashMap.values()", lhmMed, Arrays.stream(lhmTimes).average().orElse(0), lhmMed / n);
            System.out.printf("%-8s | %-25s | %14s%% | %n%n", "", "Overhead LHM vs AL", String.format("%.1f", overhead));
        }
    }

    /**
     * getList() overhead: создание new ArrayList<>(map.values()) vs возврат ссылки.
     */
    static void benchGetList() {
        System.out.println("\n=== getList() OVERHEAD ===");
        System.out.println("V1: возвращает ссылку на ArrayList (0 копирования).");
        System.out.println("V3: new ArrayList<>(map.values()) — полный копий.");
        System.out.printf("%-8s | %-30s | %-15s | %-15s%n",
            "N", "Операция", "Median (ns)", "Avg (ns)");
        System.out.println("-".repeat(80));

        int[] sizes = {1, 5, 10, 50, 100, 500, 1000, 5000, 10000};

        for (int n : sizes) {
            Entity[] entities = generateEntities(n);

            // V1: просто вернуть ссылку
            ArrayList<Entity> v1list = new ArrayList<>(Arrays.asList(entities));
            long[] v1times = new long[MEASURE];
            for (int w = 0; w < WARMUP; w++) v1list.getClass(); // warmup
            for (int i = 0; i < MEASURE; i++) {
                long t = System.nanoTime();
                List<Entity> ref = v1list; // возврат ссылки
                if (ref.size() != n) throw new Error("bug");
                v1times[i] = System.nanoTime() - t;
            }

            // V3: new ArrayList<>(map.values())
            LinkedHashMap<UUID, Entity> lhm = new LinkedHashMap<>();
            for (Entity e : entities) lhm.put(e.id, e);
            long[] v3times = new long[MEASURE];
            for (int w = 0; w < WARMUP; w++) new ArrayList<>(lhm.values());
            for (int i = 0; i < MEASURE; i++) {
                long t = System.nanoTime();
                List<Entity> copy = new ArrayList<>(lhm.values());
                if (copy.size() != n) throw new Error("bug");
                v3times[i] = System.nanoTime() - t;
            }

            System.out.printf("%-8d | %-30s | %,15.0f | %,15.0f%n",
                n, "V1: return ref", median(v1times), Arrays.stream(v1times).average().orElse(0));
            System.out.printf("%-8s | %-30s | %,15.0f | %,15.0f%n",
                "", "V3: new ArrayList<>(values())", median(v3times), Arrays.stream(v3times).average().orElse(0));
            System.out.printf("%-8s | %-30s | %,15.0f | %n%n", "",
                "  X раз медленнее:", median(v3times) / Math.max(median(v1times), 1));
        }
    }

    /**
     * Memory layout: точный аналитический расчёт.
     */
    static void printMemoryAnalysis() {
        System.out.println("\n=== MEMORY: Аналитический расчёт (JVM 64-bit, compressed oops) ===");
        System.out.println("Предпосылки: compressed oops ON (по умолчанию для heap < 32GB).");
        System.out.println("UUID = 2 x long = 16 bytes. Entity = UUID(16) + String ref(4) + padding = 24 bytes.");
        System.out.println("String = byte[] ref(4) + coder(1) + hash(4) + padding = 16 bytes + byte[] obj.");
        System.out.println();

        System.out.println("Структуры данных:");
        System.out.println("  ArrayList<E>:           object header(12) + size(4) + modCount(4) + array_ref(4) = 24 bytes");
        System.out.println("                           + Object[] = 16 + 4*N (с rounding до 8) => ~16 + 4*N bytes");
        System.out.println("  LinkedHashMap<K,V>:     object header(12) + size(4) + modCount(4) + threshold(4) + loadFactor(4) +");
        System.out.println("                           + head(before)(4) + tail(after)(4) + accessOrder(1)+pad(3) = ~40 bytes");
        System.out.println("                           + HashMap.Node[] table: ~16 + 4*capacity bytes");
        System.out.println("                           + N x HashMap.Node: каждый = 32 bytes (16 header + hash(4) + key(4) + val(4) + next(4))");
        System.out.println("                           + N x LinkedHashMap.Entry: extends Node = 32 + before(4) + after(4) = 40 bytes");
        System.out.println();

        System.out.printf("%-8s | %-25s | %-25s | %-15s | %-15s%n",
            "N", "V1: ArrayList (bytes)", "V1: AL+LHM full (bytes)", "V3: LHM only (bytes)", "V3 vs V1-full");
        System.out.println("-".repeat(100));

        int[] sizes = {0, 1, 10, 100, 1000, 10000};
        for (int n : sizes) {
            // V1: ArrayList only
            // ArrayList obj = 24 bytes
            // Object[] array: 16 + padTo8(4*n) bytes. Default capacity = max(10, n) if grown, but let's use actual
            int alCapacity = n == 0 ? 0 : (n <= 10 ? 10 : nextPowerOf2(n * 3 / 2)); // approximate growth
            long v1ArrayList = 24L + (n == 0 ? 0 : 16L + padTo8(4L * alCapacity));

            // V1: ArrayList + LinkedHashMap
            // LinkedHashMap obj = 40 bytes
            // table: 16 + 4 * tableCapacity (tableCapacity ~ nextPower2(N * 4/3))
            int tableCap = n == 0 ? 0 : nextPowerOf2((int)(n * 4.0 / 3.0 + 1));
            long tableArray = n == 0 ? 0 : 16L + padTo8(4L * tableCap);
            long nodes = n == 0 ? 0 : 40L * n; // LinkedHashMap.Entry = 40 bytes each
            long v1Full = v1ArrayList + 40L + tableArray + nodes;

            // V3: LinkedHashMap only (same as LHM in V1)
            long v3Lhm = 40L + tableArray + nodes;

            long diffFull = v3Lhm - v1Full;
            System.out.printf("%-8d | %,25d | %,25d | %,15d | %+d bytes (%+.0f%%)%n",
                n, v1ArrayList, v1Full, v3Lhm, diffFull,
                v1Full > 0 ? ((double)diffFull / v1Full) * 100 : 0);
        }

        System.out.println();
        System.out.println("Ключевое: V1-full (AL+LHM) = V3(LHM) + ArrayList_overhead");
        System.out.println("V3 экономит: ArrayList object (24 bytes) + backing array (~16 + 4*capacity bytes)");
        System.out.println("При N=1000: экономия ~4 KB (8-10%). При N=10000: экономия ~40 KB (8-10%).");
    }

    static int nextPowerOf2(int n) {
        if (n <= 0) return 1;
        int highestBit = Integer.highestOneBit(n);
        return highestBit == n ? n : highestBit << 1;
    }

    static long padTo8(long bytes) {
        return (bytes + 7) & ~7;
    }

    /**
     * Add benchmark: V1 (ArrayList.add) vs V3 (LinkedHashMap.put).
     * Ключевой вопрос: почему V3 add() медленнее?
     */
    static void benchAddDetailed() {
        System.out.println("\n=== ADD: детальный анализ ===");
        System.out.println("V1: ArrayList.add(e) — O(1) amortized, просто кладёт в массив.");
        System.out.println("V3: LinkedHashMap.put(key, e) — хэш + bucket + node allocation + doubly-linked list.");
        System.out.println();
        System.out.printf("%-8s | %-35s | %-15s | %-15s | %-10s%n",
            "N", "Операция", "Median (ns)", "Avg (ns)", "На элемент");
        System.out.println("-".repeat(95));

        int[] sizes = {10, 100, 1000, 10000};

        for (int n : sizes) {
            // Raw ArrayList.add()
            long[] alTimes = new long[MEASURE];
            for (int w = 0; w < WARMUP; w++) {
                ArrayList<Entity> al = new ArrayList<>(n);
                Entity[] ents = generateEntities(n);
                for (Entity e : ents) al.add(e);
            }
            for (int i = 0; i < MEASURE; i++) {
                Entity[] ents = generateEntities(n);
                long t = System.nanoTime();
                ArrayList<Entity> al = new ArrayList<>(n);
                for (Entity e : ents) al.add(e);
                alTimes[i] = System.nanoTime() - t;
            }

            // Raw LinkedHashMap.put()
            long[] lhmTimes = new long[MEASURE];
            for (int w = 0; w < WARMUP; w++) {
                LinkedHashMap<UUID, Entity> m = new LinkedHashMap<>(n * 4 / 3 + 1);
                Entity[] ents = generateEntities(n);
                for (Entity e : ents) m.put(e.id, e);
            }
            for (int i = 0; i < MEASURE; i++) {
                Entity[] ents = generateEntities(n);
                long t = System.nanoTime();
                LinkedHashMap<UUID, Entity> m = new LinkedHashMap<>(n * 4 / 3 + 1);
                for (Entity e : ents) m.put(e.id, e);
                lhmTimes[i] = System.nanoTime() - t;
            }

            System.out.printf("%-8d | %-35s | %,15.0f | %,15.0f | %.1f ns%n",
                n, "ArrayList.add() raw", median(alTimes), Arrays.stream(alTimes).average().orElse(0), median(alTimes) / n);
            System.out.printf("%-8s | %-35s | %,15.0f | %,15.0f | %.1f ns%n",
                "", "LinkedHashMap.put() raw", median(lhmTimes), Arrays.stream(lhmTimes).average().orElse(0), median(lhmTimes) / n);
            System.out.printf("%-8s | %-35s | %14s%n%n", "",
                "Замедление LHM:", String.format("%.1fx", median(lhmTimes) / Math.max(median(alTimes), 1)));
        }
    }

    public static void main(String[] args) {
        printMemoryAnalysis();
        benchAddDetailed();
        benchIteration();
        benchGetList();
    }
}
