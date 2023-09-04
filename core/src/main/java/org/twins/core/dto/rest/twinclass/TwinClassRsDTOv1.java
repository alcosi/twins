package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassListRsV1")
public class TwinClassRsDTOv1 extends Response {
    @Schema(description = "results - twin class")
    public TwinClassDTOv1 twinClass;
}
