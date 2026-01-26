package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.user.UserGroupType;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "UserGroupSearchRqV1")
public class UserGroupSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "name i18n like list")
    public Set<String> nameI18NLikeList;

    @Schema(description = "name i18n not like list")
    public Set<String> nameI18nNotLikeList;

    @Schema(description = "description i18n like list")
    public Set<String> descriptionI18NLikeList;

    @Schema(description = "description i18n not like list")
    public Set<String> descriptionI18NNotLikeList;

    @Schema(description = "type list")
    public Set<UserGroupType> typeList;

    @Schema(description = "type exclude list")
    public Set<UserGroupType> typeExcludeList;
}
