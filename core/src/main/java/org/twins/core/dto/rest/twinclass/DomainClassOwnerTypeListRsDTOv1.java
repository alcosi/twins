package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainClassOwnerTypeListRsV1")
public class DomainClassOwnerTypeListRsDTOv1 extends Response {
    @Schema(description = "results - twin class owner types")
    public List<TwinClassOwnerTypeDTOv1> twinClassOwnerTypes;
}
