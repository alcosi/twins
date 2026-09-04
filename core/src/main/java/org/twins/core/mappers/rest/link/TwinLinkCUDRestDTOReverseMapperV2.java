package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinlink.TwinLinkCUD;
import org.twins.core.dto.rest.link.TwinLinkCudDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twinlink.TwinLinkService;


@Component
@RequiredArgsConstructor
public class TwinLinkCUDRestDTOReverseMapperV2 extends RestSimpleDTOMapper<TwinLinkCudDTOv1, TwinLinkCUD> {

    private final TwinLinkService twinLinkService;
    private final TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;
    private final TwinLinkUpdateRestDTOReverseMapper twinLinkUpdateRestDTOReverseMapper;

    @Override
    public void map(TwinLinkCudDTOv1 src, TwinLinkCUD dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(twinLinkUpdateRestDTOReverseMapper.convertCollection(src.getUpdate()))
                .setCreateList(twinLinkAddRestDTOReverseMapper.convertCollection(src.getCreate()))
                .setDeleteList(twinLinkService.findEntitiesSafe(src.getDelete()).getList());
    }
}
