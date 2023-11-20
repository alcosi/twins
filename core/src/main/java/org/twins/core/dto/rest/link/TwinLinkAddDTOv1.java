package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinLinkAddV1")
public class TwinLinkAddDTOv1 extends TwinLinkBaseDTOv1 {
}
