package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorListSharedInHeadV1")
public class TwinClassFieldDescriptorListSharedInHeadDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "selectSharedInHeadV1";

    public TwinClassFieldDescriptorListSharedInHeadDTOv1() {
        this.fieldType = KEY;
    }

    @Schema(description = "Field type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType;

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;
}
