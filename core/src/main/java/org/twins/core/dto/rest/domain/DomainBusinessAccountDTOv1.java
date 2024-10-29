package org.twins.core.dto.rest.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "DomainBusinessAccountV1")
public class DomainBusinessAccountDTOv1 extends BusinessAccountDTOv1 {

    @Schema(description = "domain business account id")
    public UUID domainBusinessAccountId;

    @Schema(description = "", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @Schema(description = "", example = DTOExamples.TWINFLOW_SCHEMA_ID)
    public UUID twinflowSchemaId;

    @Schema(description = "", example = DTOExamples.TWIN_CLASS_SCHEMA_ID)
    public UUID twinClassSchemaId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "domain business account created at", example = DTOExamples.INSTANT)
    public LocalDateTime domainBusinessAccountCreatedAt;

    @Schema(description = "locale", example = DTOExamples.LOCALE)
    public Locale currentLocale;

}
