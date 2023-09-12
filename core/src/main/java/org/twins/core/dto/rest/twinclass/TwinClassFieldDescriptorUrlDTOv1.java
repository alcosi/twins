package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorUrlV1")
public class TwinClassFieldDescriptorUrlDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "urlV1";
    public String fieldType = KEY;
}
