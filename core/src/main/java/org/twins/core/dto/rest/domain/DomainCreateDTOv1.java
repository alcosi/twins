package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.domain.DomainType;
import org.twins.core.dto.rest.DTOExamples;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainCreateV1")
public class DomainCreateDTOv1 extends DomainSaveDTOv1 {
    @Schema(description = "will be used for url generation and for twins aliases", example = DTOExamples.DOMAIN_KEY)
    public String key;

    @Schema(description = "type [basic/b2b]", example = DTOExamples.DOMAIN_TYPE)
    public DomainType type;
}
