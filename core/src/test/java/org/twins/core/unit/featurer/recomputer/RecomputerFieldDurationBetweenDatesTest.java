package org.twins.core.unit.featurer.recomputer;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.recomputer.RecomputerFieldDurationBetweenDates;
import org.twins.core.service.recompute.FieldRecomputeRequest;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RecomputerFieldDurationBetweenDatesTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;
    @Mock
    private TwinClassFieldService twinClassFieldService;
    @Mock
    private FeaturerService featurerService;
    @Mock
    private FieldTyper fieldTyper;
    @Mock
    private TwinChangesCollector collector;

    private RecomputerFieldDurationBetweenDates recomputer;

    private static final UUID DURATION_ID = UUID.randomUUID();
    private static final UUID START_ID = UUID.randomUUID();
    private static final UUID END_ID = UUID.randomUUID();
    private static final TwinClassFieldEntity DURATION_FIELD = new TwinClassFieldEntity()
            .setId(DURATION_ID)
            .setFieldTyperFeaturerId(1317);

    @BeforeEach
    void setUp() throws Exception {
        recomputer = new RecomputerFieldDurationBetweenDates(twinService, twinClassFieldService);
        inject(recomputer, "featurerService", featurerService);
        lenient().when(featurerService.getFeaturer(eq(1317), eq(FieldTyper.class))).thenReturn(fieldTyper);
    }

    private void inject(Object target, String name, Object value) throws Exception {
        Field f = findField(target.getClass(), name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("field not found: " + name);
    }

    private Properties props() {
        var p = new Properties();
        p.setProperty("startDateTwinClassFieldId", START_ID.toString());
        p.setProperty("endDateTwinClassFieldId", END_ID.toString());
        return p;
    }

    private FieldRecomputeRequest request(TwinEntity twin) {
        return new FieldRecomputeRequest(twin, DURATION_FIELD, List.of());
    }

    @Nested
    class WhenDatesPresent {
        @Test
        void setsInclusiveDayCount() throws ServiceException {
            TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
            when(twinClassFieldService.getTimestampValue(twin, START_ID, null))
                    .thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 3, 1, 0, 0)));
            when(twinClassFieldService.getTimestampValue(twin, END_ID, null))
                    .thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 3, 5, 0, 0)));

            recomputer.recompute(request(twin), collector, props());

            ArgumentCaptor<FieldValueText> captor = ArgumentCaptor.forClass(FieldValueText.class);
            verify(fieldTyper).serializeValue(eq(twin), captor.capture(), eq(collector));
            assertEquals("5", captor.getValue().getValue());
        }
    }

    @Nested
    class WhenDatesMissing {
        @Test
        void skipsWhenStartMissing() throws ServiceException {
            TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
            when(twinClassFieldService.getTimestampValue(twin, START_ID, null)).thenReturn(null);
            when(twinClassFieldService.getTimestampValue(twin, END_ID, null))
                    .thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 3, 5, 0, 0)));

            recomputer.recompute(request(twin), collector, props());

            verify(fieldTyper, never()).serializeValue(any(), any(), any());
        }
    }
}
