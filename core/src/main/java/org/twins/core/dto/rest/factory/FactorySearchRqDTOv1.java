package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactorySearchRqV1")
public class FactorySearchRqDTOv1 extends Request {
    @Schema(description = "id List")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "key like list")
    public Set<String> keyLikeList;

    @Schema(description = "key not like list")
    public Set<String> keyNotLikeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;
}
