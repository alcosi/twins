package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TransitionAliasSearchRqV1")
public class TransitionAliasSearchRqDTOv1 extends Request {
    @Schema(description = "transition alias id")
    public Set<UUID> idList;
    @Schema(description = "transition alias id exclude")
    public Set<UUID> idExcludeList;
    @Schema(description = "alias like list")
    public Set<String> aliasLikeList;
    @Schema(description = "alias not like list")
    public Set<String> aliasNotLikeList;
}
