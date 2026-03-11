package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinFieldValueSearchNumeric extends TwinFieldValueSearch {

    private Double lessThen;
    private Double moreThen;
    private Double equals;

    @Override
    public boolean isEmptySearch() {
        return ObjectUtils.isEmpty(lessThen)
                && ObjectUtils.isEmpty(moreThen)
                && ObjectUtils.isEmpty(equals);
    }
}

