package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinFieldLastChangeSearchRange extends TwinFieldLastChangeSearch {

    private LocalDateTime lessThenOrEquals;
    private LocalDateTime moreThenOrEquals;
    private LocalDateTime equals;

    @Override
    public boolean isEmptySearch() {
        return ObjectUtils.isEmpty(lessThenOrEquals)
                && ObjectUtils.isEmpty(moreThenOrEquals)
                && ObjectUtils.isEmpty(equals);
    }
}

