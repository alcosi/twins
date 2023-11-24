package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class TwinLinkCUDRestDTOReverseMapper extends RestSimpleDTOMapper<TwinUpdateDTOv1, EntityCUD<TwinLinkEntity>> {
    final TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;
    final TwinLinkUpdateRestDTOReverseMapper twinLinkUpdateRestDTOReverseMapper;

    @Override
    public void map(TwinUpdateDTOv1 src, EntityCUD<TwinLinkEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setCreateList(twinLinkAddRestDTOReverseMapper.convertList(src.getTwinLinksAdd()))
                .setUpdateList(twinLinkUpdateRestDTOReverseMapper.convertList(src.getTwinLinksUpdate()))
                .setDeleteUUIDList(src.getTwinLinksDelete());
    }
}
