package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorColorHexV1")
public class TwinClassFieldDescriptorColorHexDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "colorHexV1";
    @Override
    public String fieldType() {
        return KEY;
    }
}
