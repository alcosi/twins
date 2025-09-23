package org.twins.core.domain.system;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CacheInfo {
    private String cacheName;
    private Double sizeInMb;
    private Long itemsCount;
}
