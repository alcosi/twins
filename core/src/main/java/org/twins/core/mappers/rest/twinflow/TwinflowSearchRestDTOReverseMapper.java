package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinflowSearch;
import org.twins.core.dto.rest.twinflow.TwinflowSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TwinflowSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowSearchRqDTOv1, TwinflowSearch> {

    @Override
    public void map(TwinflowSearchRqDTOv1 src, TwinflowSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(convertSafe(src.getTwinClassIdList()))
                .setTwinClassIdExcludeList(convertSafe(src.getTwinClassIdExcludeList()))
                .setNameLikeList(convertSafe(src.getNameLikeList()))
                .setDescriptionLikeList(convertSafe(src.getDescriptionLikeList()))
                .setInitialStatusIdList(convertSafe(src.getInitialStatusIdList()))
                .setInitialStatusIdExcludeList(convertSafe(src.getInitialStatusIdExcludeList()));
    }

    private <T> Set<T> convertSafe(List<T> list) {
        if (list == null)
            return null;
        return Set.copyOf(list);
    }
}
