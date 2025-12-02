package org.twins.core.dto.rest.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainUserV1")
public class DomainUserDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_ID)
    public UUID id;

    @Schema(description = "user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "user")
    public UUID userId;

    @Schema(description = "locale", example = DTOExamples.LOCALE)
    public Locale currentLocale;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "Business account id list."  + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> businessAccountUserIdList;

    //todo think over move to related objects
    @Schema(description = "Business account users." + DTOExamples.LAZY_RELATION_MODE_ON)
    public List<BusinessAccountUserDTOv1> businessAccountUsers;
}
