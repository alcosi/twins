package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(name = "UserSearchConfiguredV1")
public class UserSearchConfiguredDTOv1 {
    @Schema(description = "params")
    public Map<String, String> params;

    @Schema(description = "narrow search")
    public UserSearchDTOv1 narrow;
}
