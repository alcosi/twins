package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinValidatorSetSearch;
import org.twins.core.dto.rest.validator.TwinValidatorSetSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorSetSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorSetSearchRqDTOv1, TwinValidatorSetSearch> {

    @Override
    public void map(TwinValidatorSetSearchRqDTOv1 src, TwinValidatorSetSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setInvert(src.getInvert());
    }

}
