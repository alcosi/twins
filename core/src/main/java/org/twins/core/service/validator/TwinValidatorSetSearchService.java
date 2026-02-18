package org.twins.core.service.validator;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.Ternary;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dao.validator.TwinValidatorSetRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinValidatorSetSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinValidatorSetSearchService {
    private final AuthService authService;
    private final TwinValidatorSetRepository twinValidatorSetRepository;

    public PaginationResult<TwinValidatorSetEntity> findTwinValidatorSetsForDomain(TwinValidatorSetSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinValidatorSetEntity> spec = createTwinValidatorSetSpecification(search);
        Page<TwinValidatorSetEntity> ret = twinValidatorSetRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinValidatorSetEntity> createTwinValidatorSetSpecification(TwinValidatorSetSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), TwinValidatorSetEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinValidatorSetEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinValidatorSetEntity.Fields.id),
                checkFieldLikeIn(search.getNameLikeList(), false, true, TwinValidatorSetEntity.Fields.name),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, TwinValidatorSetEntity.Fields.name),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinValidatorSetEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinValidatorSetEntity.Fields.description),
                checkTernary(search.getInvert(), TwinValidatorSetEntity.Fields.invert)
        );
    }
}
