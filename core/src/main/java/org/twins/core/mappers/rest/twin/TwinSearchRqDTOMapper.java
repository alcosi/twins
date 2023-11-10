package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinSearchRqDTOMapper extends RestSimpleDTOMapper<TwinSearchRqDTOv1, BasicSearch> {

    @Override
    public void map(TwinSearchRqDTOv1 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(convertSafe(src.getTwinClassIdList()))
                .setStatusIdList(convertSafe(src.getStatusIdList()))
                .setAssignerUserIdList(convertSafe(src.getAssignerUserIdList()))
                .setHeaderTwinIdList(convertSafe(src.getHeadTwinIdList()))
                .setCreatedByUserIdList(convertSafe(src.getCreatedByUserIdList()))
                .setOwnerUserIdList(convertSafe(src.getOwnerUserIdList()))
                .setOwnerBusinessAccountIdList(convertSafe(src.getOwnerBusinessAccountIdList()));
    }

    private Set<UUID> convertSafe(List<UUID> list) {
        if (list == null)
            return null;
        return Set.copyOf(list);
    }

}
