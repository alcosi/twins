package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.domain.ApiUser;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldService {
    final TwinClassFieldRepository twinClassFieldRepository;

    public List<TwinClassFieldEntity> findTwinClassFields(ApiUser apiUser, UUID twinClassId) {
        return twinClassFieldRepository.findByTwinClassId(twinClassId);
    }
}
