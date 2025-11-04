package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.domain.SubscriptionEventType;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainSubscriptionEventBaseV1")
public class DomainSubscriptionEventBaseDTOv1 {

  @Schema(description = "id", example = DTOExamples.DOMAIN_ID)
  public UUID id;

  @Schema(description = "domain id", example = DTOExamples.DOMAIN_ID)
  public UUID domainId;

  @Schema(description = "subscription event type", example = "TWIN_UPDATED")
  public SubscriptionEventType subscriptionEventTypeId;

  @Schema(description = "dispatcher featurer id", example = DTOExamples.FEATURER_ID)
  public Integer dispatcherFeaturerId;

  @Schema(description = "dispatcher featurer params", example = DTOExamples.FEATURER_PARAM)
  public Map<String, String> dispatcherFeaturerParams;

  @Schema(name = "dispatcher featurer")
  public FeaturerDTOv1 dispatcherFeaturer;
}
