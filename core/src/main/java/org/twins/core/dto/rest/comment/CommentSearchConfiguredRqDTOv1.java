package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(name = "CommentSearchConfiguredRqV1")
public class CommentSearchConfiguredRqDTOv1 {
    @Schema(description = "Search named params values")
    public Map<String, String> params;

    @Schema(description = "search narrow")
    public CommentSearchDTO narrow;
}
