package org.twins.core.service.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

public class MemoryCheck {
    public record MemoryInfo(String name, long used, long committed, long max) {
    }

    public static List<MemoryInfo> getInfo() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        var nonHeap = memoryBean.getNonHeapMemoryUsage();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        return List.of(new MemoryInfo("heap", heapUsage.getUsed(), heapUsage.getCommitted(), heapUsage.getMax()), new MemoryInfo("nonheap", nonHeap.getUsed(), nonHeap.getCommitted(), nonHeap.getMax()));
    }
}
