package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorLinkLongV1")
public class TwinClassFieldDescriptorLinkLongDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "selectLinkLongV1";
    @Override
    public String fieldType() {
        return KEY;
    }

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "Link id for grabbing valid dst twins", example = "")
    public UUID linkId;
}
