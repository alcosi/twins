package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.*;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinFieldSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldSearchDTOv1, TwinFieldSearch> {

    @Override
    public void map(TwinFieldSearchDTOv1 src, TwinFieldSearch dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public TwinFieldSearch convert(TwinFieldSearchDTOv1 twinFieldSearchDTOv1, MapperContext mapperContext) throws Exception {
        if (twinFieldSearchDTOv1 instanceof TwinFieldSearchTextDTOv1 text) {
            return new TwinFieldSearchText()
                    .setValueLikeAnyOfList(text.valueLikeAnyOfList())
                    .setValueLikeAllOfList(text.valueLikeAllOfList())
                    .setValueLikeNoAnyOfList(text.valueLikeNoAnyOfList())
                    .setValueLikeNoAllOfList(text.valueLikeNoAllOfList());
        } else if (twinFieldSearchDTOv1 instanceof TwinFieldSearchListDTOv1 list) {
            return new TwinFieldSearchList()
                    .setOptionsAnyOfList(list.optionsAnyOfList())
                    .setOptionsAllOfList(list.optionsAllOfList())
                    .setOptionsNoAnyOfList(list.optionsNoAnyOfList())
                    .setOptionsNoAllOfList(list.optionsNoAllOfList());
        } else if (twinFieldSearchDTOv1 instanceof TwinFieldSearchDateDTOv1 date) {
             return new TwinFieldSearchDate()
                    .setLessThen(date.lessThen())
                    .setMoreThen(date.moreThen())
                    .setEquals(date.equals());
        } else if (twinFieldSearchDTOv1 instanceof TwinFieldSearchNumericDTOv1 numeric) {
            return new TwinFieldSearchNumeric()
                    .setLessThen(numeric.lessThen())
                    .setMoreThen(numeric.moreThen())
                    .setEquals(numeric.equals());
        } else {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, "Invalid search field type: " + twinFieldSearchDTOv1.type());
        }
    }
}
