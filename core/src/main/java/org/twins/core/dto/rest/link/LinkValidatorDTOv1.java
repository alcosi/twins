package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "LinkValidatorV1")
public class LinkValidatorDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "link id this validator belongs to")
    @RelatedObject(type = LinkDTOv1.class, name = "link")
    public UUID linkId;

    @Schema(description = "link validator order within the link")
    public Integer order;

    @Schema(description = "linker featurer id")
    @RelatedObject(type = FeaturerDTOv1.class, name = "linkerFeaturer")
    public Integer linkerFeaturerId;

    @Schema(description = "linker featurer params")
    public HashMap<String, String> linkerParams;
}
