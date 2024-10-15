package org.twins.core.dto.rest.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainUserV2")
public class DomainUserDTOv2 extends DomainUserDTOv1 {
    @Schema(description = "user")
    public UserDTOv1 user;

    @Schema(description = "Business account users." + DTOExamples.LAZY_RELATION_MODE_ON)
    public List<BusinessAccountUserDTOv2> businessAccountUsers;
}
