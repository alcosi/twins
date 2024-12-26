package org.twins.core.dto.rest.link;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinBaseDTOv2;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "TwinLinkViewV1")
public class TwinLinkViewDTOv1 extends TwinLinkAddDTOv1 {
    @Schema(description = "id", example = DTOExamples.LINK_ID)
    public UUID id;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "createdByUser", example = DTOExamples.USER_ID)
    public UUID createdByUserId;

    @Schema(description = "createdByUser")
    public UserDTOv1 createdByUser;

    @Schema(description = "Link")
    public LinkDTOv1 link;

    @Schema(description = "Source twin")
    public TwinBaseDTOv2 srcTwin;

    @Schema(description = "Destination twin")
    public TwinBaseDTOv2 dstTwin;
}
