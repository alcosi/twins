package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainSearchV1")
public class DomainPublicSearchDTOv1 {
    @Schema(description = "key like list")
    public Set<String> keyLikeList;

    @Schema(description = "key not like list")
    public Set<String> keyNotLikeList;
}
