package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.link.TwinLinkCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class TwinLinkCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinLinkCreateDTOv1, TwinLinkEntity> {

    private final AuthService authService;
    private final TwinService twinService;

    @Override
    public void map(TwinLinkCreateDTOv1 src, TwinLinkEntity dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();
        dst
                .setDstTwin(twinService.findEntity(src.getDstTwinId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows))
                .setLinkId(src.getLinkId())
                .setDstTwinId(src.getDstTwinId())
                .setCreatedByUserId(apiUser.getUser().getId());
    }
}
