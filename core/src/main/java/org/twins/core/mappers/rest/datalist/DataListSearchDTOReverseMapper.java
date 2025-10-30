package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DataListSearch;
import org.twins.core.dto.rest.datalist.DataListSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListSearchDTOReverseMapper extends RestSimpleDTOMapper<DataListSearchRqDTOv1, DataListSearch> {

    private final DataListOptionSearchDTOReverseMapper dataListOptionSearchDTOReverseMapper;

    @Override
    public void map(DataListSearchRqDTOv1 src, DataListSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList())
                .setExternalIdLikeList(src.getExternalIdLikeList())
                .setExternalIdNotLikeList(src.getExternalIdNotLikeList())
                .setDefaultOptionIdList(src.getDefaultOptionIdList())
                .setDefaultOptionIdExcludeList(src.getDefaultOptionIdExcludeList())
                .setOptionSearch(dataListOptionSearchDTOReverseMapper.convert(src.getOptionSearch(), mapperContext));
    }
}
