package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassSchemaSearch;
import org.twins.core.dto.rest.twinclass.TwinClassSchemaSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassSchemaSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassSchemaSearchDTOv1, TwinClassSchemaSearch> {
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;

    @Override
    public void map(TwinClassSchemaSearchDTOv1 src, TwinClassSchemaSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setCreatedByUserIdList(src.getCreatedByUserIdList())
                .setCreatedByUserIdExcludeList(src.getCreatedByUserIdExcludeList())
                .setCreatedAt(src.getCreatedAt() == null ? null : dataTimeRangeDTOReverseMapper.convert(src.getCreatedAt()));
    }
}