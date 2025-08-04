package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "DataListOptionV1")
public class DataListOptionDTOv1 {
    @Schema(description = "id", example = DTOExamples.DATA_LIST_OPTION_ID)
    public UUID id;

    @Schema(description = "name", example = "Bharat")
    public String name;

    @Schema(description = "icon", example = "Icon path")
    public String icon;

    @Schema(description = "status", example = DTOExamples.DATA_LIST_OPTION_STATUS)
    public DataListOptionEntity.Status status;

    @Schema(description = "map attributes (key : value)")
    public Map<String, String> attributes;

    @Schema(description = "background color", example = DTOExamples.COLOR_HEX)
    public String backgroundColor;

    @Schema(description = "font color", example = DTOExamples.COLOR_HEX)
    public String fontColor;

    @Schema(description = "external id [optional]")
    public String externalId;
}
