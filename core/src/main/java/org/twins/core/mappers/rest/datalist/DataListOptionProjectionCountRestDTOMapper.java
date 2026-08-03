package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionCountDTOv1;
import org.twins.core.enums.sort.DataListOptionProjectionGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionProjectionMode;
import org.twins.core.mappers.rest.mappercontext.modes.ProjectionTypeMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.projection.ProjectionTypeRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.datalist.DataListOptionProjectionService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListOptionProjectionMode.class)
public class DataListOptionProjectionCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<DataListOptionProjectionEntity, DataListOptionProjectionGroupField>, DataListOptionProjectionCountDTOv1> {

    @MapperModePointerBinding(modes = ProjectionTypeMode.DataListOptionProjection2ProjectionTypeMode.class)
    private final ProjectionTypeRestDTOMapper projectionTypeRestDTOMapper;

    @MapperModePointerBinding(modes = DataListOptionMode.DataListOptionProjection2DataListOptionMode.class)
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.DataListOptionProjection2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final DataListOptionProjectionService dataListOptionProjectionService;

    @Override
    public void map(CountResult<DataListOptionProjectionEntity, DataListOptionProjectionGroupField> src, DataListOptionProjectionCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setProjectionTypeId(entity.getProjectionTypeId())
                .setSrcDataListOptionId(entity.getSrcDataListOptionId())
                .setDstDataListOptionId(entity.getDstDataListOptionId())
                .setSavedByUserId(entity.getSavedByUserId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, ProjectionTypeMode.DataListOptionProjection2ProjectionTypeMode.HIDE, src, DataListOptionProjectionGroupField.projectionTypeId)) {
            dataListOptionProjectionService.loadProjectionTypes(entity);
            projectionTypeRestDTOMapper.convertOrPostpone(entity.getProjectionType(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(ProjectionTypeMode.DataListOptionProjection2ProjectionTypeMode.SHORT)));
        }
        // src and dst options are loaded together by loadDataListOptions
        if (needLoad(mapperContext, DataListOptionMode.DataListOptionProjection2DataListOptionMode.HIDE, src, DataListOptionProjectionGroupField.srcDataListOptionId, DataListOptionProjectionGroupField.dstDataListOptionId)) {
            dataListOptionProjectionService.loadDataListOptions(entity);
            if (src.getGroupFields().contains(DataListOptionProjectionGroupField.srcDataListOptionId)) {
                dataListOptionRestDTOMapper.convertOrPostpone(entity.getSrcDataListOption(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListOptionMode.DataListOptionProjection2DataListOptionMode.SHORT)));
            }
            if (src.getGroupFields().contains(DataListOptionProjectionGroupField.dstDataListOptionId)) {
                dataListOptionRestDTOMapper.convertOrPostpone(entity.getDstDataListOption(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListOptionMode.DataListOptionProjection2DataListOptionMode.SHORT)));
            }
        }
        if (needLoad(mapperContext, UserMode.DataListOptionProjection2UserMode.HIDE, src, DataListOptionProjectionGroupField.savedByUserId)) {
            dataListOptionProjectionService.loadSavedByUser(entity);
            userRestDTOMapper.convertOrPostpone(entity.getSavedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DataListOptionProjection2UserMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<DataListOptionProjectionEntity, DataListOptionProjectionGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(java.util.Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var sample = srcCollection.iterator().next();
        if (needLoad(mapperContext, ProjectionTypeMode.DataListOptionProjection2ProjectionTypeMode.HIDE, sample, DataListOptionProjectionGroupField.projectionTypeId)) {
            dataListOptionProjectionService.loadProjectionTypes(entityCollection);
        }
        if (needLoad(mapperContext, DataListOptionMode.DataListOptionProjection2DataListOptionMode.HIDE, sample, DataListOptionProjectionGroupField.srcDataListOptionId, DataListOptionProjectionGroupField.dstDataListOptionId)) {
            dataListOptionProjectionService.loadDataListOptions(entityCollection);
        }
        if (needLoad(mapperContext, UserMode.DataListOptionProjection2UserMode.HIDE, sample, DataListOptionProjectionGroupField.savedByUserId)) {
            dataListOptionProjectionService.loadSavedByUser(entityCollection);
        }
    }
}
