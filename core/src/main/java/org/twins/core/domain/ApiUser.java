package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
public class ApiUser {
    private UUID domainId;
    private UUID userId;
    private UUID businessAccountId;
    private Channel channel;
}
