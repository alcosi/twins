package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.TwinFieldConditionDTOv1;
import org.twins.core.domain.search.TwinFieldClause;
import org.twins.core.domain.search.TwinFieldFilter;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.dto.rest.twin.TwinFieldClauseDTOv1;
import org.twins.core.dto.rest.twin.TwinFieldSearchDTOv1;
import org.twins.core.dto.rest.twin.TwinFieldsFilterDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinFieldsFilterDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldsFilterDTOv1, TwinFieldFilter> {

    private final TwinFieldSearchDTOReverseMapper twinFieldSearchDTOReverseMapper;

    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final FeaturerService featurerService;

    @Override
    public void map(TwinFieldsFilterDTOv1 src, TwinFieldFilter dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public TwinFieldFilter convert(TwinFieldsFilterDTOv1 src, MapperContext mapperContext) throws Exception {
        if (src == null || CollectionUtils.isEmpty(src.getClauses())) {
            return null;
        }
        Set<UUID> allFieldIds = new HashSet<>();
        for (TwinFieldClauseDTOv1 clause : src.getClauses()) {
            if (CollectionUtils.isNotEmpty(clause.getConditions())) {
                for (TwinFieldConditionDTOv1 cond : clause.getConditions()) {
                    if (cond.getTwinClassFieldId() != null) {
                        allFieldIds.add(cond.getTwinClassFieldId());
                    }
                }
            }
        }
        if (allFieldIds.isEmpty()) {
            return null;
        }
        KitGrouped<TwinClassFieldEntity, UUID, UUID> twinClassFieldEntitiesKit = twinClassFieldService.findTwinClassFields(allFieldIds);

        TwinFieldFilter dst = new TwinFieldFilter();
        for (TwinFieldClauseDTOv1 clauseDto : src.getClauses()) {
            if (CollectionUtils.isEmpty(clauseDto.getConditions())) {
                continue;
            }
            TwinFieldClause clause = new TwinFieldClause();
            List<TwinFieldSearch> conditions = new ArrayList<>();
            for (TwinFieldConditionDTOv1 cond : clauseDto.getConditions()) {
                UUID fieldId = cond.getTwinClassFieldId();
                TwinFieldSearchDTOv1 searchDto = cond.getTwinFieldSearch();
                if (fieldId == null || searchDto == null) {
                    continue;
                }
                TwinFieldSearch search = twinFieldSearchDTOReverseMapper.convert(searchDto, mapperContext);
                TwinClassFieldEntity fieldEntity = twinClassFieldEntitiesKit.get(fieldId);
                if (fieldEntity == null) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_UNKNOWN, "Twin class field not found: " + fieldId);
                }
                search.setTwinClassFieldEntity(fieldEntity);
                FieldTyper<?, ?, ?, TwinFieldSearch> fieldTyper = featurerService.getFeaturer(fieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);
                if (fieldTyper.getTwinFieldSearch().equals(TwinFieldSearchNotImplemented.class)) {
                    throw new ServiceException(ErrorCodeTwins.FIELD_TYPER_SEARCH_NOT_IMPLEMENTED, "Field of type: [" + fieldTyper.getClass().getSimpleName() + "] does not support twin field search");
                }
                if (!fieldTyper.getTwinFieldSearch().equals(search.getClass())) {
                    throw new ServiceException(ErrorCodeCommon.FEATURER_INCORRECT_TYPE, "Incompatible field search type: [" + search.getClass().getSimpleName() + "] for FieldTyper: [" + fieldTyper.getClass() + "] expected type: [" + fieldTyper.getTwinFieldSearch() + "]");
                }
                search.setFieldTyper(fieldTyper);
                conditions.add(search);
            }
            if (!conditions.isEmpty()) {
                clause.setConditions(conditions);
                dst.addClause(clause);
            }
        }
        return dst.isEmpty() ? null : dst;
    }
}
