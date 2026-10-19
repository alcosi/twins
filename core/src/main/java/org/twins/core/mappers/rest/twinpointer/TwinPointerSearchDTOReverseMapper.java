package org.twins.core.mappers.rest.twinpointer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twin.TwinPointerSearch;
import org.twins.core.dto.rest.twinpointer.TwinPointerSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinPointerSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinPointerSearchDTOv1, TwinPointerSearch> {
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;

    @Override
    public void map(TwinPointerSearchDTOv1 src, TwinPointerSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinClassIdList(src.getTwinClassIdList())
                .setTwinClassIdExcludeList(src.getTwinClassIdExcludeList())
                .setPointerFeaturerIdList(src.getPointerFeaturerIdList())
                .setPointerFeaturerIdExcludeList(src.getPointerFeaturerIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setCreatedAt(dataTimeRangeDTOReverseMapper.convert(src.getCreatedAt()));
    }
}
