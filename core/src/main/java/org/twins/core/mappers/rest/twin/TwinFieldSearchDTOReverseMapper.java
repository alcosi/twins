package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
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
                    .setEquals(date.equals())
                    .setEmpty(date.empty());
        } else if (twinFieldSearchDTOv1 instanceof TwinFieldSearchBooleanDTOv1 booleanDto) {
          return new TwinFieldSearchBoolean()
                  .setValue(booleanDto.value());
        } else if (twinFieldSearchDTOv1 instanceof TwinFieldSearchIdDTOv1 user) {
            return new TwinFieldSearchId()
                    .setIdList(user.idList())
                    .setIdExcludeList(user.idExcludeList());
        } else if (twinFieldSearchDTOv1 instanceof TwinFieldSearchUserDTOv1 user) {
            return new TwinFieldSearchUser()
                    .setIdList(user.idList())
                    .setIdExcludeList(user.idExcludeList());
        } else if (twinFieldSearchDTOv1 instanceof TwinFieldSearchSpaceRoleUserDTOv1 user) {
            return new TwinFieldSearchSpaceRoleUser()
                    .setRoleIdList(user.roleIdList())
                    .setRoleIdExcludeList(user.roleIdExcludeList())
                    .setUserIdList(user.userIdList())
                    .setUserIdExcludeList(user.userIdExcludeList());
        } else if (twinFieldSearchDTOv1 instanceof TwinFieldSearchNumericDTOv1 numeric) {
            Double less = null;
            Double more = null;
            Double equals = null;
            if (ObjectUtils.isNotEmpty(numeric.lessThen()))
                try {
                    less = Double.parseDouble(numeric.lessThen());
                } catch (NumberFormatException e) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT, "Incorrect value for LT compare with field: [" + numeric.lessThen() + "]");
                }
            if (ObjectUtils.isNotEmpty(numeric.moreThen()))
                try {
                    more = Double.parseDouble(numeric.moreThen());
                } catch (NumberFormatException e) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT, "Incorrect value for GT compare with field: [" + numeric.moreThen() + "]");
                }
            if (ObjectUtils.isNotEmpty(numeric.equals()))
                try {
                    equals = Double.parseDouble(numeric.equals());
                } catch (NumberFormatException e) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT, "Incorrect value for EQ compare with field: [" + numeric.equals() + "]");
                }
            return new TwinFieldSearchNumeric()
                    .setLessThen(less)
                    .setMoreThen(more)
                    .setEquals(equals);
        } else if (twinFieldSearchDTOv1 instanceof TwinFieldSearchTwinClassListDTOv1 dto) {
            return new TwinFieldSearchTwinClassList()
                    .setIdExcludeAllSet(dto.idExcludeAllSet())
                    .setIdIncludeAllSet(dto.idIncludeAllSet())
                    .setIdExcludeAnySet(dto.idExcludeAnySet())
                    .setIdIncludeAnySet(dto.idIncludeAnySet());
        } else {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, "Invalid search field type: " + twinFieldSearchDTOv1.type());
        }
    }
}
