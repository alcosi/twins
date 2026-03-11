package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "SpaceRoleSaveV1")
public class SpaceRoleSaveDTOv1 {
    @Schema(description = "key", example = "Member")
    public String key;

    @Schema(description = "nameI18n")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "descriptionI18n")
    public I18nSaveDTOv1 descriptionI18n;

    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "twinClass")
    public UUID twinClassId;

    @Schema(description = "business account id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;
}
