package org.twins.core.domain.system;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CacheInfoDTO {
    private String cacheName;
    private Double sizeInMb;
    private Long itemsCount;
}