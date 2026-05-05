package org.twins.core.featurer.statistic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.dao.twin.TwinFieldValueProjection;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class StatisterFromFieldPercentTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    @InjectMocks
    private StatisterFromFieldPercent statister;

    private UUID twinClassFieldId;
    private UUID labelI18nId;

    @BeforeEach
    void setUp() {
        twinClassFieldId = UUID.randomUUID();
        labelI18nId = UUID.randomUUID();
    }

    private Properties props() {
        var props = new Properties();
        props.put("twinClassFieldId", twinClassFieldId.toString());
        props.put("key", "progress");
        props.put("labelI18nId", labelI18nId.toString());
        props.put("colorHex", "#FF0000");
        return props;
    }

    @Nested
    class GetStatistic {

        @Test
        void getStatistic_withFieldValue_returnsPercent() {
            var twinId = UUID.randomUUID();

            when(twinFieldDecimalRepository.valueByTwinId(any(), eq(twinClassFieldId)))
                    .thenReturn(List.of(new TwinFieldValueProjection(twinId, new BigDecimal("0.75"))));

            var result = statister.getStatistic(props(), Set.of(twinId));

            assertEquals(1, result.size());
            var stat = result.get(twinId);
            assertNotNull(stat);
            assertEquals(1, stat.getItems().size());
            assertEquals(75, stat.getItems().getFirst().getPercent());
            assertEquals("progress", stat.getItems().getFirst().getKey());
        }

        @Test
        void getStatistic_nullValue_returnsZeroPercent() {
            var twinId = UUID.randomUUID();

            when(twinFieldDecimalRepository.valueByTwinId(any(), eq(twinClassFieldId)))
                    .thenReturn(List.of(new TwinFieldValueProjection(twinId, null)));

            var result = statister.getStatistic(props(), Set.of(twinId));

            assertEquals(0, result.get(twinId).getItems().getFirst().getPercent());
        }

        @Test
        void getStatistic_noProjection_returnsZeroPercent() {
            var twinId = UUID.randomUUID();

            when(twinFieldDecimalRepository.valueByTwinId(any(), eq(twinClassFieldId)))
                    .thenReturn(Collections.emptyList());

            var result = statister.getStatistic(props(), Set.of(twinId));

            assertEquals(0, result.get(twinId).getItems().getFirst().getPercent());
        }
    }
}
