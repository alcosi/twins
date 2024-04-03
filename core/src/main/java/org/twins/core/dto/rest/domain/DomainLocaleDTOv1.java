package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Schema(name = "DomainLocaleV1")
@Accessors(chain = true)
public class DomainLocaleDTOv1 {
    @Schema(description = "id")
    public String id;

    @Schema(description = "name")
    public String name;

    @Schema(description = "nativeName")
    public String nativeName;

    @Schema(description = "icon")
    public String icon;
}
