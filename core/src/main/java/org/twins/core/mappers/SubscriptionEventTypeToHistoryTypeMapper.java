package org.twins.core.mappers;

import org.twins.core.dao.domain.SubscriptionEventType;
import org.twins.core.enums.history.HistoryType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SubscriptionEventTypeToHistoryTypeMapper {

    private SubscriptionEventTypeToHistoryTypeMapper() {}

    public static List<HistoryType> map(SubscriptionEventType type) {
        List<HistoryType> historyTypes = null;

        switch (type) {
            case TWIN_CREATED -> historyTypes = Collections.singletonList(HistoryType.twinCreated);
            case TWIN_DELETED -> historyTypes = Collections.singletonList(HistoryType.twinDeleted);
            case TWIN_UPDATED -> historyTypes =  new ArrayList<>(
                    Arrays.stream(HistoryType.values())
                            .filter(historyType -> historyType != HistoryType.unknown && historyType != HistoryType.twinCreated && historyType != HistoryType.twinDeleted)
                            .toList()
            );
        }

        return historyTypes;
    }
}

