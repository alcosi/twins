package org.twins.core.mappers.rest.featurer;

import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.featurer.FeaturerCountDTOv1;
import org.twins.core.enums.sort.FeaturerGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;

import java.util.Collection;

@Component
@MapperModeBinding(modes = FeaturerMode.class)
public class FeaturerCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<FeaturerEntity, FeaturerGroupField>, FeaturerCountDTOv1> {

    @Override
    public void map(CountResult<FeaturerEntity, FeaturerGroupField> src, FeaturerCountDTOv1 dst, MapperContext mapperContext) {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst.setCount(src.getCount());
        if (src.getGroupFields().contains(FeaturerGroupField.featurerTypeId))
            dst.setFeaturerTypeId(entity.getFeaturerTypeId());
        if (src.getGroupFields().contains(FeaturerGroupField.deprecated))
            dst.setDeprecated(entity.isDeprecated());
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<FeaturerEntity, FeaturerGroupField>> srcCollection, MapperContext mapperContext) {
        // no related objects to load — group fields are plain scalars (featurerTypeId, deprecated)
    }
}
