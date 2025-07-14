package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  TwinClassFieldDescriptorListLongDTOv1.KEY)
public class TwinClassFieldDescriptorListLongDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "TwinClassFieldDescriptorListLongV1";

    public TwinClassFieldDescriptorListLongDTOv1() {
        this.fieldType = KEY;
    }

    @Schema(description = "Field type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType;

    @Schema(description = "Can custom option be entered", example = "false")
    public Boolean supportCustom;

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "Data list id for grabbing valid options", example = "")
    public UUID dataListId;
}
