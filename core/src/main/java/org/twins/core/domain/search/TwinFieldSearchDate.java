package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinFieldSearchDate extends TwinFieldSearch {
    private LocalDateTime lessThen;
    private LocalDateTime moreThen;
    private LocalDateTime equals;
    private boolean empty;

    public boolean isEmptySearch() {
        return ObjectUtils.isEmpty(lessThen) &&
                ObjectUtils.isEmpty(moreThen) &&
                ObjectUtils.isEmpty(equals);
    }
}
