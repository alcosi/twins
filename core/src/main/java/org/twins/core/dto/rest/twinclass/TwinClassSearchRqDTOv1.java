package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "TwinClassListRqV1")
public class TwinClassSearchRqDTOv1 extends Request {
    @Schema(description = "twin class id list")
    public Set<UUID> twinClassIdList;

    @Schema(description = "twin class id exclude list")
    public Set<UUID> twinClassIdExcludeList;

    @Schema(description = "twin class key list")
    public Set<String> twinClassKeyLikeList;

    @Schema(description = "name i18n keyword list(AND)")
    public Set<String> nameI18nLikeList;

    @Schema(description = "name i18n keyword exclude list(OR)")
    public Set<String> nameI18nNotLikeList;

    @Schema(description = "description i18n keyword list(AND)")
    public Set<String> descriptionI18nLikeList;

    @Schema(description = "description i18n exclude keyword list(OR)")
    public Set<String> descriptionI18nNotLikeList;

    @Schema(description = "head twin class id list")
    public Set<UUID> headTwinClassIdList;

    @Schema(description = "head twin class id exclude list")
    public Set<UUID> headTwinClassIdExcludeList;

    @Schema(description = "extends twin class id list")
    public Set<UUID> extendsTwinClassIdList;

    @Schema(description = "extends twin class id exclude list")
    public Set<UUID> extendsTwinClassIdExcludeList;

    @Schema(description = "owner type list")
    public Set<TwinClassEntity.OwnerType> ownerTypeList;

    @Schema(description = "owner type exclude list")
    public Set<TwinClassEntity.OwnerType> ownerTypeExcludeList;

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
    public Set<UUID> viewPermissionIdList;

    @Schema(description = "permission id exclude list")
    public Set<UUID> viewPermissionIdExcludeList;

    @Schema(description = "create permission id list")
    public Set<UUID> createPermissionIdList;

    @Schema(description = "create permission id exclude list")
    public Set<UUID> createPermissionIdExcludeList;

    @Schema(description = "edit permission id list")
    public Set<UUID> editPermissionIdList;

    @Schema(description = "edit permission id exclude list")
    public Set<UUID> editPermissionIdExcludeList;

    @Schema(description = "delete permission id list")
    public Set<UUID> deletePermissionIdList;

    @Schema(description = "delete permission id exclude list")
    public Set<UUID> deletePermissionIdExcludeList;
}
