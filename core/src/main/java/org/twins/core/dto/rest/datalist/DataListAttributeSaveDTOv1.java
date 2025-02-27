package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.i18n.dto.I18nDTOv1;
import org.twins.core.dto.rest.DTOExamples;

@Data
@Accessors(chain = true)
@Schema(name = "DataListAttributeSaveV1")
public class DataListAttributeSaveDTOv1 {
    @Schema(description = "key", example = DTOExamples.DATA_LIST_ATTRIBUTE_KEY)
    public String key;

    @Schema(description = "name", example = DTOExamples.NAME)
    public I18nDTOv1 nameI18n;
}
