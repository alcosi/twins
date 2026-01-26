package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionSaveV1")
public class DataListOptionSaveDTOv1 {
    @Schema(description = "icon")
    public String icon;

    @Schema(description = "option")
    public I18nSaveDTOv1 optionI18n;

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

    @Schema(description = "boolean flag for custom field", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean custom;
}
