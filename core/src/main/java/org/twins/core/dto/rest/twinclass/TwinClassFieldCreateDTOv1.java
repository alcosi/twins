package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassFieldCreateV1")
public class TwinClassFieldCreateDTOv1 extends TwinClassFieldSaveDTOv1 {
    @Schema(description = "twin class id")
    public UUID twinClassId;
}
