package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinLinkCountV1")
public class TwinLinkCountDTOv1 extends CountDTOv1 {
    @Schema(description = "source twin id", example = DTOExamples.TWIN_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "srcTwin")
    public UUID srcTwinId;

    @Schema(description = "destination twin id", example = DTOExamples.TWIN_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "dstTwin")
    public UUID dstTwinId;

    @Schema(description = "link id", example = DTOExamples.LINK_ID)
    @RelatedObject(type = LinkDTOv1.class, name = "link")
    public UUID linkId;

    @Schema(description = "created by user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;
}
