package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinCountV1")
public class TwinCountDTOv1 extends CountDTOv1 {
    @Schema(description = "Twin class id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "twinClass")
    public UUID twinClassId;

    @Schema(description = "Twin status id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = TwinStatusDTOv1.class, name = "twinStatus")
    public UUID twinStatusId;

    @Schema(description = "Owner business account id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "ownerBusinessAccount")
    public UUID ownerBusinessAccountId;

    @Schema(description = "Owner user id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = UserDTOv1.class, name = "ownerUser")
    public UUID ownerUserId;

    @Schema(description = "Created by user id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;

    @Schema(description = "Assigner user id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = UserDTOv1.class, name = "assignerUser")
    public UUID assignerUserId;

    @Schema(description = "Head twin id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "headTwin")
    public UUID headTwinId;
}
