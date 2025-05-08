package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinflowListRqV1")
public class TwinflowSearchRqDTOv1 extends Request {

    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin class id map")
    public Map<UUID, Boolean> twinClassIdMap;

    @Schema(description = "twin class id exclude map")
    public Map<UUID, Boolean> twinClassIdExcludeMap;

    @Schema(description = "name i18n keyword list(AND)")
    public Set<String> nameI18nLikeList;

    @Schema(description = "name i18n keyword exclude list(OR)")
    public Set<String> nameI18nNotLikeList;

    @Schema(description = "description i18n keyword list(AND)")
    public Set<String> descriptionI18nLikeList;

    @Schema(description = "description i18n exclude keyword list(OR)")
    public Set<String> descriptionI18nNotLikeList;

    @Schema(description = "initial status id list")
    public Set<UUID> initialStatusIdList;

    @Schema(description = "initial status id exclude list")
    public Set<UUID> initialStatusIdExcludeList;

    @Schema(description = "user id list")
    public Set<UUID> createdByUserIdList;

    @Schema(description = "user id exclude list")
    public Set<UUID> createdByUserIdExcludeList;

    @Schema(description = "twinflow schema id list")
    public Set<UUID> twinflowSchemaIdList;

    @Schema(description = "twinflow schema id exclude list")
    public Set<UUID> twinflowSchemaIdExcludeList;
}
