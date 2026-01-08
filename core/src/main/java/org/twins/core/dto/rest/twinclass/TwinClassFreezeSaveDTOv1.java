package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassFreezeSaveV1")
public class TwinClassFreezeSaveDTOv1 {

    @Schema(description = "key")
    public String key;

    @Schema(description = "statusId")
    public UUID statusId;

    @Schema(description = "name")
    public I18nSaveDTOv1 name;

    @Schema(description = "description")
    public I18nSaveDTOv1 description;
}
