package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainViewPublicV1")
public class DomainViewPublicDTOv1 {
    @Schema(description = "domain id")
    private UUID id;

    @Schema(description = "key", example = DTOExamples.DOMAIN_KEY)
    public String key;

    @Schema(description = "domain description", example = DTOExamples.DOMAIN_KEY)
    public String description;

    @Schema(description = "Icon dark uri. Might be relative")
    public String iconDark;

    @Schema(description = "Icon light uri. Might be relative")
    public String iconLight;

    @Schema(description = "domain name")
    public String name;
}
