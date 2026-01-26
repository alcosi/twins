package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.MapUtils;
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
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinFieldSearchMapDTOReverseMapper extends RestSimpleDTOMapper<Map<UUID, TwinFieldSearchDTOv1>, List<TwinFieldSearch>> {

    private final TwinFieldSearchDTOReverseMapper twinFieldSearchDTOReverseMapper;

    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final FeaturerService featurerService;

    @Override
    public void map(Map<UUID, TwinFieldSearchDTOv1> src, List<TwinFieldSearch> dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public List<TwinFieldSearch> convert(Map<UUID, TwinFieldSearchDTOv1> src, MapperContext mapperContext) throws Exception {
        List<TwinFieldSearch> dst = null;
        if (MapUtils.isNotEmpty(src)) {
            dst = new ArrayList<>();
            KitGrouped<TwinClassFieldEntity, UUID, UUID> twinClassFieldEntitiesKit = twinClassFieldService.findTwinClassFields(src.keySet());
            for (Map.Entry<UUID, TwinFieldSearchDTOv1> field : src.entrySet()) {
                TwinFieldSearch search = twinFieldSearchDTOReverseMapper.convert(field.getValue(), mapperContext);
                search.setTwinClassFieldEntity(twinClassFieldEntitiesKit.get(field.getKey()));
                FieldTyper<?, ?, ?, TwinFieldSearch> fieldTyper = featurerService.getFeaturer(search.getTwinClassFieldEntity().getFieldTyperFeaturerId(), FieldTyper.class);
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
