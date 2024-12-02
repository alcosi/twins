package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "DataListOptionSearchV1")
public class DataListOptionSearchDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;
    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;
    @Schema(description = "data list id list")
    public Set<UUID> dataListIdList;
    @Schema(description = "data list id exclude list")
    public Set<UUID> dataListIdExcludeList;
    @Schema(description = "option like list")
    public Set<String> optionLikeList;
    @Schema(description = "option not like list")
    public Set<String> optionNotLikeList;
    @Schema(description = "option i18n like list")
    public Set<String> optionI18nLikeList;
    @Schema(description = "option i18n not like list")
    public Set<String> optionI18nNotLikeList;
    @Schema(description = "business account id list")
    public Set<UUID> businessAccountIdList;
    @Schema(description = "business account id exclude list")
    public Set<UUID> businessAccountIdExcludeList;
}
