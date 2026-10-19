package org.twins.core.featurer.statistic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.dao.twin.TwinFieldHeadSumCountProjection;
import org.twins.core.dao.twin.TwinFieldValueProjection;
import org.twins.core.featurer.statistic.StatisterForParentOrChildPercent;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class StatisterForParentOrChildPercentTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    @InjectMocks
    private StatisterForParentOrChildPercent statister;

    private UUID headTwinClassFieldId;
    private UUID childTwinClassFieldId;
    private UUID labelI18nId;

    @BeforeEach
    void setUp() {
        headTwinClassFieldId = UUID.randomUUID();
        childTwinClassFieldId = UUID.randomUUID();
        labelI18nId = UUID.randomUUID();
    }

    private Properties props() {
        var props = new Properties();
        props.put("headTwinClassFieldId", headTwinClassFieldId.toString());
        props.put("childTwinClassFieldId", childTwinClassFieldId.toString());
        props.put("key", "progress");
        props.put("labelI18nId", labelI18nId.toString());
        props.put("colorHex", "#00FF00");
        return props;
    }

    @Nested
    class GetStatistic {

        @Test
        void getStatistic_withChildStats_usesAvgFromChildren() {
            var parentId = UUID.randomUUID();

            when(twinFieldDecimalRepository.sumAndCountByHeadTwinId(any(), eq(childTwinClassFieldId)))
                    .thenReturn(List.of(new TwinFieldHeadSumCountProjection(parentId, new BigDecimal("1.50"), 2L)));

            var result = statister.getStatistic(props(), Set.of(parentId));

            assertEquals(75, result.get(parentId).getItems().getFirst().getPercent());
        }

        @Test
        void getStatistic_noChildStats_usesParentFieldValue() {
            var parentId = UUID.randomUUID();

            when(twinFieldDecimalRepository.sumAndCountByHeadTwinId(any(), eq(childTwinClassFieldId)))
                    .thenReturn(Collections.emptyList());
            when(twinFieldDecimalRepository.valueByTwinId(any(), eq(headTwinClassFieldId)))
                    .thenReturn(List.of(new TwinFieldValueProjection(parentId, new BigDecimal("0.60"))));

            var result = statister.getStatistic(props(), Set.of(parentId));

            assertEquals(60, result.get(parentId).getItems().getFirst().getPercent());
        }

        @Test
        void getStatistic_zeroCount_returnsZeroPercent() {
            var parentId = UUID.randomUUID();

            when(twinFieldDecimalRepository.sumAndCountByHeadTwinId(any(), eq(childTwinClassFieldId)))
                    .thenReturn(List.of(new TwinFieldHeadSumCountProjection(parentId, new BigDecimal("1.00"), 0L)));

            var result = statister.getStatistic(props(), Set.of(parentId));

            assertEquals(0, result.get(parentId).getItems().getFirst().getPercent());
        }

        @Test
        void getStatistic_nullSum_returnsZeroPercent() {
            var parentId = UUID.randomUUID();

            when(twinFieldDecimalRepository.sumAndCountByHeadTwinId(any(), eq(childTwinClassFieldId)))
                    .thenReturn(List.of(new TwinFieldHeadSumCountProjection(parentId, null, 5L)));

            var result = statister.getStatistic(props(), Set.of(parentId));

            assertEquals(0, result.get(parentId).getItems().getFirst().getPercent());
        }
    }
}
