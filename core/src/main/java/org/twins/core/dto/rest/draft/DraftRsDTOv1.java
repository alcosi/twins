package org.twins.core.dto.rest.draft;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "DraftRsV1")
public class DraftRsDTOv1 extends Response {
    @Schema(description = "draft")
    public DraftDTOv1 draft;
}
