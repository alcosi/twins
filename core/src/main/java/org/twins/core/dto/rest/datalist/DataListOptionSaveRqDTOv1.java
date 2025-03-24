package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionSaveRqV1")
public class DataListOptionSaveRqDTOv1 extends Request {
    @Schema(description = "icon")
    public String icon;

    @Schema(description = "option")
    public I18nSaveDTOv1 optionI18n;

    @Schema(description = "attributes map")
    public Map<String, String> attributesMap;
}
