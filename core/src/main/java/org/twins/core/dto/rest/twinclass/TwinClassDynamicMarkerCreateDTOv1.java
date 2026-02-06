package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassDynamicMarkerCreateV1")
public class TwinClassDynamicMarkerCreateDTOv1 extends TwinClassDynamicMarkerSaveDTOv1 {
}
