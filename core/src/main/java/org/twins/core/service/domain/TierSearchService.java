package org.twins.core.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.domain.TierRepository;
import org.twins.core.domain.search.TierSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.domain.TierSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TierSearchService {
    private final TierRepository tierRepository;
    private final AuthService authService;

    public PaginationResult<TierEntity> findTiers(TierSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TierEntity> spec = createTierSearchSpecification(search);
        Page<TierEntity> ret = tierRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TierEntity> createTierSearchSpecification(TierSearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinAttachmentEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinAttachmentEntity.Fields.id),
                checkUuidIn(search.getPermissionSchemaIdList(), false, false, TwinAttachmentEntity.Fields.twinId),
                checkUuidIn(search.getPermissionSchemaIdExcludeList(), true, false, TwinAttachmentEntity.Fields.twinId),
                checkUuidIn(search.getTwinflowSchemaIdList(), false, false, TwinAttachmentEntity.Fields.twinId),
                checkUuidIn(search.getTwinflowSchemaIdExcludeList(), true, false, TwinAttachmentEntity.Fields.twinId),
                checkUuidIn(search.getTwinclassSchemaIdList(), false, false, TwinAttachmentEntity.Fields.twinId),
                checkUuidIn(search.getTwinclassSchemaIdExcludeList(), true, false, TwinAttachmentEntity.Fields.twinId),
                checkFieldLikeIn(search.getNameLikeList(), false, true, TwinAttachmentEntity.Fields.storageLink),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, TwinAttachmentEntity.Fields.storageLink),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinAttachmentEntity.Fields.storageLink),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinAttachmentEntity.Fields.storageLink),
                checkAttachmentsStorageQuotaCountRange(search.getAttachmentsStorageQuotaSizeRange()),
                checkAttachmentsStorageQuotaSizeRange(search.getAttachmentsStorageQuotaSizeRange()),
                checkUserCountQuotaRange(search.getUserCountQuotaRange()),
                checkTernary(TierEntity.Fields.custom, search.getCustom())
        );
    }
}
