package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TwinStatisticProgressPercent implements TwinStatistic {
    private List<Item> items;
}
