package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "LinkUpdateRsV1")
public class LinkUpdateRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "link")
    public LinkDTOv2 link;
}
