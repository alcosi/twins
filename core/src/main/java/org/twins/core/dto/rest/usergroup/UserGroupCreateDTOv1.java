package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;
import org.twins.core.enums.user.UserGroupType;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UserGroupCreateV1")
public class UserGroupCreateDTOv1 {
    @NotNull
    @Schema(description = "user group type")
    public UserGroupType userGroupTypeId;

    @Schema(description = "Translation for name", example = DTOExamples.TRANSLATION)
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "Translation for description", example = DTOExamples.TRANSLATION)
    public I18nSaveDTOv1 descriptionI18n;

    @Schema(description = "business account id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    public UUID businessAccountId;
}
