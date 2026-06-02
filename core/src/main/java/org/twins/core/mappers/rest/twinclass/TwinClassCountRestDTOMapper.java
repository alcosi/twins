package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.twinclass.TwinClassCountDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinClassCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinClassEntity>, TwinClassCountDTOv1> {

    @Override
    public void map(CountResult<TwinClassEntity> src, TwinClassCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setOwnerType(entity.getOwnerType())
                .setAbstractt(entity.getAbstractt())
                .setSegment(entity.getSegment())
                .setTwinClassFreezeId(entity.getTwinClassFreezeId())
                .setHeadTwinClassId(entity.getHeadTwinClassId())
                .setExtendsTwinClassId(entity.getExtendsTwinClassId())
                .setMarkerDataListId(entity.getMarkerDataListId())
                .setTagDataListId(entity.getTagDataListId())
                .setTwinflowSchemaSpace(entity.getTwinflowSchemaSpace())
                .setTwinClassSchemaSpace(entity.getTwinClassSchemaSpace())
                .setAliasSpace(entity.getAliasSpace())
                .setViewPermissionId(entity.getViewPermissionId())
                .setHeadHunterFeaturerId(entity.getHeadHunterFeaturerId())
                .setEditPermissionId(entity.getEditPermissionId())
                .setDeletePermissionId(entity.getDeletePermissionId())
                .setAssigneeRequired(entity.getAssigneeRequired())
                .setUniqueName(entity.getUniqueName())
                .setHasDynamicMarkers(entity.getHasDynamicMarkers())
                .setBreadCrumbsFaceId(entity.getBreadCrumbsFaceId())
                .setPageFaceId(entity.getPageFaceId())
                .setCount(src.getCount());
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinClassEntity>> srcCollection, MapperContext mapperContext) throws Exception {
        // no related objects to batch-load
    }
}
