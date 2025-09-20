package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassFieldPlugV1")
public class TwinClassFieldPlugDTOv1 extends TwinClassFieldPlugBaseDTOv1 {

    @Schema(description = "twin class dto")
    private TwinClassDTOv1 twinClass;

    @Schema(description = "twin class field dto")
    private TwinClassFieldDTOv1 twinClassField;
}
