package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainSubscriptionEventSearchRqV1")
public class DomainSubscriptionEventSearchRqDTOv1 extends Request {

    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "domain id list")
    public Set<UUID> domainIdList;

    @Schema(description = "domain id exclude list")
    public Set<UUID> domainIdExcludeList;

    @Schema(description = "subscription event type list")
    public Set<String> subscriptionEventTypeList;

    @Schema(description = "subscription event type exclude list")
    public Set<String> subscriptionEventTypeExcludeList;

    @Schema(description = "dispatcher featurer id list")
    public Set<Integer> dispatcherFeaturerIdList;

    @Schema(description = "dispatcher featurer id exclude list")
    public Set<Integer> dispatcherFeaturerIdExcludeList;
}

