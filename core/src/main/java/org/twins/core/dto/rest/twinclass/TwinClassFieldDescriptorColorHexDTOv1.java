package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinFieldValueDTO;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorColorHexV1")
public class TwinClassFieldDescriptorColorHexDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "colorHexV1";
    public String fieldType = KEY;
}
