package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.i18n.dto.I18nSaveDTOv1;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinStatusV1")
public class TwinStatusSaveDTOv1 {
    @Schema(description = "uuid", example = DTOExamples.TWIN_STATUS_ID)
    public UUID id;

    @Schema(name = "key within the domain")
    private String key;

    @Schema(description = "translation of names")
    public I18nSaveDTOv1 translationName;

    @Schema(description = "translation of descriptions")
    public I18nSaveDTOv1 translationDescription;

    @Schema(description = "url for status UI logo", example = "https://twins.org/img/twin_status_default.png")
    public String logo;

    @Schema(description = "color hex", example = "#ff00ff")
    public String color;

    @JsonIgnore
    public UUID twinClassId;
}
