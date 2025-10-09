package org.twins.core.dto.rest.twinflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryDTOv1;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinflowTransitionBaseV2")
public class TwinflowTransitionBaseDTOv2 extends TwinflowTransitionBaseDTOv1 {
    @Schema(example = DTOExamples.TWIN_STATUS_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "srcTwinStatus")
    public UUID srcTwinStatusId;

    @Schema(description = "srcStatus")
    public TwinStatusDTOv1 srcTwinStatus;

    @Schema(example = DTOExamples.PERMISSION_ID)
    @RelatedObject(type = PermissionDTOv1.class, name = "permission")
    public UUID permissionId;

    @Schema(description = "permission details")
    public PermissionDTOv1 permission;

    @Schema(description = "twinflow id", example = DTOExamples.TWINFLOW_ID)
    @RelatedObject(type = TwinflowTransitionBaseDTOv1.class, name = "twinflow")
    public UUID twinflowId;

    @Schema(description = "twinflow")
    public TwinflowBaseDTOv1 twinflow;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "twinflow author")
    public UserDTOv1 createdByUser;

    @Schema(description = "createdByUserId")
    @RelatedObject(type = TwinDTOv2.class, name = "createdByUser")
    public UUID createdByUserId;

    @Schema(description = "inbuilt factory id", example = DTOExamples.FACTORY_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "inbuiltTwinFactory")
    public UUID inbuiltTwinFactoryId;

    @Schema(description = "inbuilt factory")
    public FactoryDTOv1 inbuiltTwinFactory;

    @Schema(description = "drafting factory id", example = DTOExamples.FACTORY_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "draftingTwinFactory")
    public UUID draftingTwinFactoryId;

    @Schema(description = "drafting factory")
    public FactoryDTOv1 draftingTwinFactory;

}


