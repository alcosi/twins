package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@Accessors(chain  = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainUpdateV1")
public class DomainUpdateDTOv1 extends DomainSaveDTOv1 {
}
