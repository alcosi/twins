package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.domain.DomainType;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainAddRqV1")
public class DomainAddRqDTOv1 extends Request {
    @Schema(description = "will be used for url generation and for twins aliases", example = DTOExamples.DOMAIN_KEY)
    public String key;

    @Schema(description = "domain description", example = DTOExamples.DOMAIN_DESCRIPTION)
    public String description;

    @Schema(description = "type [basic/b2b]", example = DTOExamples.DOMAIN_TYPE)
    public DomainType type;

    @Schema(description = "default locale for domain [en/de/by]", example = DTOExamples.LOCALE)
    public String defaultLocale;
}
