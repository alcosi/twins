package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "LinkV1")
public class LinkDTOv1 {
    @Schema(description = "id", example = DTOExamples.LINK_ID)
    public UUID id;

    @Schema(example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "dstTwinClass")
    public UUID dstTwinClassId;

    @Schema(description = "name", example = "Serial number")
    public String name;

    @Schema(description = "Ling strength id", example = "OPTIONAL")
    public LinkStrength linkStrengthId;

    @Schema(description = "link type", example = "ManyToOne")
    public LinkType type;
}


