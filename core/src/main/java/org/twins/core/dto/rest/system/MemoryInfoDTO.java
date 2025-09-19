package org.twins.core.dto.rest.system;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MemoryInfoDTO {
    private String name;
    private long used;
    private long committed;
    private long max;
}
