package org.twins.core.dto.rest.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.domain.DomainType;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainViewRsv1")
public class DomainViewDTOv1 {
    @Schema(description = "domain id")
    private UUID id;

    @Schema(description = "key", example = DTOExamples.DOMAIN_KEY)
    public String key;

    @Schema(description = "domain description", example = DTOExamples.DOMAIN_KEY)
    public String description;

    @Schema(description = "type [basic/b2b]")
    public DomainType type;

    @Schema(description = "permission schema id")
    private UUID permissionSchemaId;

    @Schema(description = "twinflow schema id")
    private UUID twinflowSchemaId;

    @Schema(description = "twinclass schema id")
    private UUID twinClassSchemaId;

    @Schema(description = "business account template twin id")
    private UUID businessAccountTemplateTwinId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "default locale")
    public String defaultLocale;
    @Schema(description = "Icon dark uri. Might be relative")
    public String iconDark;
    @Schema(description = "Icon light uri. Might be relative")
    public String iconLight;

}
