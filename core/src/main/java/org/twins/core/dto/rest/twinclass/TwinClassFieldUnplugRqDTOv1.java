package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassFieldUnplugRqV1")
public class TwinClassFieldUnplugRqDTOv1 extends Request {

    @Schema(description = "fields to unplug")
    private List<TwinClassFieldPlugBaseDTOv1> fieldsToUnplug;
}
