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
@Schema(name = "DomainViewV1")
public class DomainViewDTOv1 extends DomainViewPublicDTOv1 {
    @Schema(description = "type [basic/b2b]")
    public DomainType type;

    @Schema(description = "permission schema id")
    public UUID permissionSchemaId;

    @Schema(description = "twinflow schema id")
    public UUID twinflowSchemaId;

    @Schema(description = "twinclass schema id")
    public UUID twinClassSchemaId;

    @Schema(description = "business account template twin id")
    public UUID businessAccountTemplateTwinId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "default locale")
    public String defaultLocale;

    @Schema(description = "domain navigation bar pointer")
    public UUID navbarFaceId;
}
