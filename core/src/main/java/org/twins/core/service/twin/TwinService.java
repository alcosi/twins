package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TQL;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinService {
    final TwinRepository twinRepository;

    public List<TwinEntity> findTwins(ApiUser apiUser, TQL tql) {
        return twinRepository.findByBusinessAccountId(apiUser.businessAccountId());
    }

    public TwinEntity findTwin(ApiUser apiUser, UUID twinId) {
        return twinRepository.findById(twinId).get();
    }
}
