package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainUserSearchRqV1")
public class DomainUserSearchRqDTOv1 extends Request {
    @Schema(description = "user id list")
    public Set<UUID> userIdList;

    @Schema(description = "user id exclude list")
    public Set<UUID> userIdExcludeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "email like list")
    public Set<String> emailLikeList;

    @Schema(description = "email not like list")
    public Set<String> emailNotLikeList;

    @Schema(description = "status id list")
    public Set<UserStatus> statusIdList;

    @Schema(description = "status id exclude list")
    public Set<UserStatus> statusIdExcludeList;

    @Schema(description = "business account id list")
    public Set<UUID> businessAccountIdList;

    @Schema(description = "business account id exclude list")
    public Set<UUID> businessAccountIdExcludeList;
}
