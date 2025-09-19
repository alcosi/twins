package org.twins.core.domain.system;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MemoryInfo {
    private String name;
    private long used;
    private long committed;
    private long max;
}
