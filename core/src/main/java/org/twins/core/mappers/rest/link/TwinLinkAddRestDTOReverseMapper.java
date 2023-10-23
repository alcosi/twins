package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.attachment.AttachmentBaseDTOv1;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.auth.AuthService;


@Component
@RequiredArgsConstructor
public class TwinLinkAddRestDTOReverseMapper extends RestSimpleDTOMapper<TwinLinkAddDTOv1, TwinLinkEntity> {
    final AuthService authService;

    @Override
    public void map(TwinLinkAddDTOv1 src, TwinLinkEntity dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();
        dst
                .setLinkId(src.getLinkId())
                .setDstTwinId(src.getDstTwinId())
                .setCreatedByUserId(apiUser.getUser().getId());
    }
}
