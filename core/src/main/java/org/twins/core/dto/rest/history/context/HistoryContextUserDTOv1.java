package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "HistoryContextUserV1")
public class HistoryContextUserDTOv1 implements HistoryContextDTO {
    public static final String KEY = "userV1";
    public String contextType = KEY;

    @Schema(description = "From user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "fromUser")
    public UUID fromUserId;

    @Schema(description = "To user id")
    @RelatedObject(type = UserDTOv1.class, name = "toUser")
    public UUID toUserId;
}


