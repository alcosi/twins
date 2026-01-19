package org.twins.core.mappers.rest.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.dto.rest.twinclass.HierarchySearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class HierarchySearchRestDTOReverseMapper extends RestSimpleDTOMapper<HierarchySearchDTOv1, HierarchySearch> {

    @Override
    public void map(HierarchySearchDTOv1 src, HierarchySearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(convertToSetSafe(src.getIdList()))
                .setIdExcludeList(convertToSetSafe(src.getIdExcludeList()))
                .setHierarchyList(convertToSetSafe(src.getHierarchyList()))
                .setDepth(src.getDepth());
    }

    @Override
    public HierarchySearch convert(HierarchySearchDTOv1 src, MapperContext mapperContext) throws Exception {
        if (src == null)
            return HierarchySearch.EMPTY;
        return super.convert(src, mapperContext);
    }
}
