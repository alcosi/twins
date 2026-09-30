package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListUpdateV1")
public class DataListUpdateDTOv1 {

    @NotNull
    @Schema(description = "data list id", example = DTOExamples.DATA_LIST_ID)
    public UUID id;

    @Schema(description = "key", example = DTOExamples.DATA_LIST_KEY)
    public String key;

    @Schema(description = "name")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "description")
    public I18nSaveDTOv1 descriptionI18n;

    @Schema(description = "attribute1")
    public DataListAttributeSaveDTOv1 attribute1;

    @Schema(description = "attribute2")
    public DataListAttributeSaveDTOv1 attribute2;

    @Schema(description = "attribute3")
    public DataListAttributeSaveDTOv1 attribute3;

    @Schema(description = "attribute4")
    public DataListAttributeSaveDTOv1 attribute4;

    @Schema(description = "external id")
    public String externalId;

    @Schema(description = "default option id")
    public UUID defaultOptionId;
}
