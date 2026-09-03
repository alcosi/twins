package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldValidatorSearch;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldValidatorSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldValidatorSearchDTOv1, TwinClassFieldValidatorSearch> {

    @Override
    public void map(TwinClassFieldValidatorSearchDTOv1 src, TwinClassFieldValidatorSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinClassFieldIdList(src.getTwinClassFieldIdList())
                .setTwinClassFieldIdExcludeList(src.getTwinClassFieldIdExcludeList())
                .setFieldValidatorFeaturerIdList(src.getFieldValidatorFeaturerIdList())
                .setFieldValidatorFeaturerIdExcludeList(src.getFieldValidatorFeaturerIdExcludeList());
    }
}
