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
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.recomputer.RecomputerFieldDateShiftByDuration;
import org.twins.core.service.recompute.FieldRecomputeRequest;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RecomputerFieldDateShiftByDurationTest extends BaseUnitTest {

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

    private RecomputerFieldDateShiftByDuration recomputer;

    private static final UUID TARGET_ID = UUID.randomUUID();
    private static final UUID SOURCE_ID = UUID.randomUUID();
    private static final UUID DURATION_ID = UUID.randomUUID();
    private static final TwinClassFieldEntity TARGET_FIELD = new TwinClassFieldEntity()
            .setId(TARGET_ID)
            .setFieldTyperFeaturerId(1302);

    @BeforeEach
    void setUp() throws Exception {
        recomputer = new RecomputerFieldDateShiftByDuration(twinService, twinClassFieldService);
        inject(recomputer, "featurerService", featurerService);
        lenient().when(twinClassFieldService.getDateFieldPattern(TARGET_FIELD)).thenReturn("yyyy-MM-dd");
        lenient().when(featurerService.getFeaturer(eq(1302), eq(FieldTyper.class))).thenReturn(fieldTyper);
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

    private Properties props(boolean subtract) {
        var p = new Properties();
        p.setProperty("sourceDateTwinClassFieldId", SOURCE_ID.toString());
        p.setProperty("durationTwinClassFieldId", DURATION_ID.toString());
        p.setProperty("subtractDuration", Boolean.toString(subtract));
        return p;
    }

    private FieldRecomputeRequest request(TwinEntity twin) {
        return new FieldRecomputeRequest(twin, TARGET_FIELD, List.of());
    }

    @Nested
    class WhenOperandsPresent {
        @Test
        void addsDurationMinusOneDays() throws ServiceException {
            TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
            LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
            when(twinClassFieldService.getTimestampValue(twin, SOURCE_ID, null))
                    .thenReturn(Timestamp.valueOf(start));
            when(twinClassFieldService.getDecimalValue(twin, DURATION_ID, null))
                    .thenReturn(BigDecimal.valueOf(5));

            recomputer.recompute(request(twin), collector, props(false));

            ArgumentCaptor<FieldValueDate> captor = ArgumentCaptor.forClass(FieldValueDate.class);
            verify(fieldTyper).serializeValue(eq(twin), captor.capture(), eq(collector));
            assertEquals(LocalDateTime.of(2026, 3, 5, 0, 0), captor.getValue().getDate());
        }

        @Test
        void subtractsDurationMinusOneDays() throws ServiceException {
            TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
            LocalDateTime end = LocalDateTime.of(2026, 3, 5, 0, 0);
            when(twinClassFieldService.getTimestampValue(twin, SOURCE_ID, null))
                    .thenReturn(Timestamp.valueOf(end));
            when(twinClassFieldService.getDecimalValue(twin, DURATION_ID, null))
                    .thenReturn(BigDecimal.valueOf(5));

            recomputer.recompute(request(twin), collector, props(true));

            ArgumentCaptor<FieldValueDate> captor = ArgumentCaptor.forClass(FieldValueDate.class);
            verify(fieldTyper).serializeValue(eq(twin), captor.capture(), eq(collector));
            assertEquals(LocalDateTime.of(2026, 3, 1, 0, 0), captor.getValue().getDate());
        }
    }

    @Nested
    class WhenOperandsMissing {
        @Test
        void skipsWhenSourceMissing() throws ServiceException {
            TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
            when(twinClassFieldService.getTimestampValue(twin, SOURCE_ID, null)).thenReturn(null);
            when(twinClassFieldService.getDecimalValue(twin, DURATION_ID, null)).thenReturn(BigDecimal.TEN);

            recomputer.recompute(request(twin), collector, props(false));

            verify(fieldTyper, never()).serializeValue(any(), any(), any());
        }

        @Test
        void skipsWhenDurationMissing() throws ServiceException {
            TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
            when(twinClassFieldService.getTimestampValue(twin, SOURCE_ID, null))
                    .thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 3, 1, 0, 0)));
            when(twinClassFieldService.getDecimalValue(twin, DURATION_ID, null)).thenReturn(null);

            recomputer.recompute(request(twin), collector, props(false));

            verify(fieldTyper, never()).serializeValue(any(), any(), any());
        }
    }
}
