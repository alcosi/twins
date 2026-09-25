package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListOptionCreateDV1")
public class DataListOptionCreateDTOv1 {
    @NotNull
    @Schema(description = "data list id", example = DTOExamples.DATA_LIST_ID)
    public UUID dataListId;

    @NotNull
    @Schema(description = "option")
    public I18nSaveDTOv1 optionI18n;

    @NotNull
    @Schema(description = "boolean flag for custom field", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean custom;

    @Schema(description = "icon")
    public String icon;

    @Schema(description = "description")
    public I18nSaveDTOv1 descriptionI18n;

    @Schema(description = "attributes map")
    public Map<String, String> attributesMap;

    @Schema(description = "external id")
    public String externalId;

    @Schema(description = "background color hex", example = DTOExamples.COLOR_HEX)
    public String backgroundColor;

    @Schema(description = "font color hex", example = DTOExamples.COLOR_HEX)
    public String fontColor;
}
