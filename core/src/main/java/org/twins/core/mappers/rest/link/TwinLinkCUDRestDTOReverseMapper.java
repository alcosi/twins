package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.link.TwinLinkService;


@Component
@RequiredArgsConstructor
public class TwinLinkCUDRestDTOReverseMapper extends RestSimpleDTOMapper<TwinUpdateDTOv1, EntityCUD<TwinLinkEntity>> {
    private final TwinLinkService twinLinkService;
    private final TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;
    private final TwinLinkUpdateRestDTOReverseMapper twinLinkUpdateRestDTOReverseMapper;

    @Override
    public void map(TwinUpdateDTOv1 src, EntityCUD<TwinLinkEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(twinLinkUpdateRestDTOReverseMapper.convertCollection(src.getTwinLinksUpdate()))
                .setCreateList(twinLinkAddRestDTOReverseMapper.convertCollection(src.getTwinLinksAdd()))
                .setDeleteList(twinLinkService.findEntitiesSafe(src.getTwinLinksDelete()).getList());
        dst.getCreateList().forEach(tl -> tl.setSrcTwinId(src.getTwinId()));
    }
}
