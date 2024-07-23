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
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinClassListRqV1")
public class TwinClassSearchRqDTOv1 extends Request {
    @Schema(description = "twin class id list")
    public List<UUID> twinClassIdList;

    @Schema(description = "twin class key list")
    public List<String> twinClassKeyLikeList;

    @Schema(description = "name i18n keyword list")
    public List<String> nameI18nLikeList;

    @Schema(description = "description i18n keyword list")
    public List<String> descriptionI18nLikeList;

    @Schema(description = "head twin class id list")
    public List<UUID> headTwinClassIdList;

    @Schema(description = "extends twin class id list")
    public List<UUID> extendsTwinClassIdList;

    @Schema(description = "owner type", example = DTOExamples.TWIN_CLASS_OWNER_TYPE)
    public TwinClassEntity.OwnerType ownerType;

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
}
