package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.domain.ApiUser;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassService {
    final TwinClassRepository twinClassRepository;

    public List<TwinClassEntity> findTwinClasses(ApiUser apiUser, List<UUID> uuidLists) {
        if (CollectionUtils.isNotEmpty(uuidLists))
            return twinClassRepository.findByDomainIdAndIdIn(apiUser.domainId(), uuidLists);
        else
            return twinClassRepository.findByDomainId(apiUser.domainId());
    }

    public void checkTwinClassPermission(ApiUser apiUser, UUID twinclassId) {

    }
}

