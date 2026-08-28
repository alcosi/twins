package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinValidatorSearch;
import org.twins.core.dto.rest.validator.TwinValidatorSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorSearchDTOv1, TwinValidatorSearch> {

    @Override
    public void map(TwinValidatorSearchDTOv1 src, TwinValidatorSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinValidatorSetIdList(src.getTwinValidatorSetIdList())
                .setTwinValidatorSetIdExcludeList(src.getTwinValidatorSetIdExcludeList())
                .setValidatorFeaturerIdList(src.getValidatorFeaturerIdList())
                .setValidatorFeaturerIdExcludeList(src.getValidatorFeaturerIdExcludeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setInvert(src.getInvert())
                .setActive(src.getActive());
    }

}
