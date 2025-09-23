package org.twins.core.dto.rest.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CacheInfoRsV1")
public class CacheInfoRsDTOv1 extends Response {

    @Schema(description = "List of cache information")
    public List<CacheInfoDTO> caches;

}
