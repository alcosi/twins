package org.twins.core.dto.rest.tier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TierUpdateRqV1")
public class TierUpdateRqDTOv1 extends TierSaveRqDTOv1 {
    @JsonIgnore
    public UUID id;
}