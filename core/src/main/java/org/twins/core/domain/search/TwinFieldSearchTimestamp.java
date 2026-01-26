package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinFieldSearchTimestamp extends TwinFieldSearch {
    private LocalDateTime beforeDate;
    private LocalDateTime afterDate;
    private LocalDateTime equals;

    public boolean isEmptySearch() {
        return ObjectUtils.isEmpty(equals) &&
                ObjectUtils.isEmpty(beforeDate) &&
                ObjectUtils.isEmpty(afterDate);
    }
}
