package org.twins.core.dto.rest.twinclass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "TwinClassListRsV1")
public class TwinClassListRsDTOv1 extends Response {
    @ApiModelProperty(notes = "results - transfers list")
    public List<TwinClassDTOv1> twinClassList;
}
