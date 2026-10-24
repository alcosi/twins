package org.twins.core.dto.rest.domainversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.enums.domain.DomainOperationStatus;
import org.twins.core.enums.domain.DomainVersionAuditOperation;
import org.twins.core.enums.domain.DomainVersionedTable;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainVersionAuditV1")
public class DomainVersionAuditDTOv1 {
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "version window this change is stamped with", example = DTOExamples.UUID_ID)
    @RelatedObject(type = DomainVersionDTOv1.class, name = "domainVersion")
    public UUID domainVersionId;

    @Schema(description = "strict insertion order (bigserial)", example = "123456")
    public Long seq;

    @Schema(description = "changed table", example = "TWIN_CLASS")
    public DomainVersionedTable tableName;

    @Schema(description = "PK of the changed row", example = DTOExamples.UUID_ID)
    public UUID rowId;

    @Schema(description = "change operation", example = "UPDATE")
    public DomainVersionAuditOperation operation;

    @Schema(description = "domain operation state at the moment of the change", example = "IDLE")
    public DomainOperationStatus domainOperationStatus;

    @Schema(description = "the row's version stamp BEFORE the operation; null for INSERT", example = DTOExamples.UUID_ID)
    public UUID beforeDomainVersionId;

    @Schema(description = "row image before the change (cleaned of versioning columns); null for INSERT (DETAILED mode only)")
    public Map<String, Object> before;

    @Schema(description = "row image after the change (cleaned of versioning columns); null for DELETE (DETAILED mode only)")
    public Map<String, Object> after;

    @Schema(description = "change timestamp", example = DTOExamples.INSTANT)
    public Timestamp changedAt;

    @Schema(description = "actor user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "actorUser")
    public UUID actorUserId;
}
