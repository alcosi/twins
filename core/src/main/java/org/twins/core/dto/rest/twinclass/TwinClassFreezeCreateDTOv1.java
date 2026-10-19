package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassFreezeCreateV1")
public class TwinClassFreezeCreateDTOv1 {

    @NotBlank
    @Schema(description = "key")
    public String key;

    @NotNull
    @Schema(description = "statusId")
    public UUID statusId;

    @Schema(description = "name")
    public I18nSaveDTOv1 name;

    @Schema(description = "description")
    public I18nSaveDTOv1 description;
}
