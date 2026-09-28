package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.i18n.I18nHasTranslation;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "PermissionCreateRqV1")
public class PermissionCreateRqDTOv1 extends Request {
    @NotNull
    @I18nHasTranslation
    @Schema(description = "name", example = DTOExamples.NAME)
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "[optional] description", example = DTOExamples.DESCRIPTION)
    public I18nSaveDTOv1 descriptionI18n;

    @NotBlank
    @Schema(description = "key", example = DTOExamples.PERMISSION_KEY)
    public String key;

    @NotNull
    @Schema(description = "group id", example = DTOExamples.PERMISSION_GROUP_ID)
    public UUID groupId;
}
