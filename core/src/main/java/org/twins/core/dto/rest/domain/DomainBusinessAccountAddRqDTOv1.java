package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainBusinessAccountAddV1")
public class DomainBusinessAccountAddRqDTOv1 extends Request {
    @Schema(description = "id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    public UUID businessAccountId;
}
