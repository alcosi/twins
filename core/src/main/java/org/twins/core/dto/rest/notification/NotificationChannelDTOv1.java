package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "NotificationChannelV1")
public class NotificationChannelDTOv1 {
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "notifier featurer id")
    @RelatedObject(type = FeaturerDTOv1.class, name = "notifierFeaturer")
    public Integer notifierFeaturerId;

    @Schema(description = "notifier params")
    public Map<String, String> notifierParams;
}
