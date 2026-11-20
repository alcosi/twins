package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twin.TwinGroupedSearch;
import org.twins.core.dto.rest.twin.TwinGroupedSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinGroupedSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinGroupedSearchRqDTOv1, TwinGroupedSearch> {

    private final TwinSearchDTOReverseMapper twinSearchDTOReverseMapper;
    private final TwinGroupLevelDTOReverseMapper twinGroupLevelDTOReverseMapper;
    private final TwinMemberSortDTOReverseMapper twinMemberSortDTOReverseMapper;

    @Override
    public void map(TwinGroupedSearchRqDTOv1 src, TwinGroupedSearch dst, MapperContext mapperContext) throws Exception {
        // null elements cannot arrive: levels/memberSorts carry @NotNull on their elements
        dst
                .setSearch(twinSearchDTOReverseMapper.convert(src.getSearch()))
                .setLevels(twinGroupLevelDTOReverseMapper.convertCollection(src.getLevels(), mapperContext))
                .setMemberSorts(twinMemberSortDTOReverseMapper.convertCollection(src.getMemberSorts(), mapperContext));
    }
}
