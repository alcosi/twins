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
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.domain.search.TwinFieldValueSearch;
import org.twins.core.domain.search.TwinFieldValueSearchNotImplemented;
import org.twins.core.dto.rest.twin.TwinFieldConditionDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinFieldConditionListDTOReverseMapper extends RestSimpleDTOMapper<List<TwinFieldConditionDTOv1>, List<TwinFieldSearch>> {

    private final TwinFieldSearchDTOReverseMapper twinFieldSearchDTOReverseMapper;

    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final FeaturerService featurerService;

    @Override
    public void map(List<TwinFieldConditionDTOv1> src, List<TwinFieldSearch> dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public List<TwinFieldSearch> convert(List<TwinFieldConditionDTOv1> src, MapperContext mapperContext) throws Exception {
        List<TwinFieldSearch> dst = null;
        if (CollectionUtils.isNotEmpty(src)) {
            dst = new ArrayList<>();
            List<UUID> twinClassFieldIds = src.stream().map(TwinFieldConditionDTOv1::getTwinClassFieldId).toList();
            KitGrouped<TwinClassFieldEntity, UUID, UUID> twinClassFieldEntitiesKit = twinClassFieldService.findTwinClassFields(twinClassFieldIds);
            for (var field : src) {
                TwinFieldSearch search = twinFieldSearchDTOReverseMapper.convert(field.getTwinFieldSearch(), mapperContext);
                search.setTwinClassFieldEntity(twinClassFieldEntitiesKit.get(field.getTwinClassFieldId()));
                if (search instanceof TwinFieldValueSearch valueSearch) {
                    FieldTyper<?, ?, ?, TwinFieldValueSearch> fieldTyper = featurerService.getFeaturer(search.getTwinClassFieldEntity().getFieldTyperFeaturerId(), FieldTyper.class);
                    if (TwinFieldValueSearchNotImplemented.class.isAssignableFrom(fieldTyper.getTwinFieldSearch()))
                        throw new ServiceException(ErrorCodeTwins.FIELD_TYPER_SEARCH_NOT_IMPLEMENTED, "Field of type: [" + this.getClass().getSimpleName() + "] do not support twin field search not implemented");
                    if (!fieldTyper.getTwinFieldSearch().equals(valueSearch.getClass()))
                        throw new ServiceException(ErrorCodeCommon.FEATURER_INCORRECT_TYPE, "Incompatible field search type: [" + valueSearch.getClass().getSimpleName() + "] for FieldTyper:  [" + fieldTyper.getClass() + "] expected type: [" + fieldTyper.getTwinFieldSearch() + "]");
                    valueSearch.setFieldTyper(fieldTyper);
                }
                dst.add(search);
            }
        }
        return dst;
    }
}
