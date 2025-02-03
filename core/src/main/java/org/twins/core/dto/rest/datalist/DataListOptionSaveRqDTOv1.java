package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.dto.I18nDTOv1;
import org.twins.core.dto.rest.Request;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionSaveRqV1")
public class DataListOptionSaveRqDTOv1 extends Request {
    @Schema(description = "icon")
    public String icon;

    @Schema(description = "option")
    public I18nDTOv1 optionI18n;

    @Schema(description = "attributes map")
    public Map<String, String> attributesMap;
}
