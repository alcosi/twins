package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "LinkCountV1")
public class LinkCountDTOv1 extends CountDTOv1 {
    @Schema(description = "source twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "srcTwinClass")
    public UUID srcTwinClassId;

    @Schema(description = "destination twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "dstTwinClass")
    public UUID dstTwinClassId;

    @Schema(description = "created by user", example = DTOExamples.UUID_ID)
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;

    @Schema(description = "link type")
    public LinkType type;

    @Schema(description = "link strength")
    public LinkStrength linkStrength;

    @Schema(description = "source twin class inheritable")
    public Boolean srcTwinClassInheritable;

    @Schema(description = "destination twin class inheritable")
    public Boolean dstTwinClassInheritable;
}
