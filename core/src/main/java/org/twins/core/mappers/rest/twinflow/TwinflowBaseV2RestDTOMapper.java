package org.twins.core.mappers.rest.twinflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperModePointer;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinflowBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinflowEntity, TwinflowBaseDTOv2> {
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;
    final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(TwinflowEntity src, TwinflowBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinflowBaseV1RestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(TwinflowAuthorMode.HIDE) && src.getCreatedByUserId() != null)
            dst
                    .setCreatedByUser(userRestDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(TwinflowAuthorMode.SHORT)))
                    .setCreatedByUserId(src.getCreatedByUserId());
        if (mapperContext.hasModeButNot(TwinflowInitStatusMode.HIDE) && src.getCreatedByUserId() != null)
            dst
                    .setInitialStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getInitialTwinStatus(), mapperContext.forkOnPoint(TwinflowInitStatusMode.SHORT)))
                    .setInitialStatusId(src.getInitialTwinStatusId());
    }

    @Override
    public String getObjectCacheId(TwinflowEntity src) {
        return twinflowBaseV1RestDTOMapper.getObjectCacheId(src);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinflowBaseV1RestDTOMapper.hideMode(mapperContext);
    }

    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinflowAuthorMode implements MapperModePointer<UserRestDTOMapper.Mode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        @Getter
        final int priority;

        @Override
        public UserRestDTOMapper.Mode point() {
            return switch (this) {
                case HIDE -> UserRestDTOMapper.Mode.HIDE;
                case SHORT -> UserRestDTOMapper.Mode.SHORT;
                case DETAILED -> UserRestDTOMapper.Mode.DETAILED;
            };
        }
    }

    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinflowInitStatusMode implements MapperModePointer<TwinStatusRestDTOMapper.Mode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        @Getter
        final int priority;

        @Override
        public TwinStatusRestDTOMapper.Mode point() {
            return switch (this) {
                case HIDE -> TwinStatusRestDTOMapper.Mode.HIDE;
                case SHORT -> TwinStatusRestDTOMapper.Mode.SHORT;
                case DETAILED -> TwinStatusRestDTOMapper.Mode.DETAILED;
            };
        }
    }
}
