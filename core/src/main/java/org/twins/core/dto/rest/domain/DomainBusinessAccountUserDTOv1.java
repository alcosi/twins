package org.twins.core.dto.rest.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainBusinessAccountUserV1")
public class DomainBusinessAccountUserDTOv1 {
    @Schema(description = "user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "user")
    public UUID userId;

    @Schema(description = "business account id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "last activity at", example = DTOExamples.INSTANT)
    public LocalDateTime lastActivityAt;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "an ids of user groups")
    @RelatedObject(type = UserGroupDTOv1.class, name = "userGroupList")
    public Set<UUID> userGroupIds;
}
