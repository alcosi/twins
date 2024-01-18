package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.link.TwinLinkCudDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinLinkCUDRestDTOReverseMapperV2 extends RestSimpleDTOMapper<TwinLinkCudDTOv1, EntityCUD<TwinLinkEntity>> {
    final TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;
    final TwinLinkUpdateRestDTOReverseMapper twinLinkUpdateRestDTOReverseMapper;

    @Override
    public void map(TwinLinkCudDTOv1 src, EntityCUD<TwinLinkEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(twinLinkUpdateRestDTOReverseMapper.convertList(src.getUpdate()))
                .setCreateList(twinLinkAddRestDTOReverseMapper.convertList(src.getCreate()))
                .setDeleteUUIDList(src.getDelete());
    }
}
