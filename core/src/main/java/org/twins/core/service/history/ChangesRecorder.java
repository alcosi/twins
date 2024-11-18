package org.twins.core.service.history;

import lombok.Getter;
import org.cambium.common.util.ChangesHelper;

@Getter
public class ChangesRecorder<D, R> extends ChangesHelper {
    final D dbEntity;
    final D updateEntity;
    final R recorder;
    HistoryCollector historyCollector;
    boolean historyCollectorEnabled = true; // in some cases we do not need to collect history changes (before drafting for example, currently we do not collect history, only after )

    public ChangesRecorder(D dbEntity, D updateEntity, R recorder, HistoryCollector historyCollector) {
        super();
        this.historyCollector = historyCollector;
        this.dbEntity = dbEntity;
        this.updateEntity = updateEntity;
        this.recorder = recorder;
    }
}
