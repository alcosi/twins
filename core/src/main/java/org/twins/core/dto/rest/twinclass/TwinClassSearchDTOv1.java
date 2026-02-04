package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.twinclass.OwnerType;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassSearchV1")
public class TwinClassSearchDTOv1 {
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

    @Schema(description = "Hierarchy search head childs")
    public HierarchySearchDTOv1 headHierarchyChildsForTwinClassSearch;

    @Schema(description = "Hierarchy search head parents")
    public HierarchySearchDTOv1 headHierarchyParentsForTwinClassSearch;

    @Schema(description = "Hierarchy search extands childs")
    public HierarchySearchDTOv1 extendsHierarchyChildsForTwinClassSearch;

    @Schema(description = "Hierarchy search extands parents")
    public HierarchySearchDTOv1 extendsHierarchyParentsForTwinClassSearch;

    @Schema(description = "owner type list")
    public Set<OwnerType> ownerTypeList;

    @Schema(description = "owner type exclude list")
    public Set<OwnerType> ownerTypeExcludeList;

    @Schema(description = "marker data list id list")
    public Set<UUID> markerDatalistIdList;

    @Schema(description = "marker data list id exclude list")
    public Set<UUID> markerDatalistIdExcludeList;

    @Schema(description = "tag data list id list")
    public Set<UUID> tagDatalistIdList;

    @Schema(description = "tag data list id exclude list")
    public Set<UUID> tagDatalistIdExcludeList;

    @Schema(description = "freeze id list")
    public Set<UUID> freezeIdList;

    @Schema(description = "freeze id exclude list")
    public Set<UUID> freezeIdExcludeList;

    @Schema(description = "twin class is abstract", example = DTOExamples.TERNARY)
    public Ternary abstractt;

    @Schema(description = "twin class is a segment", example = DTOExamples.TERNARY)
    public Ternary segment;

    @Schema(description = "twin class has segments", example = DTOExamples.TERNARY)
    public Ternary hasSegments;

    @Schema(description = "twin class has uniq name flag", example = DTOExamples.TERNARY)
    public Ternary uniqueName;

    @Schema(description = "twin class has twinflow schema space", example = DTOExamples.TERNARY)
    public Ternary twinflowSchemaSpace;

    @Schema(description = "twin class has schema space", example = DTOExamples.TERNARY)
    public Ternary twinClassSchemaSpace;

    @Schema(description = "twin class has permission schema space", example = DTOExamples.TERNARY)
    public Ternary permissionSchemaSpace;

    @Schema(description = "twin class has alias space", example = DTOExamples.TERNARY)
    public Ternary aliasSpace;

    @Schema(description = "twin class required assignee", example = DTOExamples.TERNARY)
    public Ternary assigneeRequired;

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

    @Schema(description = "external id like list")
    public Set<String> externalIdLikeList;

    @Schema(description = "external id not like list")
    public Set<String> externalIdNotLikeList;
}
