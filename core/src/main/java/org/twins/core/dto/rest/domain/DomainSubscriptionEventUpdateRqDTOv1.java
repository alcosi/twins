package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainSubscriptionEventUpdateRqV1")
public class DomainSubscriptionEventUpdateRqDTOv1 extends DomainSubscriptionEventSaveRqDTOv1 {
}

