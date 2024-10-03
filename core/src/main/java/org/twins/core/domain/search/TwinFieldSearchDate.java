package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class TwinFieldSearchDate extends TwinFieldSearch {

    LocalDateTime lessThen;
    LocalDateTime moreThen;
    LocalDateTime equals;

}
