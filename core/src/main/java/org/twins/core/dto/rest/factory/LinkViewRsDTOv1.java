package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.link.LinkDTOv3;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "LinkViewRsDTOv1")
public class LinkViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - link")
    public LinkDTOv3 link;
}
