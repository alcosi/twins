package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNoRelationsProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.service.auth.AuthService;

import java.util.List;

import static org.twins.core.dao.specifications.twin.TwinFieldSimpleSpecification.checkDomainId;
import static org.twins.core.dao.specifications.twin.TwinFieldSimpleSpecification.checkTwin;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinFieldSimpleSearchService {

    private final AuthService authService;
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;

    public List<TwinFieldSimpleNoRelationsProjection> findTwinFieldSimpleValuesKit(TwinSearch twinSearch) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Specification<TwinFieldSimpleEntity> spec = Specification.where(
                checkDomainId(apiUser.getDomainId())
                        .and(checkTwin(twinSearch, apiUser.getUserId()))
        );
        return twinFieldSimpleRepository.loadValues(spec);
    }
}
