package org.twins.core.dao.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class DomainVersionGhostId implements Serializable {
    private UUID domainId;
    private UUID userId;
    private String tableName;
}
