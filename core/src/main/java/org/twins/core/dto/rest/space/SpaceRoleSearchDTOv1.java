package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "SpaceRoleSearchV1")
public class SpaceRoleSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;
    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;
    @Schema(description = "twin class id list")
    public Set<UUID> twinClassIdList;
    @Schema(description = "twin class id exclude list")
    public Set<UUID> twinClassIdExcludeList;
    @Schema(description = "business account id list")
    public Set<UUID> businessAccountIdList;
    @Schema(description = "business account id exclude list")
    public Set<UUID> businessAccountIdExcludeList;
    @Schema(description = "key like list")
    public Set<String> keyLikeList;
    @Schema(description = "key not like list")
    public Set<String> keyNotLikeList;
    @Schema(description = "name i18n like list")
    public Set<String> nameI18nLikeList;
    @Schema(description = "name i18n not like list")
    public Set<String> nameI18nNotLikeList;
    @Schema(description = "description i18n like list")
    public Set<String> descriptionI18nLikeList;
    @Schema(description = "description i18n not like list")
    public Set<String> descriptionI18nNotLikeList;
}
