package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldRuleSearch;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinClassFieldRuleSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldRuleSearchDTOv1, TwinClassFieldRuleSearch> {

    @Override
    public void map(TwinClassFieldRuleSearchDTOv1 src, TwinClassFieldRuleSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinClassFieldIdList(src.getTwinClassFieldIdList())
                .setTwinClassFieldIdExcludeList(src.getTwinClassFieldIdExcludeList())
                .setFieldOverwriterFeaturerIdList(src.getFieldOverwriterFeaturerIdList())
                .setFieldOverwriterFeaturerIdExcludeList(src.getFieldOverwriterFeaturerIdExcludeList());
    }
}
