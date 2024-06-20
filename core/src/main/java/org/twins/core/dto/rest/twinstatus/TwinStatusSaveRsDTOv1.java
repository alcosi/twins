package org.twins.core.dto.rest.twinstatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinStatusSaveRsV1")
public class TwinStatusSaveRsDTOv1 extends Response {
    @Schema(description = "twin status")
    public TwinStatusDTOv1 twinStatus;
}
