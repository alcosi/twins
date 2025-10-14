package org.twins.core.dto.rest.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CacheRsV1")
public class CacheRsDTOv1 extends Response {
    @Schema(description = "Cache info")
    public CacheInfoDTO cacheInfo;
}
