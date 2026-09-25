package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListSubsetCreateV1")
public class DataListSubsetCreateDTOv1 {
    @NotNull
    @Schema(description = "Data list id. Immutable after creation", example = DTOExamples.DATA_LIST_ID)
    public UUID dataListId;

    @NotBlank
    @Schema(description = "Data list subset key. Unique within the data list")
    public String key;

    @Schema(description = "Name translations")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "Description translations")
    public I18nSaveDTOv1 descriptionI18n;
}
