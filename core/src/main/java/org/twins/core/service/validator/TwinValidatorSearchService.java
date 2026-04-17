package org.twins.core.service.validator;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.math.IntegerRange;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorRepository;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinValidatorSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinValidatorSearchService {
    private final TwinValidatorRepository twinValidatorRepository;
    private final AuthService authService;

    public PaginationResult<TwinValidatorEntity> findTwinValidators(TwinValidatorSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinValidatorEntity> spec = createTwinValidatorSpecification(search);
        Page<TwinValidatorEntity> ret = twinValidatorRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinValidatorEntity> createTwinValidatorSpecification(TwinValidatorSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), TwinValidatorEntity.Fields.twinValidatorSet, TwinValidatorSetEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinValidatorEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinValidatorEntity.Fields.id),
                checkUuidIn(search.getTwinValidatorSetIdList(), false, false, TwinValidatorEntity.Fields.twinValidatorSetId),
                checkUuidIn(search.getTwinValidatorSetIdExcludeList(), true, false, TwinValidatorEntity.Fields.twinValidatorSetId),
                checkIntegerIn(search.getValidatorFeaturerIdList(), false, TwinValidatorEntity.Fields.twinValidatorFeaturerId),
                checkIntegerIn(search.getValidatorFeaturerIdExcludeList(), true, TwinValidatorEntity.Fields.twinValidatorFeaturerId),
                checkTernary(search.getInvert(), TwinValidatorEntity.Fields.invert),
                checkTernary(search.getActive(), TwinValidatorEntity.Fields.active),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinValidatorEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinValidatorEntity.Fields.description),
                checkFieldIntegerRange(search.getOrder(), TwinValidatorEntity.Fields.order)
        );
    }
}
