package org.twins.core.dto.rest.domainversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainVersionAuditListRsV1")
public class DomainVersionAuditListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - domain version audit list")
    public List<DomainVersionAuditDTOv1> domainVersionAudits;
}
