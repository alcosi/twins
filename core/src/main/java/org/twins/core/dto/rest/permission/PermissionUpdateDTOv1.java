package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "PermissionUpdateV1")
public class PermissionUpdateDTOv1 {
    @NotNull
    @Schema(description = "permission id", example = DTOExamples.PERMISSION_ID)
    public UUID id;

    @Schema(description = "[optional] name", example = DTOExamples.NAME)
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "[optional] description", example = DTOExamples.DESCRIPTION)
    public I18nSaveDTOv1 descriptionI18n;

    @Schema(description = "key", example = DTOExamples.PERMISSION_KEY)
    public String key;

    @Schema(description = "group id", example = DTOExamples.PERMISSION_GROUP_ID)
    public UUID groupId;
}
