package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.enums.datalist.DataListStatus;

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

    @Schema(description = "description", example = "Bharat")
    public String description;

    @Schema(description = "icon", example = "Icon path")
    public String icon;

    @Schema(description = "status", example = DTOExamples.DATA_LIST_OPTION_STATUS)
    public DataListStatus status;

    @Schema(description = "map attributes (key : value)")
    public Map<String, String> attributes;

    @Schema(description = "background color", example = DTOExamples.COLOR_HEX)
    public String backgroundColor;

    @Schema(description = "font color", example = DTOExamples.COLOR_HEX)
    public String fontColor;

    @Schema(description = "external id [optional]")
    public String externalId;

    @Schema(description = "datalist id", example = DTOExamples.DATA_LIST_ID)
    @RelatedObject(type = DataListOptionDTOv1.class, name = "dataList")
    public UUID dataListId;

    @Schema(description = "business account id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;

    @Schema(description = "flag for custom field", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean custom;
}