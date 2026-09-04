package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinlink.TwinLinkCUD;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twinlink.TwinLinkService;


@Component
@RequiredArgsConstructor
public class TwinLinkCUDRestDTOReverseMapper extends RestSimpleDTOMapper<TwinUpdateDTOv1, TwinLinkCUD> {

    private final TwinLinkService twinLinkService;
    private final TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;
    private final TwinLinkUpdateRestDTOReverseMapper twinLinkUpdateRestDTOReverseMapper;

    @Override
    public void map(TwinUpdateDTOv1 src, TwinLinkCUD dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(twinLinkUpdateRestDTOReverseMapper.convertCollection(src.getTwinLinksUpdate()))
                .setCreateList(twinLinkAddRestDTOReverseMapper.convertCollection(src.getTwinLinksAdd()))
                .setDeleteList(twinLinkService.findEntitiesSafe(src.getTwinLinksDelete()).getList());
        if (CollectionUtils.isNotEmpty(dst.getCreateList()))
            dst.getCreateList().forEach(linkCreate -> linkCreate.getTwinLink().setSrcTwinId(src.getTwinId()));
    }
}
