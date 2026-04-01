package org.twins.core.dto.rest.usergroup;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserGroupInvolveActAsUserV1")
public class UserGroupInvolveActAsUserDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "machine user id")
    @RelatedObject(type = UserDTOv1.class, name = "machineUser")
    public UUID machineUserId;

    @Schema(description = "userGroup id)")
    @RelatedObject(type = UserGroupDTOv1.class,name = "userGroup")
    public UUID userGroupId;

    @Schema(description = "added by user id")
    @RelatedObject(type = UserDTOv1.class, name = "machineUser")
    public UUID addedByUserId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "added at", example = DTOExamples.INSTANT)
    public LocalDateTime addedAt;
}
