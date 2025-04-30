package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.datalist.DataListOptionEntity;
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

    @Schema(description = "data list key list")
    public Set<String> dataListKeyList;

    @Schema(description = "data list key exclude list")
    public Set<String> dataListKeyExcludeList;

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

    @Schema(description = "data list class subset id list")
    public Set<UUID> dataListSubsetIdList;

    @Schema(description = "data list class subset id exclude list")
    public Set<UUID> dataListSubsetIdExcludeList;

    @Schema(description = "data list class subset key list")
    public Set<String> dataListSubsetKeyList;

    @Schema(description = "data list class subset key exclude list")
    public Set<String> dataListSubsetKeyExcludeList;

    @Schema(description = "data list option status id list")
    public Set<DataListOptionEntity.Status> statusIdList;

    @Schema(description = "data list option status id exclude list")
    public Set<DataListOptionEntity.Status> statusIdExcludeList;

    @Schema(description = "external id like list")
    public Set<String> externalIdLikeList;

    @Schema(description = "external id not like list")
    public Set<String> externalIdNotLikeList;
}
