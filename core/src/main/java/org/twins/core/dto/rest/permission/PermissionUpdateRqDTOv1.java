package org.twins.core.dto.rest.permission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionUpdateRqV1")
public class PermissionUpdateRqDTOv1 extends PermissionSaveRqDTOv1 {
    @JsonIgnore
    public UUID id;
}
