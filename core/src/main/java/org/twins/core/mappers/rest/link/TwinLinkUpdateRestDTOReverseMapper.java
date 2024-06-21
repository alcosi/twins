package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.link.TwinLinkUpdateDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class TwinLinkUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinLinkUpdateDTOv1, TwinLinkEntity> {
    final TwinService twinService;

    @Override
    public void map(TwinLinkUpdateDTOv1 src, TwinLinkEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setDstTwin(twinService.findEntity(src.getDstTwinId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows))
                .setId(src.getId())
                .setDstTwinId(src.getDstTwinId()); // also it can be srcTwinId for backward link. it must be changed in service
    }
}
