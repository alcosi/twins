package org.twins.core.service.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryPoolCheck {
    public record PoolInfo(String name, String type, long used) {
    }

    public static List<PoolInfo> getInfo() {
        List<MemoryPoolMXBean> poolBeans = ManagementFactory.getMemoryPoolMXBeans();
        var pools = poolBeans.stream().map(bean -> new PoolInfo(bean.getName(), bean.getType().toString(), bean.getUsage().getUsed())).collect(Collectors.toUnmodifiableList());
        return pools;
    }
}