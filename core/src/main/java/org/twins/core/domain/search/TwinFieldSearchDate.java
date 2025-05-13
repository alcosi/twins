package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class TwinFieldSearchDate extends TwinFieldSearch {

    LocalDateTime lessThen;
    LocalDateTime moreThen;
    LocalDateTime equals;
    boolean empty;

    public boolean isEmptySearch() {
        return ObjectUtils.isEmpty(lessThen) &&
                ObjectUtils.isEmpty(moreThen) &&
                ObjectUtils.isEmpty(equals);
    }

}
