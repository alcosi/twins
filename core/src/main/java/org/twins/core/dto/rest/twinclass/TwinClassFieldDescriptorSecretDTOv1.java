package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorSecretV1")
public class TwinClassFieldDescriptorSecretDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "secretV1";

    public TwinClassFieldDescriptorSecretDTOv1() {
        this.fieldType = KEY;
    }

    @Schema(description = "Field type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType;

    @Schema(description = "Some validation regexp", example = ".*")
    public String regExp;
}
