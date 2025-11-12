package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
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
    private final TwinLinkCreateRestDTOReverseMapper twinLinkCreateRestDTOReverseMapper;
    private final TwinLinkUpdateRestDTOReverseMapper twinLinkUpdateRestDTOReverseMapper;

    @Override
    public void map(TwinUpdateDTOv1 src, EntityCUD<TwinLinkEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(twinLinkUpdateRestDTOReverseMapper.convertCollection(src.getTwinLinksUpdate()))
                .setCreateList(twinLinkCreateRestDTOReverseMapper.convertCollection(src.getTwinLinksCreate()))
                .setDeleteList(twinLinkService.findEntitiesSafe(src.getTwinLinksDelete()).getList());
        if (CollectionUtils.isNotEmpty(dst.getCreateList())) {
            dst.getCreateList().forEach(tl -> tl.setSrcTwinId(src.getTwinId()));
        }
    }
}
