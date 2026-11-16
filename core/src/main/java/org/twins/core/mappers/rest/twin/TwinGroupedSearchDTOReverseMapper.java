package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twin.TwinGroupedSearch;
import org.twins.core.domain.twin.TwinGroupLevel;
import org.twins.core.domain.twin.TwinMemberSort;
import org.twins.core.dto.rest.twin.TwinGroupedSearchRqDTOv1;
import org.twins.core.dto.rest.twin.TwinGroupLevelDTOv1;
import org.twins.core.dto.rest.twin.TwinSortDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TwinGroupedSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinGroupedSearchRqDTOv1, TwinGroupedSearch> {

    private final TwinSearchDTOReverseMapper twinSearchDTOReverseMapper;

    @Override
    public void map(TwinGroupedSearchRqDTOv1 src, TwinGroupedSearch dst, MapperContext mapperContext) throws Exception {
        dst.setSearch(twinSearchDTOReverseMapper.convert(src.getSearch()));
        if (src.getLevels() != null) {
            List<TwinGroupLevel> levels = new ArrayList<>(src.getLevels().size());
            for (TwinGroupLevelDTOv1 level : src.getLevels()) {
                // null elements are preserved on purpose: resolveLevels reports "level is required"
                levels.add(level == null ? null : new TwinGroupLevel()
                        .setFieldId(level.getFieldId())
                        .setSort(level.getSort()));
            }
            dst.setLevels(levels);
        }
        if (src.getMemberSorts() != null) {
            List<TwinMemberSort> memberSorts = new ArrayList<>(src.getMemberSorts().size());
            for (TwinSortDTOv1 sort : src.getMemberSorts()) {
                memberSorts.add(sort == null ? null : new TwinMemberSort()
                        .setFieldId(sort.getTwinClassFieldId())
                        .setDirection(sort.getDirection()));
            }
            dst.setMemberSorts(memberSorts);
        }
    }
}
