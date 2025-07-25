package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorI18nV1")
public class TwinClassFieldDescriptorI18nDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "i18nV1";
    @Override
    public String fieldType() {
        return KEY;
    }
}
