package org.twins.core.domain.system;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MemoryPoolInfo {
    private String name;
    private String type;
    private long used;
}
