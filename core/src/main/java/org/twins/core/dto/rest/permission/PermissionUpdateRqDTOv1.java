package org.twins.core.dto.rest.permission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "PermissionUpdateRqV1")
public class PermissionUpdateRqDTOv1 extends Request {
    @Schema(description = "[optional] name", example = DTOExamples.NAME)
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "[optional] description", example = DTOExamples.DESCRIPTION)
    public I18nSaveDTOv1 descriptionI18n;

    @Schema(description = "key", example = DTOExamples.PERMISSION_KEY)
    public String key;

    @Schema(description = "group id", example = DTOExamples.PERMISSION_GROUP_ID)
    public UUID groupId;

    @JsonIgnore
    public UUID id;
}
