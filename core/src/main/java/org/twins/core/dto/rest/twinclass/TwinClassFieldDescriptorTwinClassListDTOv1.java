package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name = "TwinClassFieldDescriptorTwinClassListV1")
public class TwinClassFieldDescriptorTwinClassListDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "twinClassListV1";

    @Override
    public String fieldType() {
        return KEY;
    }
}
