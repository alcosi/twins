package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "LinkV1")
public class LinkDTOv1 {
    @Schema(description = "id", example = DTOExamples.LINK_ID)
    public UUID id;

    @Schema(description = "Source twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "srcTwinClass")
    public UUID srcTwinClassId;

    @Schema(example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "dstTwinClass")
    public UUID dstTwinClassId;

    @Schema(description = "Relation twin class id — when set, each twin_link of this link gets a shadow twin "
            + "of that class carrying the relation's extra attributes", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "relationTwinClass")
    public UUID relationTwinClassId;

    @Schema(description = "name", example = "Serial number")
    public String name;

    @Schema(description = "Backward name", example = "dst -> src")
    public String backwardName;

    @Schema(description = "is inheritable", example = "")
    public Boolean inheritable;

    @Schema(description = "Ling strength id", example = "OPTIONAL")
    public LinkStrength linkStrengthId;

    @Schema(description = "link type", example = "ManyToOne")
    public LinkType type;

    @Schema(description = "Creator user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;

    @Schema(description = "Creation timestamp")
    public LocalDateTime createdAt;
}


