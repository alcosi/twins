package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinLinkAddRestDTOReverseMapper extends RestSimpleDTOMapper<TwinLinkAddDTOv1, TwinLinkEntity> {

    private final AuthService authService;
    private final TwinService twinService;

    @Override
    public void map(TwinLinkAddDTOv1 src, TwinLinkEntity dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();
        dst
                .setLinkId(src.getLinkId())
                .setCreatedByUserId(apiUser.getUser().getId());

        // Only resolve dstTwin if it's not a temporalId reference
        if (src.getDstTwinId() != null && !src.getDstTwinId().startsWith("temporalId:")) {
            try {
                UUID dstTwinId = UUID.fromString(src.getDstTwinId());
                dst.setDstTwin(twinService.findEntity(dstTwinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows))
                   .setDstTwinId(dstTwinId);
            } catch (IllegalArgumentException e) {
                // Invalid UUID format - will be handled later
                dst.setDstTwinId(null);
            }
        }
    }
}
