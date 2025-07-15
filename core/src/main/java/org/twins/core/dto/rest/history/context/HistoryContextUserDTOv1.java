package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  HistoryContextUserDTOv1.KEY)
public class HistoryContextUserDTOv1 implements HistoryContextDTO {

    public static final String KEY = "HistoryContextUserV1";

    public HistoryContextUserDTOv1() {
        this.contextType = KEY;
    }

    @Schema(description = "Context type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String contextType;

    @Schema(description = "From user id", example = DTOExamples.USER_ID)
    public UUID fromUserId;

    @Schema(description = "From user")
    public UserDTOv1 fromUser;

    @Schema(description = "To user id")
    public UUID toUserId;

    @Schema(description = "To user")
    public UserDTOv1 toUser;
}
