package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorLinkV1")
public class TwinClassFieldDescriptorLinkDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "selectLinkV1";
    @Override
    public String fieldType() {
        return KEY;
    }

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "Valid twins", example = "")
    public Set<UUID> dstTwinIds = new HashSet<>();
}
