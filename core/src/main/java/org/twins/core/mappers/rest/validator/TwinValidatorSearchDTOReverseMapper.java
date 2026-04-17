package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinValidatorSearch;
import org.twins.core.dto.rest.validator.TwinValidatorSearchRqDTOv1;
import org.twins.core.mappers.rest.IntegerRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorSearchRqDTOv1, TwinValidatorSearch> {
    private final IntegerRangeDTOReverseMapper integerRangeDTOReverseMapper;

    @Override
    public void map(TwinValidatorSearchRqDTOv1 src, TwinValidatorSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getSearch().getIdList())
                .setIdExcludeList(src.getSearch().getIdExcludeList())
                .setTwinValidatorSetIdList(src.getSearch().getTwinValidatorSetIdList())
                .setTwinValidatorSetIdExcludeList(src.getSearch().getTwinValidatorSetIdExcludeList())
                .setValidatorFeaturerIdList(src.getSearch().getValidatorFeaturerIdList())
                .setValidatorFeaturerIdExcludeList(src.getSearch().getValidatorFeaturerIdExcludeList())
                .setInvert(src.getSearch().getInvert())
                .setActive(src.getSearch().getActive())
                .setDescriptionLikeList(src.getSearch().getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getSearch().getDescriptionNotLikeList())
                .setOrder(integerRangeDTOReverseMapper.convert(src.getSearch().getOrder()));
    }
}
