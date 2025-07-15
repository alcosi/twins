package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;

@Data
@Accessors(fluent = true)
@Schema(name = TwinClassFieldDescriptorBooleanDTOv1.KEY)
public class TwinClassFieldDescriptorBooleanDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "TwinClassFieldDescriptorBooleanV1";

    public TwinClassFieldDescriptorBooleanDTOv1() {
        this.fieldType = KEY;
    }

    @Schema(description = "Field type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType;

    @Schema(description = "Checkbox type", example = "TOGGLE")
    public TwinFieldBooleanEntity.CheckboxType checkboxType;
}
