package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name =  "UserCreateV1")
public class UserCreateDTOv1 extends UserSaveDTOv1 {
}
