package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.i18n.dto.I18nDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionSaveRqV1")
public class PermissionSaveRqDTOv1 extends Request {
    @Schema(description = "[optional] name", example = DTOExamples.NAME)
    public I18nDTOv1 nameI18n;

    @Schema(description = "[optional] description", example = DTOExamples.DESCRIPTION)
    public I18nDTOv1 descriptionI18n;

    @Schema(description = "key", example = DTOExamples.PERMISSION_KEY)
    public String key;

    @Schema(description = "group id", example = DTOExamples.PERMISSION_GROUP_ID)
    public UUID groupId;
}
