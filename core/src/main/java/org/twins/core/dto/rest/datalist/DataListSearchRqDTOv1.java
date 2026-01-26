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
@Schema(name =  "DataListSearchRqV1")
public class DataListSearchRqDTOv1 extends Request {
    @Schema(description = "datalist id list")
    public Set<UUID> idList;

    @Schema(description = "datalist id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "datalist name like list")
    public Set<String> nameLikeList;

    @Schema(description = "datalist name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "datalist description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "datalist description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "datalist class key like list")
    public Set<String> keyLikeList;

    @Schema(description = "datalist class key not like list")
    public Set<String> keyNotLikeList;

    @Schema(description = "data list option search")
    public DataListOptionSearchDTOv1 optionSearch;

    @Schema(description = "external id like list")
    public Set<String> externalIdLikeList;

    @Schema(description = "external id not like list")
    public Set<String> externalIdNotLikeList;

    @Schema(description = "default option id list")
    public Set<UUID> defaultOptionIdList;

    @Schema(description = "default option id exclude list")
    public Set<UUID> defaultOptionIdExcludeList;
}
