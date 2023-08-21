package org.twins.core.dto.rest.twinclass;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "TwinClassListRqV1")
public class TwinClassListRqDTOv1 extends Request {
}
