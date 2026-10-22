package org.twins.core.mappers.rest.link;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.LinkValidatorSearch;
import org.twins.core.dto.rest.link.LinkValidatorSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class LinkValidatorSearchRestDTOReverseMapper extends RestSimpleDTOMapper<LinkValidatorSearchDTOv1, LinkValidatorSearch> {

    @Override
    public void map(LinkValidatorSearchDTOv1 src, LinkValidatorSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setLinkIdList(src.getLinkIdList())
                .setLinkIdExcludeList(src.getLinkIdExcludeList())
                .setLinkerFeaturerIdList(src.getLinkerFeaturerIdList())
                .setLinkerFeaturerIdExcludeList(src.getLinkerFeaturerIdExcludeList());
    }
}
