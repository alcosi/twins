package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.LongRangeDTOv1;
import org.twins.core.dto.rest.Request;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Deprecated
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "TwinClassFieldSearchRqV1")
public class TwinClassFieldSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin class id map")
    public Map<UUID, Boolean> twinClassIdMap;

    @Schema(description = "twin class id exclude map")
    public Map<UUID, Boolean> twinClassIdExcludeMap;

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

    @Schema(description = "external id like list")
    public Set<String> externalIdLikeList;

    @Schema(description = "external id not like list")
    public Set<String> externalIdNotLikeList;

    @Schema(description = "field typer id list")
    public Set<Integer> fieldTyperIdList;

    @Schema(description = "field typer id exclude list")
    public Set<Integer> fieldTyperIdExcludeList;

    @Schema(description = "field initiator id list")
    public Set<Integer> fieldInitiatorIdList;

    @Schema(description = "field initiator id exclude list")
    public Set<Integer> fieldInitiatorIdExcludeList;

    @Schema(description = "view permission id list")
    public Set<UUID> viewPermissionIdList;

    @Schema(description = "view permission id exclude list")
    public Set<UUID> viewPermissionIdExcludeList;

    @Schema(description = "edit permission id list")
    public Set<UUID> editPermissionIdList;

    @Schema(description = "edit permission id exclude list")
    public Set<UUID> editPermissionIdExcludeList;

    @Schema(description = "required", example = DTOExamples.TERNARY)
    public Ternary required;

    @Schema(description = "order range")
    public LongRangeDTOv1 orderRange;

}
