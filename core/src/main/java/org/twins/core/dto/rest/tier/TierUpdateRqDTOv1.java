package org.twins.core.dto.rest.tier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TierUpdateRqV1")
public class TierUpdateRqDTOv1 extends Request {
    @Schema(description = "tier update")
    public TierUpdateDTOv1 tier;
}