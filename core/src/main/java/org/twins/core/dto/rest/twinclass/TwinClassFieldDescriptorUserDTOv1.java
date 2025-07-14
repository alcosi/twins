package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  TwinClassFieldDescriptorUserDTOv1.KEY)
public class TwinClassFieldDescriptorUserDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "TwinClassFieldDescriptorUserV1";

    public TwinClassFieldDescriptorUserDTOv1() {
        this.fieldType = KEY;
    }

    @Schema(description = "Field type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType;

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "Valid users", example = "")
    public List<UserDTOv1> users = new ArrayList<>();

    @Schema(description = "Valid users id list", example = "")
    public List<UUID> userIdList = new ArrayList<>();
}
