package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorUserLongV1")
public class TwinClassFieldDescriptorUserLongDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "selectUserLongV1";
    @Override
    public String fieldType() {
        return KEY;
    }

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "User filter list id for grabbing valid users", example = "")
    @RelatedObject(type = TwinDTOv2.class, name = "userFilter")
    public UUID userFilterId;
}


