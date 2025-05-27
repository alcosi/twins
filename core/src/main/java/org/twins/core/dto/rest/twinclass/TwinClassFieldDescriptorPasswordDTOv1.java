package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorPasswordV1")
public class TwinClassFieldDescriptorPasswordDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "passwordV1";
    public String fieldType = KEY;

    @Schema(description = "Some validation regexp", example = ".*")
    public String regExp;
}
