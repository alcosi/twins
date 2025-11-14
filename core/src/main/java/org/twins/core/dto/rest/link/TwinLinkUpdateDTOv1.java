
package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinLinkUpdateV1")
public class TwinLinkUpdateDTOv1 extends TwinLinkSaveDTOv1 {
    @Schema(description = "id", example = DTOExamples.LINK_ID)
    public UUID id;
}
