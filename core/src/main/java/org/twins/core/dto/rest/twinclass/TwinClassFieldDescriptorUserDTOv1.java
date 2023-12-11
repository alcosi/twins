package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinBaseDTOv2;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorUserV1")
public class TwinClassFieldDescriptorUserDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "selectUserV1";
    public String fieldType = KEY;

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "Valid users", example = "")
    public List<UserDTOv1> users = new ArrayList<>();

    public TwinClassFieldDescriptorUserDTOv1 add(UserDTOv1 user) {
        users.add(user);
        return this;
    }
}
