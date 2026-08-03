package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinValidatorSetSearch;
import org.twins.core.dto.rest.validator.TwinValidatorSetSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorSetSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorSetSearchDTOv1, TwinValidatorSetSearch> {

    @Override
    public void map(TwinValidatorSetSearchDTOv1 src, TwinValidatorSetSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setInvert(src.getInvert())
                .setUsageCountRange(src.getUsageCountRange());
    }

}
