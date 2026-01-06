package org.twins.core.service.datalist;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperList;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.List;
import java.util.Properties;

import static org.cambium.common.util.EnumUtils.convertOrEmpty;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.datalist.DataListOptionSpecification.*;

//Log calls that took more than 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListOptionSearchService {
    private final AuthService authService;
    private final DataListOptionRepository dataListOptionRepository;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final FeaturerService featurerService;

    public List<DataListOptionEntity> findDataListOptions(DataListOptionSearch search) throws ServiceException {
        Specification<DataListOptionEntity> spec = createDataListOptionSearchSpecification(search);
        return dataListOptionRepository.findAll(spec);
    }

    public PaginationResult<DataListOptionEntity> findDataListOptionForDomain(DataListOptionSearch search, SimplePagination pagination) throws ServiceException {
        Specification<DataListOptionEntity> spec = createDataListOptionSearchSpecification(search);
        Page<DataListOptionEntity> ret = dataListOptionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<DataListOptionEntity> createDataListOptionSearchSpecification(DataListOptionSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        limitSearchByValidForTwinClassFieldIdList(search);
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), DataListOptionEntity.Fields.dataList, DataListEntity.Fields.domainId),
                createBusinessAccountSpecification(apiUser, search),
                checkUuidIn(search.getIdList(), false, false, DataListOptionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, DataListOptionEntity.Fields.id),
                checkUuidIn(search.getDataListIdList(), false, false, DataListOptionEntity.Fields.dataListId),
                checkUuidIn(search.getDataListIdExcludeList(), true, true, DataListOptionEntity.Fields.dataListId),
                checkFieldLikeIn(search.getOptionLikeList(), false, true, DataListOptionEntity.Fields.option),
                checkFieldLikeIn(search.getOptionNotLikeList(), true, true, DataListOptionEntity.Fields.option),
                checkDataListKeyLikeIn(search.getDataListKeyList(), false, true),
                checkDataListKeyLikeIn(search.getDataListKeyExcludeList(), true, true),
                checkStatusLikeIn(convertOrEmpty(search.getStatusIdList()), false, true),
                checkStatusLikeIn(convertOrEmpty(search.getStatusIdExcludeList()), true, true),
                joinAndSearchByI18NField(DataListOptionEntity.Fields.optionI18n, search.getOptionI18nLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(DataListOptionEntity.Fields.optionI18n, search.getOptionI18nNotLikeList(), apiUser.getLocale(), true, true),
                checkDataListSubset(search.getDataListSubsetIdList(), false),
                checkDataListSubset(search.getDataListSubsetIdExcludeList(), true),
                checkDataListSubsetKey(search.getDataListSubsetKeyList(), false, true),
                checkDataListSubsetKey(search.getDataListSubsetKeyExcludeList(), true, true),
                checkFieldLikeIn(search.getExternalIdLikeList(), false, true, DataListOptionEntity.Fields.externalId),
                checkFieldLikeIn(search.getExternalIdNotLikeList(), true, true, DataListOptionEntity.Fields.externalId),
                checkFieldIn(search.getExternalIdList(), false, true, false, DataListOptionEntity.Fields.externalId),
                checkFieldIn(search.getExternalIdExcludeList(), true, true, false, DataListOptionEntity.Fields.externalId),
                checkTernary(search.getCustom(), DataListOptionEntity.Fields.custom));
    }

    private Specification<DataListOptionEntity> createBusinessAccountSpecification(ApiUser apiUser, DataListOptionSearch search) {
        if (apiUser.isBusinessAccountSpecified()) {
            return Specification.allOf(
                    checkUuidIn(search.getBusinessAccountIdList(), false, true, DataListOptionEntity.Fields.businessAccountId),
                    checkUuidIn(search.getBusinessAccountIdExcludeList(), true, true, DataListOptionEntity.Fields.businessAccountId)
            );
        } else {
            return (root, query, cb) -> root.get(DataListOptionEntity.Fields.businessAccountId).isNull();
        }
    }

    public void limitSearchByValidForTwinClassFieldIdList(DataListOptionSearch search) throws ServiceException {
        if (CollectionUtils.isEmpty(search.getValidForTwinClassFieldIdList())) {
            return;
        }
        var twinClassFieldKit = twinClassFieldService.findEntitiesSafe(search.getValidForTwinClassFieldIdList());
        for (var fieldEntity : twinClassFieldKit.getCollection()) {
            FieldTyper<?, ?, ?, ?> fieldTyper = featurerService.getFeaturer(fieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);

            if (fieldTyper.getStorageType() != TwinFieldStorageDatalist.class) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, "Wrong fieldTyper for [" + fieldEntity.getId() + "]");
            }
            FieldTyperList fieldTyperList = (FieldTyperList) fieldTyper;
            Properties properties = fieldTyper.extractProperties(fieldEntity.getFieldTyperParams());
            search
                    .addDataListId(fieldTyperList.getDataListId(properties), false)
                    .setIdList(CollectionUtils.safeAdd(search.getIdList(), fieldTyperList.getDataListOptionIds(properties)))
                    .setIdExcludeList(CollectionUtils.safeAdd(search.getIdExcludeList(), fieldTyperList.getDataListOptionExcludeIds(properties)))
                    .setDataListSubsetIdList(CollectionUtils.safeAdd(search.getDataListSubsetIdList(), fieldTyperList.getDataListSubsetIds(properties)))
                    .setDataListSubsetIdExcludeList(CollectionUtils.safeAdd(search.getDataListSubsetIdExcludeList(), fieldTyperList.getDataListSubsetExcludeIds(properties)));
        }
    }
}
