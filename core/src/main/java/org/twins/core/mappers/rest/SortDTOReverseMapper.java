package org.twins.core.mappers.rest;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.SortField;
import org.twins.core.dao.specifications.SortOption;
import org.twins.core.dto.rest.SortDTOv1;
import org.twins.core.enums.SortDirection;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Arrays;

@Component
public class SortDTOReverseMapper {
    public <S extends Enum<S> & SortField<?>> SortOption<S> convert(SortDTOv1 src, Class<S> sortEnumClass) throws ServiceException {
        if (src == null || src.getField() == null)
            return null;

        S enumValue = null;
        try {
            enumValue = Enum.valueOf(sortEnumClass, src.getField());
        } catch (IllegalArgumentException e) {
            throw new ServiceException(ErrorCodeTwins.SORT_FIELD_INCORRECT, "Incorrect sort field. Valid values: " + Arrays.toString(sortEnumClass.getEnumConstants()));
        }
        return new SortOption<>(
                enumValue,
                src.getDirection() != null
                        ? src.getDirection()
                        : SortDirection.ASC
        );
    }
}
