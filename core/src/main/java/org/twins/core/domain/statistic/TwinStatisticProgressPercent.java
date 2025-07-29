package org.twins.core.domain.statistic;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.domain.TwinStatistic;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinStatisticProgressPercent implements TwinStatistic {
    private List<Item> items;

    @Data
    @Accessors(chain = true)
    @FieldNameConstants
    public static class Item {
        private UUID labelI18nId;
        private String key;
        private int percent;
        private String colorHex;
    }
}
