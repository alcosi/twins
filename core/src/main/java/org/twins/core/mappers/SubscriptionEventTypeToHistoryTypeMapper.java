package org.twins.core.mappers;

import org.twins.core.dao.domain.SubscriptionEventType;
import org.twins.core.dao.history.HistoryType;

import java.util.*;

public class SubscriptionEventTypeToHistoryTypeMapper {

    private SubscriptionEventTypeToHistoryTypeMapper() {}

    public static List<HistoryType> map(SubscriptionEventType type) {
        List<HistoryType> historyTypes = null;

        switch (type) {
            case TWIN_CREATE -> historyTypes = Collections.singletonList(HistoryType.twinCreated);
            case TWIN_DELETE -> historyTypes = Collections.singletonList(HistoryType.twinDeleted);
            case TWIN_UPDATE -> historyTypes =  new ArrayList<>(
                    Arrays.stream(HistoryType.values())
                            .filter(historyType -> historyType != HistoryType.unknown && historyType != HistoryType.twinCreated && historyType != HistoryType.twinDeleted)
                            .toList()
            );
        }

        return historyTypes;
    }
}

