package org.twins.core.dto.rest.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientV1")
public class HistoryNotificationRecipientDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "createdByUserId", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at")
    public LocalDateTime createdAt;
}
