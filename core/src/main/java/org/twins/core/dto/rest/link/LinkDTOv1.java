package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkStrength;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassLinkV1")
public class LinkDTOv1 {
    @Schema(description = "id", example = DTOExamples.LINK_ID)
    public UUID id;

    @Schema(example = DTOExamples.TWIN_CLASS_ID)
    public UUID dstTwinClassId;

    @Schema(description = "key", example = DTOExamples.TWIN_CLASS_ID)
    public TwinClassBaseDTOv1 dstTwinClass;

    @Schema(description = "name", example = "Serial number")
    public String name;

    @Schema(description = "Ling strength id", example = "OPTIONAL")
    public LinkStrength linkStrengthId;

    @Schema(description = "link type", example = "ManyToOne")
    public LinkEntity.TwinlinkType type;
}
