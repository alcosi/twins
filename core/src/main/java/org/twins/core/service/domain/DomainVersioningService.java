package org.twins.core.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.*;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DomainVersioningService {
    private final DomainVersionRepository domainVersionRepository;
    private final DomainVersionGhostRepository domainVersionGhostRepository;
    private final DomainRepository domainRepository;

    public DomainVersionEntity getCurrentVersion(UUID domainId) {
        return domainRepository.findById(domainId)
                .map(DomainEntity::getCurrentDomainVersion)
                .orElse(null);
    }

    public boolean isGhostMode(UUID domainId, UUID userId, String tableName) {
        DomainVersionGhostId id = new DomainVersionGhostId();
        id.setDomainId(domainId);
        id.setUserId(userId);
        id.setTableName(tableName);
        return domainVersionGhostRepository.existsById(id);
    }
}
