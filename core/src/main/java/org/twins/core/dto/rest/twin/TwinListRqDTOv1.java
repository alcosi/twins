package org.twins.core.dto.rest.twin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.tql.TqlDTOv1;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "TwinListRqV1")
public class TwinListRqDTOv1 extends Request {
    @ApiModelProperty(notes = "TQL")
    private TqlDTOv1 tql;
}
