package org.twins.core.mappers.rest.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.HistorySearch;
import org.twins.core.dto.rest.history.HistorySearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class HistorySearchDTOReverseMapper extends RestSimpleDTOMapper<HistorySearchRqDTOv1, HistorySearch> {

    @Override
    public void map(HistorySearchRqDTOv1 src, HistorySearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinIdList(src.getTwinIdList())
                .setTwinIdExcludeList(src.getTwinIdExcludeList())
                .setIncludeDirectChildren(src.getIncludeDirectChildren())
                .setActorUseridList(src.getActorUseridList())
                .setActorUserIdExcludeList(src.getActorUserIdExcludeList())
                .setTypeList(src.getTypeList())
                .setTypeExcludeList(src.getTypeExcludeList())
                .setCreatedAt(src.getCreatedAt());
    }
}