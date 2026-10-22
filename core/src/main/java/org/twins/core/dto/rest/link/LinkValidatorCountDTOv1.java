package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "LinkValidatorCountV1")
public class LinkValidatorCountDTOv1 extends CountDTOv1 {
    @Schema(description = "link id")
    @RelatedObject(type = LinkDTOv1.class, name = "link")
    public UUID linkId;

    @Schema(description = "linker featurer id")
    @RelatedObject(type = FeaturerDTOv1.class, name = "linkerFeaturer")
    public Integer linkerFeaturerId;
}
