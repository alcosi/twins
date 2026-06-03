package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.twinclass.TwinClassFieldCountDTOv1;
import org.twins.core.enums.sort.TwinClassFieldGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinClassFieldCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinClassFieldEntity, TwinClassFieldGroupField>, TwinClassFieldCountDTOv1> {

    @Override
    public void map(CountResult<TwinClassFieldEntity, TwinClassFieldGroupField> src, TwinClassFieldCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setRequired(entity.getRequired())
                .setInheritable(entity.getInheritable())
                .setSystem(entity.getSystem())
                .setDependentField(entity.getDependentField())
                .setHasDependentFields(entity.getHasDependentFields())
                .setProjectionField(entity.getProjectionField())
                .setHasProjectedFields(entity.getHasProjectedFields())
                .setTwinClassId(entity.getTwinClassId())
                .setFieldTyperFeaturerId(entity.getFieldTyperFeaturerId())
                .setTwinSorterFeaturerId(entity.getTwinSorterFeaturerId())
                .setFieldInitializerFeaturerId(entity.getFieldInitializerFeaturerId())
                .setViewPermissionId(entity.getViewPermissionId())
                .setEditPermissionId(entity.getEditPermissionId())
                .setCount(src.getCount());
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinClassFieldEntity, TwinClassFieldGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        // no related objects to batch-load
    }
}
