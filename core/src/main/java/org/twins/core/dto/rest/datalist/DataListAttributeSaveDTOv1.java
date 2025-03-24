package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

@Data
@Accessors(chain = true)
@Schema(name = "DataListAttributeSaveV1")
public class DataListAttributeSaveDTOv1 {
    @Schema(description = "key", example = DTOExamples.DATA_LIST_ATTRIBUTE_KEY)
    public String key;

    @Schema(description = "name", example = DTOExamples.NAME)
    public I18nSaveDTOv1 nameI18n;
}
