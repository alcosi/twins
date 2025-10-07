package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.twinclass.OwnerType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassAvailabilityV1")
public class TwinClassAvailabilityDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_CLASS_AVAILABILITY_ID)
    public UUID id;

    @Schema(description = "key")
    public String key;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;
}
