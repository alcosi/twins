package org.twins.core.mappers.rest.domainversion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DomainVersionSearch;
import org.twins.core.dto.rest.domainversion.DomainVersionSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DomainVersionSearchDTOReverseMapper extends RestSimpleDTOMapper<DomainVersionSearchDTOv1, DomainVersionSearch> {
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;

    @Override
    public void map(DomainVersionSearchDTOv1 src, DomainVersionSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setVersionLikeList(src.getVersionLikeList())
                .setVersionNotLikeList(src.getVersionNotLikeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setStatusList(src.getStatusList())
                .setStatusExcludeList(src.getStatusExcludeList())
                .setPreviousDomainVersionIdList(src.getPreviousDomainVersionIdList())
                .setPreviousDomainVersionIdExcludeList(src.getPreviousDomainVersionIdExcludeList())
                .setStartedAt(dataTimeRangeDTOReverseMapper.convert(src.getStartedAt()))
                .setReleasedAt(dataTimeRangeDTOReverseMapper.convert(src.getReleasedAt()));
    }
}
