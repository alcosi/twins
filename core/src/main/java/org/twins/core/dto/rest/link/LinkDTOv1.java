package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDescriptorDTO;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassLinkV1")
public class LinkDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID id;

    @Schema(description = "key", example = DTOExamples.TWIN_CLASS_ID)
    public TwinClassBaseDTOv1 dstTwinClass;

    @Schema(description = "name", example = "Serial number")
    public String name;

    @Schema(description = "if link is mandatory, twin can not be created without it", example = "false")
    public boolean mandatory;

    @Schema(description = "link type", example = "ManyToOne")
    public LinkEntity.TwinlinkType type;
}
