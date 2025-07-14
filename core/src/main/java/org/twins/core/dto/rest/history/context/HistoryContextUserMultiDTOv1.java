package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  HistoryContextUserMultiDTOv1.KEY, oneOf = { HistoryContextDTO.class })
public class HistoryContextUserMultiDTOv1 implements HistoryContextDTO {

    public static final String KEY = "HistoryContextUserMultiV1";

    public HistoryContextUserMultiDTOv1() {
        this.contextType = KEY;
    }

    @Schema(description = "Context type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String contextType;

    @Schema(description = "From user id  set", example = DTOExamples.USER_ID)
    public Set<UUID> fromUserIdSet;

    @Schema(description = "From user set")
    public Set<UserDTOv1> fromUserSet;

    @Schema(description = "To user id set")
    public Set<UUID> toUserIdSet;

    @Schema(description = "To user set")
    public Set<UserDTOv1> toUserSet;
}
