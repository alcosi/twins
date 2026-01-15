package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum HistoryNotificationRecipientMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum HistoryNotificationRecipientCollector2HistoryNotificationRecipientMode implements MapperModePointer<HistoryNotificationRecipientCollectorMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public HistoryNotificationRecipientCollectorMode point() {
            return switch (this) {
                case HIDE -> HistoryNotificationRecipientCollectorMode.HIDE;
                case SHORT -> HistoryNotificationRecipientCollectorMode.SHORT;
                case DETAILED -> HistoryNotificationRecipientCollectorMode.DETAILED;
            };
        }
    }
}
