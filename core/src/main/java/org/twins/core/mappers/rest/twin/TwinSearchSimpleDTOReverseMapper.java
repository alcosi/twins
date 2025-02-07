package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchSimpleDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TwinSearchSimpleDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchSimpleDTOv1, BasicSearch> {

    @Override
    public void map(TwinSearchSimpleDTOv1 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .addTwinNameLike(src.getNameLike());
//todo          .addTwinAliaLike(src.getAliasLike());
    }
}
