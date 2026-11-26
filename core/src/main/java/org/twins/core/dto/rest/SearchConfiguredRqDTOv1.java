package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SearchConfiguredRqV1")
public abstract class SearchConfiguredRqDTOv1 extends Request {
    @Schema(description = "Search named params values")
    public Map<String, String> params;
}
