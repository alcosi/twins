package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  TwinClassFieldDescriptorI18nDTOv1.KEY)
public class TwinClassFieldDescriptorI18nDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "TwinClassFieldDescriptorI18nV1";

    public TwinClassFieldDescriptorI18nDTOv1() {
        this.fieldType = KEY;
    }

    @Schema(description = "Field type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType;
}
