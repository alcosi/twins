package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.i18n.dto.I18nDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListSaveRqV1")
public class DataListSaveRqDTOv1 extends Request {
    @Schema(description = "key", example = DTOExamples.DATA_LIST_KEY)
    public String key;

    @Schema(description = "name", example = DTOExamples.NAME)
    public I18nDTOv1 nameI18n;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public I18nDTOv1 descriptionI18n;

    @Schema(description = "attribute1")
    public DataListAttributeSaveDTOv1 attribute1;

    @Schema(description = "attribute2")
    public DataListAttributeSaveDTOv1 attribute2;

    @Schema(description = "attribute3")
    public DataListAttributeSaveDTOv1 attribute3;

    @Schema(description = "attribute4")
    public DataListAttributeSaveDTOv1 attribute4;
}
