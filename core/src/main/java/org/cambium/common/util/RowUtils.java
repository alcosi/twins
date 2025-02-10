package org.cambium.common.util;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RowUtils {
    public static Map<UUID, Integer> mapUuidInt(List<Object[]> list) {
        return list.stream().collect(Collectors.toMap(
                row -> (UUID) row[0],
                row -> ((Long) row[1]).intValue()
        ));
    }
}
