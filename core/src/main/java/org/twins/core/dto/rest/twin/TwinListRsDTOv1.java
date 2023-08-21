package org.twins.core.dto.rest.twin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.PaginationBean;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "TwinListRsV4")
public class TwinListRsDTOv1 extends Response {
    @ApiModelProperty(notes = "pagination data")
    public PaginationBean pagination;

    @ApiModelProperty(notes = "results - transfers list")
    public List<TwinDTOv1> twinList;
}
