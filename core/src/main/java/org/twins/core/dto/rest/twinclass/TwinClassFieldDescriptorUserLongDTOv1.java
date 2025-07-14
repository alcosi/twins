package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorUserLongV1")
public class TwinClassFieldDescriptorUserLongDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "selectUserLongV1";

    public TwinClassFieldDescriptorUserLongDTOv1() {
        this.fieldType = KEY;
    }

    @Schema(description = "Field type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType;

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "User filter list id for grabbing valid users", example = "")
    public UUID userFilterId;
}
