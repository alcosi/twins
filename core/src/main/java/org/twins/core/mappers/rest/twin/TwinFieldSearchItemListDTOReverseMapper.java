package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.*;
import org.twins.core.dto.rest.twin.*;
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
public class TwinFieldSearchItemListDTOReverseMapper extends RestSimpleDTOMapper<List<TwinFieldSearchItem>, List<TwinFieldSearch>> {

    private final TwinFieldSearchDTOReverseMapper twinFieldSearchDTOReverseMapper;

    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final FeaturerService featurerService;

    @Override
    public void map(List<TwinFieldSearchItem> src, List<TwinFieldSearch> dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public List<TwinFieldSearch> convert(List<TwinFieldSearchItem> src, MapperContext mapperContext) throws Exception {
        List<TwinFieldSearch> dst = null;
        if (CollectionUtils.isNotEmpty(src)) {
            dst = new ArrayList<>();
            Kit<TwinFieldSearchItem, UUID> twinFieldSearchItemsKit = new Kit<>(src, TwinFieldSearchItem::getFieldId);
            KitGrouped<TwinClassFieldEntity, UUID, UUID> twinClassFieldEntitiesKit = twinClassFieldService.findTwinClassFields(twinFieldSearchItemsKit.getIdSet());
            for (TwinFieldSearchItem field : src) {
                TwinFieldSearch search = twinFieldSearchDTOReverseMapper.convert(field.getFieldSearch(), mapperContext);
                search.setTwinClassFieldEntity(twinClassFieldEntitiesKit.get(field.getFieldId()));
                FieldTyper<?, ?, ?, TwinFieldSearch> fieldTyper = featurerService.getFeaturer(search.getTwinClassFieldEntity().getFieldTyperFeaturer(), FieldTyper.class);
                if (fieldTyper.getTwinFieldSearch().equals(TwinFieldSearchNotImplemented.class))
                    throw new ServiceException(ErrorCodeTwins.FIELD_TYPER_SEARCH_NOT_IMPLEMENTED, "Field of type: [" + this.getClass().getSimpleName() + "] do not support twin field search not implemented");
                if (!fieldTyper.getTwinFieldSearch().equals(search.getClass()))
                    throw new ServiceException(ErrorCodeCommon.FEATURER_INCORRECT_TYPE, "Incompatible field search type: [" + search.getClass().getSimpleName() + "] for FieldTyper:  [" + fieldTyper.getClass() + "] expected type: [" + fieldTyper.getTwinFieldSearch() + "]");
                search.setFieldTyper(fieldTyper);
                dst.add(search);
            }
        }
        return dst;
    }
}
