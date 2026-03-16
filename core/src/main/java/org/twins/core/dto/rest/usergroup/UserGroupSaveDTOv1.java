package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UserGroupSaveV1")
public class UserGroupSaveDTOv1 {
    //@Schema(description = "user group id", example = DTOExamples.USER_GROUP_ID)
    public I18nSaveDTOv1 nameI18n;

    public I18nSaveDTOv1 descriptionI18n;

    public UUID businessAccountId;


}
