package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "TwinClassListRqV1")
public class TwinClassSearchRqDTOv1 extends Request {
    @Schema(description = "twin class id list")
    public List<UUID> twinClassIdList;

    @Schema(description = "twin class id exclude list")
    public List<UUID> twinClassIdExcludeList;

    @Schema(description = "twin class key list")
    public List<String> twinClassKeyLikeList;

    @Schema(description = "name i18n keyword list(AND)")
    public List<String> nameI18nLikeList;

    @Schema(description = "name i18n keyword exclude list(OR)")
    public List<String> nameI18nNotLikeList;

    @Schema(description = "description i18n keyword list(AND)")
    public List<String> descriptionI18nLikeList;

    @Schema(description = "description i18n exclude keyword list(OR)")
    public List<String> descriptionI18nNotLikeList;

    @Schema(description = "head twin class id list")
    public List<UUID> headTwinClassIdList;

    @Schema(description = "head twin class id exclude list")
    public List<UUID> headTwinClassIdExcludeList;

    @Schema(description = "extends twin class id list")
    public List<UUID> extendsTwinClassIdList;

    @Schema(description = "extends twin class id exclude list")
    public List<UUID> extendsTwinClassIdExcludeList;

    @Schema(description = "owner type list")
    public List<TwinClassEntity.OwnerType> ownerTypeList;

    @Schema(description = "owner type exclude list")
    public List<TwinClassEntity.OwnerType> ownerTypeExcludeList;

    @Schema(description = "marker data list id list")
    public Set<UUID> markerDatalistIdList;

    @Schema(description = "marker data list id exclude list")
    public Set<UUID> markerDatalistIdExcludeList;

    @Schema(description = "tag data list id list")
    public Set<UUID> tagDatalistIdList;

    @Schema(description = "tag data list id exclude list")
    public Set<UUID> tagDatalistIdExcludeList;

    @Schema(description = "twin class is abstract", example = DTOExamples.TERNARY)
    public Ternary abstractt;

    @Schema(description = "twin class has twinflow schema space", example = DTOExamples.TERNARY)
    public Ternary twinflowSchemaSpace;

    @Schema(description = "twin class has schema space", example = DTOExamples.TERNARY)
    public Ternary twinClassSchemaSpace;

    @Schema(description = "twin class has permission schema space", example = DTOExamples.TERNARY)
    public Ternary permissionSchemaSpace;

    @Schema(description = "twin class has alias space", example = DTOExamples.TERNARY)
    public Ternary aliasSpace;

    @Schema(description = "permission id list")
    public List<UUID> viewPermissionIdList;

    @Schema(description = "permission id exclude list")
    public List<UUID> viewPermissionIdExcludeList;
}
