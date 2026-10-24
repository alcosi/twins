package org.twins.core.dto.rest.domainversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.enums.domain.DomainOperationStatus;
import org.twins.core.enums.domain.DomainVersionAuditOperation;
import org.twins.core.enums.domain.DomainVersionedTable;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "DomainVersionAuditCountV1")
public class DomainVersionAuditCountDTOv1 extends CountDTOv1 {
    @Schema(description = "domain version id. Meaningful only when grouped by it; null otherwise", example = DTOExamples.UUID_ID)
    @RelatedObject(type = DomainVersionDTOv1.class, name = "domainVersion")
    public UUID domainVersionId;

    @Schema(description = "changed table. Meaningful only when grouped by it; null otherwise", example = "TWIN_CLASS")
    public DomainVersionedTable tableName;

    @Schema(description = "operation. Meaningful only when grouped by it; null otherwise", example = "UPDATE")
    public DomainVersionAuditOperation operation;

    @Schema(description = "domain operation status. Meaningful only when grouped by it; null otherwise", example = "IDLE")
    public DomainOperationStatus domainOperationStatus;

    @Schema(description = "actor user id. Meaningful only when grouped by it; null otherwise", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "actorUser")
    public UUID actorUserId;
}
