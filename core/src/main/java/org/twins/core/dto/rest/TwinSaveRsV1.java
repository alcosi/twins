package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.twins.core.dto.rest.twin.TwinDTOv2;

import java.util.Map;
import java.util.UUID;

@Schema
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
public class TwinSaveRsV1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "Invalid twin field id list")
    private Map<UUID, String> invalidTwinFieldErrors;

    @Schema(description = "twin")
    public TwinDTOv2 twin;

    public TwinSaveRsV1() {
        super();
    }
}
