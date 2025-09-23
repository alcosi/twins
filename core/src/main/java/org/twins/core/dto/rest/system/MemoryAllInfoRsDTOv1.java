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
@Schema(name = "MemoryAllInfoRsV1")
public class MemoryAllInfoRsDTOv1 extends Response {

    @Schema(description = "List of memory information")
    private List<MemoryInfoDTO> memoryInfo;

    @Schema(description = "List of memory pool information")
    private List<MemoryPoolInfoDTO> memoryPoolInfo;

}
