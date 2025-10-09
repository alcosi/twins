package org.twins.core.dto.rest.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv2;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinclass.TwinClassSchemaDTOv1;
import org.twins.core.enums.domain.DomainType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainViewV1")
public class DomainViewDTOv1 extends DomainViewPublicDTOv1 {
    @Schema(description = "type [basic/b2b]", example = DTOExamples.DOMAIN_TYPE)
    public DomainType type;

    @Schema(description = "business account initiator featurer id", example = DTOExamples.FEATURER_ID)
    public Integer businessAccountInitiatorFeaturerId;

    @Schema(description = "business account initiator params", example = DTOExamples.FEATURER_PARAM)
    public HashMap<String, String> businessAccountInitiatorParams;

    @Schema(description = "user group manager featurer id", example = DTOExamples.FEATURER_ID)
    public Integer userGroupManagerFeaturerId;

    @Schema(description = "user group manager params", example = DTOExamples.FEATURER_PARAM)
    public HashMap<String, String> userGroupManagerParams;

    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    @RelatedObject(type = PermissionSchemaDTOv2.class, name = "permissionSchema")
    public UUID permissionSchemaId;

    @Schema(description = "twinflow schema id", example = DTOExamples.TWINFLOW_SCHEMA_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "twinflowSchema")
    public UUID twinflowSchemaId;

    @Schema(description = "twinclass schema id", example = DTOExamples.TWIN_CLASS_SCHEMA_ID)
    @RelatedObject(type = TwinClassSchemaDTOv1.class, name = "twinClassSchema")
    public UUID twinClassSchemaId;

    @Schema(description = "business account template twin id", example = DTOExamples.TWIN_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "businessAccountTemplateTwin")
    public UUID businessAccountTemplateTwinId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "default locale", example = DTOExamples.LOCALE)
    public String defaultLocale;

    @Schema(description = "ancestor twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "ancestorTwinClass")
    public UUID ancestorTwinClassId;

    @Schema(description = "default tier id", example = DTOExamples.TIER_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "defaultTier")
    public UUID defaultTierId;

    @Schema(description = "attachment storage used count")
    public Long attachmentStorageUsedCount;

    @Schema(description = "attachment storage used size")
    public Long attachmentStorageUsedSize;

    @Schema(description = "domain user template twin id", example = DTOExamples.TWIN_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "domainUserTemplateTwin")
    public UUID domainUserTemplateTwinId;

    @Schema(description = "Resource storage type", example = DTOExamples.RESOURCE_STORAGE_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "resourceStorage")
    public UUID resourceStorageId;

    @Schema(description = "Attachment storage type", example = DTOExamples.RESOURCE_STORAGE_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "attachmentStorage")
    public UUID attachmentStorageId;

    @Schema(description = "domain navigation bar pointer", example = DTOExamples.FACE_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "navbarFace")
    public UUID navbarFaceId;
}


